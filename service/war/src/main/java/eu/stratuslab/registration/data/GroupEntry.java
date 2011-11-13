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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.AuthenticationException;
import javax.naming.Context;
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

public final class GroupEntry {

    private static final String GROUP_NAME_ATTRIBUTE = "cn";

    private static final String GROUP_MEMBER_ATTRIBUTE = "uniquemember";

    private static final String DATABASE_CONNECT_ERROR = "error contacting database";

    private static final Logger LOGGER = Logger.getLogger("org.restlet");

    private GroupEntry() {

    }

    public static void addUserToGroup(String group, Attribute userDn,
            LdapConfig ldapEnv) {

        String user = getUserFromAttribute(userDn);

        if (user != null) {
            if (!groupContainsUser(group, user, ldapEnv)) {
                Form form = createMemberForm(user);
                rawUpdateGroup(group, DirContext.ADD_ATTRIBUTE, form, ldapEnv);
            }
        }

    }

    public static void remoteUserFromGroup(String group, Attribute userDn,
            LdapConfig ldapEnv) {

        String user = getUserFromAttribute(userDn);

        if (user != null) {
            if (groupContainsUser(group, user, ldapEnv)) {
                Form form = createMemberForm(user);
                rawUpdateGroup(group, DirContext.REMOVE_ATTRIBUTE, form,
                        ldapEnv);
            }
        }

    }

    public static String getUserFromAttribute(Attribute userDn) {
        if (userDn != null) {
            try {
                return userDn.get().toString();
            } catch (NamingException e) {
                LOGGER.severe(e.getMessage());
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                        "error obtaining attribute value");
            }
        } else {
            return null;
        }
    }

    public static Form createMemberForm(String user) {
        Form form = new Form();
        form.add(GROUP_MEMBER_ATTRIBUTE, user);
        return form;
    }

    public static boolean groupContainsUser(String group, String user,
            LdapConfig ldapEnv) {

        Set<String> members = getGroupMembers(group, ldapEnv);
        return members.contains(user);
    }

    public static Set<String> getGroupMembers(String group, LdapConfig ldapEnv) {

        Set<String> members = new HashSet<String>();

        Attributes attrs = getGroupAttributes(group, ldapEnv);

        try {

            NamingEnumeration<? extends Attribute> i = attrs.getAll();
            while (i.hasMore()) {
                Attribute attr = i.next();
                String key = attr.getID();
                if (GROUP_MEMBER_ATTRIBUTE.equalsIgnoreCase(key)) {
                    String member = attr.get().toString();
                    members.add(member);
                }
            }

        } catch (NamingException e) {

            LOGGER.severe(e.getMessage());
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error contacting database");

        }

        return members;
    }

    public static void rawUpdateGroup(String group, int ldapAction, Form form,
            LdapConfig ldapEnv) {

        String dn = GROUP_NAME_ATTRIBUTE + "=" + group;

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            // Copy all of the attributes.
            Attributes attrs = new BasicAttributes(true);
            for (Parameter parameter : form) {
                attrs.put(parameter.getName(), parameter.getValue());
            }

            if (attrs.size() > 0) {
                ctx.modifyAttributes(dn, ldapAction, attrs);
            }

        } catch (InvalidAttributesException e) {

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "incomplete user entry");

        } catch (AuthenticationException e) {

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } catch (NamingException e) {

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } finally {
            freeContext(ctx);
        }

    }

    public static Attributes getGroupAttributes(String group, LdapConfig ldapEnv) {

        Attributes attrs = null;

        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            Attributes matchingAttrs = new BasicAttributes(true);
            matchingAttrs.put(GROUP_NAME_ATTRIBUTE, group);

            NamingEnumeration<SearchResult> results = ctx.search("",
                    matchingAttrs, null);

            if (results.hasMore()) {
                SearchResult result = results.next();
                attrs = result.getAttributes();
            } else {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                        "group record not found for " + group);
            }

            if (results.hasMore()) {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                        "multiple records found for group " + group);
            }

        } catch (InvalidAttributesException e) {

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "incomplete group entry");

        } catch (AuthenticationException e) {

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } catch (NamingException e) {

            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } finally {
            freeContext(ctx);
        }

        return attrs;
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
