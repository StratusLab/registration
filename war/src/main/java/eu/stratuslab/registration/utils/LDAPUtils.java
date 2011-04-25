package eu.stratuslab.registration.utils;

import java.util.Hashtable;

import javax.naming.Context;

public final class LDAPUtils {

    private static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    private static final String LDAP_URL = "ldap://localhost:10389/ou=users,ou=system";

    private static final String CONNECTION_POOL_KEY = "com.sun.jndi.ldap.connect.pool";

    private LDAPUtils() {

    }

    public static Hashtable<String, String> createLdapConnectionEnvironment() {

        // Set up environment for creating initial context
        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, LDAP_URL);

        // Enable connection pooling
        env.put(CONNECTION_POOL_KEY, "true");

        // Setup the authentication.
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "uid=admin,ou=system");
        env.put(Context.SECURITY_CREDENTIALS, "secret");

        return env;
    }

}
