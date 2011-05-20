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
package eu.stratuslab.registration.data;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

public enum UserAttribute {

    UID("uid", "username", false, true, true) {
        @Override
        public boolean isValid(Object o) {
            return isValidUsername(o);
        }
    }, //

    EMAIL("mail", "email address", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isValidEmailAddress(o);
        }
    }, //

    GIVEN_NAME("givenName", "given name", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isNotWhitespace(o);
        }
    }, //

    SURNAME("sn", "family name", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isNotWhitespace(o);
        }
    }, //

    X500_DN("seeAlso", "X500 DN", true, false, false) {
        @Override
        public boolean isValid(Object o) {
            return isWhitespace(o) || isValidCertificateDN(o);
        }
    }, //

    PASSWORD("userPassword", "password", true, false, true) {
        @Override
        public boolean isValid(Object o) {
            return isValidPassword(o);
        }
    }, //

    NEW_PASSWORD("newUserPassword", "new password", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isValidPassword(o);
        }
    }, //

    NEW_PASSWORD_CHECK("newUserPasswordCheck", "new password", true, true,
            false) {
        @Override
        public boolean isValid(Object o) {
            return isValidPassword(o);
        }
    }, //

    MESSAGE("message", "message", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            return isNotWhitespace(o);
        }
    }, //

    AGREEMENT("agreement", "agreement", true, true, false) {
        @Override
        public boolean isValid(Object o) {
            // Only sent if the checkbox is ticked.
            // Must check separately if it is missing.
            return true;
        }
    };

    private static final Pattern VALID_USERNAME = Pattern
            .compile("^\\w{3,20}$");

    public static final Pattern WHITESPACE_ONLY = Pattern.compile("^\\s*$");

    private static final Pattern VALID_PASSWORD = Pattern
            .compile("^\\S{8,20}$");

    private static final Map<String, UserAttribute> ID_TO_VALUE = new HashMap<String, UserAttribute>();

    static {
        for (UserAttribute attr : UserAttribute.values()) {
            ID_TO_VALUE.put(attr.key, attr);
        }
    }

    public final String key;

    public final String name;

    public final boolean isModifiable;

    public final boolean isRequiredForCreate;

    public final boolean isRequiredForUpdate;

    UserAttribute(String key, String name, boolean isModifiable,
            boolean isRequiredForCreate, boolean isRequiredForUpdate) {
        this.key = key;
        this.name = name;
        this.isModifiable = isModifiable;
        this.isRequiredForCreate = isRequiredForCreate;
        this.isRequiredForUpdate = isRequiredForUpdate;
    }

    public abstract boolean isValid(Object o);

    public static UserAttribute valueWithKey(String key) {
        UserAttribute attr = ID_TO_VALUE.get(key);
        if (attr == null) {
            throw new IllegalArgumentException("no UserAttribute with key = "
                    + key);
        }
        return attr;
    }

    public static boolean isValidUsername(Object username) {

        if (username == null) {
            return false;
        }

        Matcher matcher = VALID_USERNAME.matcher(username.toString());
        if (!matcher.matches()) {
            return false;
        }

        return true;
    }

    public static boolean isValidCertificateDN(Object name) {

        if (name == null) {
            return false;
        }

        String dn = name.toString();

        try {
            LdapName ldapName = new LdapName(dn);
            return (!ldapName.isEmpty());
        } catch (InvalidNameException e) {
            return false;
        }
    }

    public static boolean isValidEmailAddress(Object email) {

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

    public static boolean isWhitespace(Object string) {

        if (string == null) {
            return true;
        }

        Matcher matcher = WHITESPACE_ONLY.matcher(string.toString());
        return matcher.matches();
    }

    public static boolean isNotWhitespace(Object string) {
        return !isWhitespace(string);
    }

    public static boolean isValidPassword(Object password) {

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
