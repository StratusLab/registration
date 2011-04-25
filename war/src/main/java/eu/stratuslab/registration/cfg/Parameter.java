package eu.stratuslab.registration.cfg;

import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public enum Parameter {

    LDAP_SCHEME(true, "ldap", "LDAP scheme to access server (ldap or ldaps)") {
        @Override
        public void validate(String value) {
            super.validate(value);
            if (!("ldap".equals(value) || "ldaps".equals(value))) {
                throw new IllegalArgumentException(getKey()
                        + " must be 'ldap' or 'ldaps'");
            }
        }
    },

    LDAP_HOST(true, "localhost", "LDAP URL for DN holding user entries") {
    },

    LDAP_PORT(true, "10389", "LDAP URL for DN holding user entries") {
        @Override
        public void validate(String value) {
            super.validate(value);
            isValidPort(value);
        }
    },

    LDAP_BASE_DN(true, "ou=users,ou=system",
            "LDAP URL for DN holding user entries") {
    },

    LDAP_MANAGER_DN(true, "uid=admin,ou=system", "DN for the LDAP manager") {
    },

    LDAP_MANAGER_PASSWORD(true, null,
            "Password associated with LDAP manager DN") {
    },

    ADMIN_EMAIL(true, "Email address for account approvals, etc.") {
        @Override
        public void validate(String value) {
            super.validate(value);
            isValidEmail(value);
        }
    },

    MAIL_HOST(true, "Host for SMTP server for email notifications."),

    MAIL_PORT(false, "Port on SMTP server (defaults to standard ports).") {
        @Override
        public void validate(String value) {
            super.validate(value);
            isValidPort(value);
        }
    },

    MAIL_USER(true, "Username for SMTP server."),

    MAIL_PASSWORD(false, "Password for SMTP server."),

    MAIL_SSL(false, "Use SSL for SMTP server (default is 'true').") {
        @Override
        public void validate(String value) {
            super.validate(value);
            isBoolean(value);
        }
    },

    MAIL_DEBUG(false, "Debug mail sending (default is 'false').") {
        @Override
        public void validate(String value) {
            super.validate(value);
            isBoolean(value);
        }
    };

    private final String key;
    private final boolean required;
    private final String description;
    private final String defaultValue;

    private Parameter(boolean required, String description) {
        this(required, null, description);
    }

    private Parameter(boolean required, String defaultValue, String description) {
        this.key = nameToKey(this.name());
        this.required = required;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getProperty(Properties properties) {
        String value = (String) properties.get(key);
        return (value != null) ? value : defaultValue;
    }

    public void validate(String value) {
        if (isRequired()) {
            if (value != null) {
                if ("".equals(value)) {
                    throw new IllegalArgumentException("required parameter ("
                            + getKey() + ") is not defined");
                }
            } else {
                if (defaultValue == null) {
                    throw new IllegalArgumentException("required parameter ("
                            + getKey() + ") is not defined");
                }
            }
        }
    }

    public static Parameter parameterFromKey(Object key) {
        String skey = key.toString().toUpperCase().replace('.', '_');
        return Parameter.valueOf(skey);
    }

    public static String nameToKey(String name) {
        return name.toLowerCase().replace('_', '.');
    }

    private static void isBoolean(String s) {
        if (!("true".equals(s) || "false".equals(s))) {
            throw new IllegalArgumentException(
                    "value must be 'true' or 'false'");
        }
    }

    private static void isValidPort(String s) {
        try {
            int port = Integer.parseInt(s);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("invalid port number("
                        + port + ")");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private static void isValidEmail(String s) {
        try {
            new InternetAddress(s);
            if (!Pattern.matches(".+@.+", s)) {
                throw new IllegalArgumentException("invalid email address: "
                        + s);

            }
        } catch (AddressException e) {
            throw new IllegalArgumentException("invalid email address: "
                    + e.getMessage());
        }
    }

}
