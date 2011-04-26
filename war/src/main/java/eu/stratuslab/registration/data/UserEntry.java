package eu.stratuslab.registration.data;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.InvalidAttributesException;
import javax.naming.directory.SearchResult;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

public final class UserEntry {

    public static final String COMMON_NAME_KEY = "cn";

    public static final String OBJECT_CLASS_KEY = "objectClass";

    private static final String DATABASE_CONNECT_ERROR = "error contacting database";

    private UserEntry() {

    }

    // Removes unknown keys from form and parameters with empty values.
    // This method does NOT validate the parameters.
    public static Form sanitizeForm(Form form) {

        Form sanitizedForm = new Form();

        for (Parameter parameter : form) {
            String key = parameter.getName();
            try {
                UserAttribute.valueWithKey(key);
                String value = parameter.getValue();
                if (UserAttribute.isNotWhitespace(value)) {
                    sanitizedForm.add(parameter);
                }
            } catch (IllegalArgumentException consumed) {
                // Do not copy the parameter into the new form.
            }
        }

        return sanitizedForm;
    }

    public static void validateEntries(Form form) {

        for (Parameter parameter : form) {
            String key = parameter.getName();
            String value = parameter.getValue();

            UserAttribute attr = UserAttribute.valueWithKey(key);

            if (!attr.isValid(value)) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "invalid " + attr.name);
            }
        }

    }

    public static void addDerivedAttributes(Form form) {

        String surname = form.getFirstValue(UserAttribute.SURNAME.key);
        String givenName = form.getFirstValue(UserAttribute.GIVEN_NAME.key);

        String cn = givenName + " " + surname;

        form.add(COMMON_NAME_KEY, cn);

        form.add(OBJECT_CLASS_KEY, "inetOrgPerson");

    }

    public static void createUser(Form form, Hashtable<String, String> ldapEnv) {

        checkUserCreateFormCorrect(form);

        String uid = form.getFirstValue(UserAttribute.UID.key);
        String dn = UserAttribute.UID.key + "=" + uid;

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            // Copy all of the attributes.
            Attributes attrs = new BasicAttributes(true);
            for (Parameter parameter : form) {
                attrs.put(parameter.getName(), parameter.getValue());
            }

            ctx.createSubcontext(dn, attrs);

        } catch (NameAlreadyBoundException e) {

            e.printStackTrace();
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "username (" + uid + ") already exists");

        } catch (InvalidAttributesException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "incomplete user entry");

        } catch (AuthenticationException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } finally {

            // Return the connection to the pool.
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException consumed) {
                    // TODO: Log this.
                }
            }
        }

    }

    public static void checkUserCreateFormCorrect(Form form) {

        // All required attributes exist.
        for (UserAttribute attr : UserAttribute.values()) {
            if (attr.isRequiredForCreate) {
                if (form.getFirstValue(attr.key) == null) {
                    throw new ResourceException(
                            Status.CLIENT_ERROR_BAD_REQUEST, "missing "
                                    + attr.key);
                }
            }
        }

        // Passwords match.
        String pswd1 = form.getFirstValue(UserAttribute.NEW_PASSWORD.key);
        String pswd2 = form.getFirstValue(UserAttribute.NEW_PASSWORD_CHECK.key);

        if (!pswd1.equals(pswd2)) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "mismatched passwords");
        }

        // Copy password to 'userPassword'.
        form.removeAll(UserAttribute.PASSWORD.key);
        form.add(UserAttribute.PASSWORD.key, pswd1);

        stripNonLdapAttributes(form);
    }

    public static void stripNonLdapAttributes(Form form) {
        form.removeAll(UserAttribute.NEW_PASSWORD.key);
        form.removeAll(UserAttribute.NEW_PASSWORD_CHECK.key);
        form.removeAll(UserAttribute.AGREEMENT.key);
        form.removeAll(UserAttribute.MESSAGE.key);
    }

    public static void updateUser(Form form, Hashtable<String, String> ldapEnv) {

        checkUserUpdateFormCorrect(form, ldapEnv);

        String uid = form.getFirstValue(UserAttribute.UID.key);
        String dn = UserAttribute.UID.key + "=" + uid;

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            // Copy all of the attributes.
            Attributes attrs = new BasicAttributes(true);
            for (Parameter parameter : form) {
                attrs.put(parameter.getName(), parameter.getValue());
            }

            ctx.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE, attrs);

        } catch (InvalidAttributesException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "incomplete user entry");

        } catch (AuthenticationException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } finally {

            // Return the connection to the pool.
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException consumed) {
                    // TODO: Log this.
                }
            }
        }

    }

    public static void checkUserUpdateFormCorrect(Form form,
            Hashtable<String, String> ldapEnv) {

        // All required attributes exist.
        for (UserAttribute attr : UserAttribute.values()) {
            if (attr.isRequiredForUpdate) {
                if (form.getFirstValue(attr.key) == null) {
                    throw new ResourceException(
                            Status.CLIENT_ERROR_BAD_REQUEST, "missing "
                                    + attr.key);
                }
            }
        }

        // Check the current password.
        String uid = form.getFirstValue(UserAttribute.UID.key);
        String currentPassword = form.getFirstValue(UserAttribute.PASSWORD.key);
        checkCurrentPassword(uid, currentPassword, ldapEnv);

        // New passwords match, if given.
        String pswd1 = form.getFirstValue(UserAttribute.NEW_PASSWORD.key);
        String pswd2 = form.getFirstValue(UserAttribute.NEW_PASSWORD_CHECK.key);

        if (pswd1 != null || pswd2 != null) {
            if (pswd1 == null || !pswd1.equals(pswd2)) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "mismatched passwords");
            }
        }

        // Copy password to 'userPassword' only if a new value has been set.
        form.removeAll(UserAttribute.PASSWORD.key);
        if (pswd1 != null) {
            form.set(UserAttribute.PASSWORD.key, pswd1);
        }

        stripNonLdapAttributes(form);
    }

    public static void checkCurrentPassword(String uid, String currentPassword,
            Hashtable<String, String> ldapEnv) {

        Attributes attrs = getUserAttributes(uid, ldapEnv);
        String ldapPassword = extractPassword(attrs);

        if (!currentPassword.equals(ldapPassword)) {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "incorrect password");
        }

    }

    public static String extractPassword(Attributes attrs) {

        String ldapPassword = "";

        try {

            Attribute attr = attrs.get(UserAttribute.PASSWORD.key);
            if (attr != null) {
                byte[] bytes;
                bytes = (byte[]) attr.get();
                ldapPassword = new String(bytes);
            }

        } catch (NamingException consumed) {
            // Do nothing; return empty password.
        }

        return ldapPassword;
    }

    public static Properties getUserProperties(String uid,
            Hashtable<String, String> ldapEnv) {

        Properties userProperties = new Properties();

        Attributes attrs = getUserAttributes(uid, ldapEnv);

        try {

            NamingEnumeration<String> ids = attrs.getIDs();
            while (ids.hasMore()) {
                String id = ids.next();
                Attribute attr = attrs.get(id);
                Object value = attr.get();

                // Ensure that password is not sent back through browser.
                if (UserAttribute.PASSWORD.key.equals(id)) {
                    value = "";
                }
                userProperties.put(id, value);
            }

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        }

        return userProperties;
    }

    public static Attributes getUserAttributes(String uid,
            Hashtable<String, String> ldapEnv) {

        Attributes attrs = null;

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            Attributes matchingAttrs = new BasicAttributes(true);
            matchingAttrs.put(UserAttribute.UID.key, uid);

            NamingEnumeration<SearchResult> results = ctx.search("",
                    matchingAttrs, null);

            if (results.hasMore()) {
                SearchResult result = results.next();
                attrs = result.getAttributes();
            } else {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                        "user record not found for " + uid);
            }

            if (results.hasMore()) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                        "multiple records found for " + uid);
            }

        } catch (InvalidAttributesException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "incomplete user entry");

        } catch (AuthenticationException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } finally {

            // Return the connection to the pool.
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException consumed) {
                    // TODO: Log this.
                }
            }
        }

        return attrs;
    }

}
