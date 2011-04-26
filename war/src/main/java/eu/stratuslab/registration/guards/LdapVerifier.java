package eu.stratuslab.registration.guards;

import java.util.Hashtable;

import javax.naming.directory.Attributes;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.SecretVerifier;
import org.restlet.security.User;

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.RequestUtils;

public class LdapVerifier extends SecretVerifier {

    @Override
    public int verify(Request request, Response response) {

        // Get the identifier and secret from the request.
        String identifier = getIdentifier(request, response);
        char[] secret = getSecret(request, response);

        if (secret.length == 0) {
            return RESULT_MISSING;
        }

        // Pull out the current password from LDAP.
        AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);
        Hashtable<String, String> ldapEnv = cfg.getLdapEnv();
        Attributes attrs = UserEntry.getUserAttributes(identifier, ldapEnv);
        char[] password = UserEntry.extractPassword(attrs).toCharArray();

        User user = new User(identifier);
        request.getClientInfo().setUser(user);

        return (compare(secret, password)) ? RESULT_VALID : RESULT_INVALID;
    }

    @Override
    public boolean verify(String identifier, char[] secret) {
        // This method is never used.
        return false;
    }

}
