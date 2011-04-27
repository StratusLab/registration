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
package eu.stratuslab.registration.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;

/**
 * Simply a marker class to make type declarations clearer.
 * 
 * @author loomis
 * 
 */
@SuppressWarnings("serial")
public class LdapConfig extends Hashtable<String, String> {

    private static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final String CONNECTION_POOL_KEY = "com.sun.jndi.ldap.connect.pool";

    public LdapConfig() {
        super();
    }

    public LdapConfig(Map<String, String> map) {
        super(map);
    }

    public static Map<String, String> create(String ldapScheme,
            String ldapHost, String ldapPort, String baseDn, String managerDn,
            String managerPassword) {

        // Set up environment for creating initial context
        Map<String, String> cfg = new HashMap<String, String>();

        String ldapUrl = String.format("%s://%s:%s/%s", ldapScheme, ldapHost,
                ldapPort, baseDn);

        cfg.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
        cfg.put(Context.PROVIDER_URL, ldapUrl);

        // Enable connection pooling
        cfg.put(CONNECTION_POOL_KEY, "true");

        // Setup the authentication.
        cfg.put(Context.SECURITY_AUTHENTICATION, "simple");
        cfg.put(Context.SECURITY_PRINCIPAL, managerDn);
        cfg.put(Context.SECURITY_CREDENTIALS, managerPassword);

        return Collections.unmodifiableMap(cfg);
    }

}
