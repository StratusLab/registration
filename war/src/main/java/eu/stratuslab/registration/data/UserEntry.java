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

import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.AuthenticationException;
import javax.naming.Context;
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

import eu.stratuslab.registration.utils.LdapConfig;

public final class UserEntry {

    private static final String DATABASE_CONNECT_ERROR = "error contacting database";

    private static final Logger LOGGER = Logger.getLogger("org.restlet");

    private UserEntry() {

    }

    public static String createUser(Form form, LdapConfig ldapEnv) {

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
            freeContext(ctx);
        }

        // createCertDnEntry(dn, form, ldapEnv);

        return uid;
    }

    public static void createCertDnEntry(String userDN, Form form,
            LdapConfig ldapEnv) {

        String certDN = form.getFirstValue(UserAttribute.X500_DN.key);

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            // Copy all of the attributes.
            Attributes attrs = new BasicAttributes(true);
            attrs.put("objectClass", "alias");
            attrs.put("objectClass", "extensibleObject");
            attrs.put("aliasedObjectName", userDN);

            ctx.createSubcontext(certDN, attrs);

        } catch (NameAlreadyBoundException e) {

            e.printStackTrace();
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "certificate DN (" + certDN + ") already exists");

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
            freeContext(ctx);
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

        // If certificate DN is just white space, then remove the attribute.
        String dn = form.getFirstValue(UserAttribute.X500_DN.key);
        if (dn != null) {
            if (UserAttribute.isWhitespace(dn)) {
                form.removeAll(UserAttribute.X500_DN.key);
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

    public static void updateUser(Form form, LdapConfig ldapEnv) {

        checkUserUpdateFormCorrect(form, ldapEnv);

        rawUpdateUser(form, ldapEnv);

    }

    public static void rawUpdateUser(Form form, LdapConfig ldapEnv) {

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
            freeContext(ctx);
        }

    }

    public static void checkUserUpdateFormCorrect(Form form, LdapConfig ldapEnv) {

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
            LdapConfig ldapEnv) {

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

    public static Properties getUserProperties(String uid, LdapConfig ldapEnv) {

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

    public static Attributes getUserAttributes(String uid, LdapConfig ldapEnv) {

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
            freeContext(ctx);
        }

        return attrs;
    }

    public static String getEmailAddress(String uid, LdapConfig ldapEnv) {

        String userEmail = null;
        Attributes attrs = UserEntry.getUserAttributes(uid, ldapEnv);
        Attribute attr = attrs.get(UserAttribute.EMAIL.key);
        if (attr != null) {
            try {
                userEmail = (String) attr.get();
            } catch (NamingException consumed) {
                consumed.printStackTrace();
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                        "missing email address in record");
            }
        } else {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "missing email address in record");
        }

        return userEmail;
    }

    private static void freeContext(Context ctx) {
        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                LOGGER.warning("cannot free directory context: "
                        + e.getMessage());
            }
        }
    }

}
