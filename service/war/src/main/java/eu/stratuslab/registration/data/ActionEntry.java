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

import java.util.UUID;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.actions.Action;
import eu.stratuslab.registration.utils.LdapConfig;

public final class ActionEntry {

    private static final String DATABASE_CONNECT_ERROR = "error contacting database";

    private static final Logger LOGGER = Logger.getLogger("org.restlet");

    private ActionEntry() {

    }

    public static String storeAction(Action action, LdapConfig ldapEnv) {

        String uuid = UUID.randomUUID().toString();
        String dn = "cn=" + uuid;

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            ctx.bind(dn, action);

        } catch (NameAlreadyBoundException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "entry exists: " + dn);

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } finally {
            freeContext(ctx);
        }

        return uuid;

    }

    public static Action retrieveAction(String uuid, LdapConfig ldapEnv) {

        String dn = "cn=" + uuid;

        // Get a connection from the pool.
        DirContext ctx = null;

        try {

            ctx = new InitialDirContext(ldapEnv);

            Action action = (Action) ctx.lookup(dn);

            try {
                ctx.destroySubcontext(dn);
            } catch (NamingException e) {
                LOGGER.warning("cannot delete action (" + dn + "): "
                        + e.getMessage());
            }

            return action;

        } catch (NamingException e) {

            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    DATABASE_CONNECT_ERROR);

        } finally {
            freeContext(ctx);
        }

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
