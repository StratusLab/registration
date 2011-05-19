/*
 Created as part of the StratusLab project (http://stratuslab.eu),
 co-funded by the European Commission under the Grant Agreement
 INFSO-RI-261552.

 Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.ext.freemarker.ContextTemplateLoader;

import eu.stratuslab.registration.utils.LdapConfig;
import freemarker.template.Configuration;

public final class AppConfiguration {

    private static final Logger LOGGER = Logger.getLogger("org.restlet");

    private static final String CONFIG_FILENAME = "registration.cfg";

    private final Properties properties;

    private final Configuration freeMarkerConfig;

    private final Map<String, String> ldapCfgMap;

    public AppConfiguration(Context context) {
        List<File> configFileLocations = getConfigurationFileLocations();
        properties = getConfigurationProperties(configFileLocations);
        freeMarkerConfig = createFreeMarkerConfig(context);

        ldapCfgMap = LdapConfig.create( //
                getValue(LDAP_SCHEME), //
                getValue(LDAP_HOST), //
                getValue(LDAP_PORT), //
                getValue(LDAP_BASE_DN), //
                getValue(LDAP_MANAGER_DN), //
                getValue(LDAP_MANAGER_PASSWORD));
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
                LOGGER.info("configuration file: " + f);
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
            } catch (IOException e) {
                LOGGER.warning("error loading properties file (" + configFile
                        + "): " + e.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.warning("error closing properties file ("
                            + configFile + "): " + e.getMessage());
                }
            }
        } catch (FileNotFoundException consumed) {
            // Return empty properties file.
        }

        return properties;
    }

    public String getValue(Parameter parameter) {
        return parameter.getProperty(properties);
    }

    public boolean getValueAsBoolean(Parameter parameter) {
        return Boolean.parseBoolean(parameter.getProperty(properties));
    }

    public int getValueAsInt(Parameter parameter) {
        return Integer.parseInt(parameter.getProperty(properties));
    }

    public long getValueAsLong(Parameter parameter) {
        return Long.parseLong(parameter.getProperty(properties));
    }

    public File getValueAsFile(Parameter parameter) {
        return new File(parameter.getProperty(properties));
    }

    public Configuration getFreeMarkerConfig() {
        return freeMarkerConfig;
    }

    public LdapConfig getLdapConfig() {
        return new LdapConfig(ldapCfgMap);
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

    private static Configuration createFreeMarkerConfig(Context context) {

        Configuration cfg = new Configuration();
        cfg.setLocalizedLookup(false);

        LocalReference fmBaseRef = LocalReference
                .createClapReference("/freemarker/");
        cfg.setTemplateLoader(new ContextTemplateLoader(context, fmBaseRef));

        return cfg;
    }

}
