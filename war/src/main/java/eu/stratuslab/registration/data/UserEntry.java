package eu.stratuslab.registration.data;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

public class UserEntry {

    public static final String COMMON_NAME_KEY = "cn";

    public static final String OBJECT_CLASS_KEY = "objectClass";

    private UserEntry() {

    }

    public static Form sanitizeForm(Form form) {

        Form sanitizedForm = new Form();

        for (Parameter parameter : form) {
            String key = parameter.getName();
            try {
                UserAttribute.valueWithKey(key);
                sanitizedForm.add(parameter);
            } catch (IllegalArgumentException consumed) {
                // Do not copy the parameter into the new form.
            }
        }

        return sanitizedForm;
    }

    public static void checkCompleteForm(Form form) {

        for (UserAttribute attr : UserAttribute.values()) {
            if (form.getFirstValue(attr.key) == null) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "missing " + attr.key);
            }
        }

    }

    public static void validateEntries(Form form) {

        for (Parameter parameter : form) {
            String key = parameter.getName();
            String value = parameter.getValue();

            UserAttribute attr = UserAttribute.valueWithKey(key);

            if (!attr.isValid(value)) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "invalid " + key);
            }
        }

    }

    public static void addDerivedAttributes(Form form) {

        String surname = form.getFirstValue(UserAttribute.SURNAME.key);
        String givenName = form.getFirstValue(UserAttribute.GIVEN_NAME.key);

        String cn = givenName + " " + surname;

        form.add(COMMON_NAME_KEY, cn);

        form.add(OBJECT_CLASS_KEY, "top");
        form.add(OBJECT_CLASS_KEY, "person");
        form.add(OBJECT_CLASS_KEY, "inetOrgPerson");

    }

    public static void createUser(Form form, Hashtable<String, String> ldapEnv) {

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

            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "username (" + uid + ") already exists");

        } catch (InvalidAttributesException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "incomplete user entry");

        } catch (AuthenticationException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error contacting database");

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error contacting database");

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

    public static void updateUser(Form form, Hashtable<String, String> ldapEnv) {

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
                    "error contacting database");

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error contacting database");

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

    public static Map<String, Object> listUsers(
            Hashtable<String, String> ldapEnv) {

        String[] desiredAttrs = new String[] { "uid", "cn" };

        List<Properties> userInfoList = new LinkedList<Properties>();

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            NamingEnumeration<SearchResult> results = ctx.search("", null,
                    desiredAttrs);
            while (results.hasMore()) {
                SearchResult result = results.next();
                String name = result.getName();
                Attributes attrs = result.getAttributes();
                Attribute uid = attrs.get("uid");
                Attribute cn = attrs.get("cn");

                Properties userProperties = new Properties();
                userProperties.put("name", name);
                userProperties.put("uid", uid.get());
                userProperties.put("cn", cn.get());

                userInfoList.add(userProperties);
            }

        } catch (InvalidAttributesException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "incomplete user entry");

        } catch (AuthenticationException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error contacting database");

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error contacting database");

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

        Map<String, Object> info = new HashMap<String, Object>();
        info.put("users", userInfoList);

        return info;
    }

    public static Properties getUserProperties(String uid,
            Hashtable<String, String> ldapEnv) {

        Properties userProperties = new Properties();

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            Attributes matchingAttrs = new BasicAttributes(true);
            matchingAttrs.put(UserAttribute.UID.key, uid);

            NamingEnumeration<SearchResult> results = ctx.search("",
                    matchingAttrs, null);

            while (results.hasMore()) {
                SearchResult result = results.next();

                Attributes attrs = result.getAttributes();
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

            }

        } catch (InvalidAttributesException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "incomplete user entry");

        } catch (AuthenticationException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error contacting database");

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error contacting database");

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

        return userProperties;
    }

}
