package eu.stratuslab.registration.cfg;

import static eu.stratuslab.registration.cfg.Parameter.LDAP_BASE_DN;
import static eu.stratuslab.registration.cfg.Parameter.LDAP_HOST;
import static eu.stratuslab.registration.cfg.Parameter.LDAP_MANAGER_DN;
import static eu.stratuslab.registration.cfg.Parameter.LDAP_MANAGER_PASSWORD;
import static eu.stratuslab.registration.cfg.Parameter.LDAP_PORT;
import static eu.stratuslab.registration.cfg.Parameter.LDAP_SCHEME;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.ext.freemarker.ContextTemplateLoader;

import eu.stratuslab.registration.utils.LDAPUtils;
import freemarker.template.Configuration;

public final class AppConfiguration {

    private static final String CONFIG_FILENAME = "registration.cfg";

    private final Properties properties;

    private final Configuration freeMarkerConfig;

    private final Hashtable<String, String> ldapEnv;

    public AppConfiguration(Context context) {
        List<File> configFileLocations = getConfigurationFileLocations();
        properties = getConfigurationProperties(configFileLocations);
        freeMarkerConfig = createFreeMarkerConfig(context);

        ldapEnv = LDAPUtils.createLdapConnectionEnvironment(
                getParameterValue(LDAP_SCHEME), //
                getParameterValue(LDAP_HOST), //
                getParameterValue(LDAP_PORT), //
                getParameterValue(LDAP_BASE_DN), //
                getParameterValue(LDAP_MANAGER_DN), //
                getParameterValue(LDAP_MANAGER_PASSWORD));
    }

    private static List<File> getConfigurationFileLocations() {

        ArrayList<File> locations = new ArrayList<File>();

        // Possible locations for the configuration file are the current working
        // directory, the user's home directory, or the standard system
        // location, in that order.
        File[] dirs = { new File(System.getProperty("user.dir")),
                new File(System.getProperty("user.home")),
                new File("/etc/stratuslab/") };

        for (File dir : dirs) {
            locations.add(new File(dir, CONFIG_FILENAME));
        }

        return Collections.unmodifiableList(locations);
    }

    private static Properties getConfigurationProperties(
            List<File> configFileLocations) {

        for (File f : configFileLocations) {
            if (f.canRead()) {
                Properties properties = loadProperties(f);
                validateConfiguration(properties);
                return properties;
            }
        }
        throw new RuntimeException("cannot locate configuration file");
    }

    private static Properties loadProperties(File configFile) {

        Properties properties = new Properties();

        try {
            Reader reader = new FileReader(configFile);
            try {
                properties.load(reader);
            } catch (IOException consumed) {
                // TODO: Add logging.
            } finally {
                try {
                    reader.close();
                } catch (IOException consumed) {
                    // TODO: Add logging.
                }
            }
        } catch (FileNotFoundException consumed) {
            // Return empty properties file.
        }

        return properties;
    }

    public String getParameterValue(Parameter parameter) {
        return parameter.getProperty(properties);
    }

    public boolean getParameterValueAsBoolean(Parameter parameter) {
        return Boolean.parseBoolean(parameter.getProperty(properties));
    }

    public int getParameterValueAsInt(Parameter parameter) {
        return Integer.parseInt(parameter.getProperty(properties));
    }

    public long getParameterValueAsLong(Parameter parameter) {
        return Long.parseLong(parameter.getProperty(properties));
    }

    public File getParameterValueAsFile(Parameter parameter) {
        return new File(parameter.getProperty(properties));
    }

    private static void validateConfiguration(Properties properties) {
        checkAllParametersAreValid(properties);
        checkAllParametersAreKnown(properties);
    }

    private static void checkAllParametersAreValid(Properties properties) {
        for (Parameter p : Parameter.values()) {
            String value = p.getProperty(properties);
            if (value != null) {
                p.validate(value);
            }
        }
    }

    private static void checkAllParametersAreKnown(Properties properties) {
        for (Object key : properties.keySet()) {
            Parameter.parameterFromKey(key);
        }
    }

    public Configuration getFreeMarkerConfig() {
        return freeMarkerConfig;
    }

    private static Configuration createFreeMarkerConfig(Context context) {

        Configuration cfg = new Configuration();
        cfg.setLocalizedLookup(false);

        LocalReference fmBaseRef = LocalReference
                .createClapReference("/freemarker/");
        cfg.setTemplateLoader(new ContextTemplateLoader(context, fmBaseRef));

        return cfg;
    }

    public Hashtable<String, String> getLdapEnv() {
        // TODO: Make this more efficient.
        return new Hashtable<String, String>(ldapEnv);
    }

}
