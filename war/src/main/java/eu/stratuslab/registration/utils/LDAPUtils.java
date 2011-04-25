package eu.stratuslab.registration.utils;

import java.util.Hashtable;

import javax.naming.Context;

public final class LDAPUtils {

    private static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final String CONNECTION_POOL_KEY = "com.sun.jndi.ldap.connect.pool";

    private LDAPUtils() {

    }

    public static Hashtable<String, String> createLdapConnectionEnvironment(
            String ldapScheme, String ldapHost, String ldapPort, String baseDn,
            String managerDn, String managerPassword) {

        // Set up environment for creating initial context
        Hashtable<String, String> env = new Hashtable<String, String>();

        String ldapUrl = String.format("%s://%s:%s/%s", ldapScheme, ldapHost,
                ldapPort, baseDn);

        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, ldapUrl);

        // Enable connection pooling
        env.put(CONNECTION_POOL_KEY, "true");

        // Setup the authentication.
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, managerDn);
        env.put(Context.SECURITY_CREDENTIALS, managerPassword);

        return env;
    }

}
