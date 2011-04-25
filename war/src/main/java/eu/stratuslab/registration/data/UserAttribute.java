package eu.stratuslab.registration.data;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public enum UserAttribute {

    UID("uid", false, true, true) {
        @Override
        public boolean isValid(Object o) {
            return isValidUsername(o);
        }
    }, //

    EMAIL("mail", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isValidEmailAddress(o);
        }
    }, //

    GIVEN_NAME("givenName", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isNotEmptyString(o);
        }
    }, //

    SURNAME("sn", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isNotEmptyString(o);
        }
    }, //

    PASSWORD("userPassword", true, false, true) {
        @Override
        public boolean isValid(Object o) {
            return isValidPassword(o);
        }
    }, //

    NEW_PASSWORD("newUserPassword", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isValidPassword(o);
        }
    }, //

    NEW_PASSWORD_CHECK("newUserPasswordCheck", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isValidPassword(o);
        }
    }, //

    MESSAGE("message", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isNotEmptyString(o);
        }
    }, //

    AGREEMENT("agreement", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            // Only sent if the checkbox is ticked.
            // Must check separately if it is missing.
            return true;
        }
    };

    private static final Pattern VALID_USERNAME = Pattern
            .compile("^\\w{3,20}$");

    private static final Pattern WHITESPACE_ONLY = Pattern.compile("^\\s*$");

    private static final Pattern VALID_PASSWORD = Pattern
            .compile("^\\S{8,20}$");

    private static final Map<String, UserAttribute> ID_TO_VALUE = new HashMap<String, UserAttribute>();

    static {
        for (UserAttribute attr : UserAttribute.values()) {
            ID_TO_VALUE.put(attr.key, attr);
        }
    }

    public final String key;

    public final boolean isModifiable;

    public final boolean isRequiredForCreate;

    public final boolean isRequiredForUpdate;

    UserAttribute(String key, boolean isModifiable,
            boolean isRequiredForCreate, boolean isRequiredForUpdate) {
        this.key = key;
        this.isModifiable = isModifiable;
        this.isRequiredForCreate = isRequiredForCreate;
        this.isRequiredForUpdate = isRequiredForUpdate;
    }

    abstract public boolean isValid(Object o);

    public static UserAttribute valueWithKey(String key) {
        UserAttribute attr = ID_TO_VALUE.get(key);
        if (attr == null) {
            throw new IllegalArgumentException("no UserAttribute with key = "
                    + key);
        }
        return attr;
    }

    private static boolean isValidUsername(Object username) {

        if (username == null) {
            return false;
        }

        Matcher matcher = VALID_USERNAME.matcher(username.toString());
        if (!matcher.matches()) {
            return false;
        }

        return true;
    }

    private static boolean isValidEmailAddress(Object email) {

        if (email == null) {
            return false;
        }

        String address = email.toString();

        try {
            new InternetAddress(address);
            String[] parts = address.split("@");
            return parts.length == 2 && !"".equals(parts[0])
                    && !"".equals(parts[1]);
        } catch (AddressException e) {
            return false;
        }
    }

    public static boolean isNotEmptyString(Object name) {
        if (name == null) {
            return false;
        }

        Matcher matcher = WHITESPACE_ONLY.matcher(name.toString());
        if (matcher.matches()) {
            return false;
        }

        return true;
    }

    private static boolean isValidPassword(Object password) {

        if (password == null) {
            return false;
        }

        Matcher matcher = VALID_PASSWORD.matcher(password.toString());
        if (!matcher.matches()) {
            return false;
        }

        return true;
    }

}
