package eu.stratuslab.registration.guards;

import java.util.Hashtable;

import javax.naming.directory.Attributes;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.SecretVerifier;
import org.restlet.security.User;
import org.restlet.security.Verifier;

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.RequestUtils;

public class LdapVerifier implements Verifier {

    public int verify(Request request, Response response) {

        // Make sure the necesssary information is available. If not, this will
        // cause it to be requested.
        if (request.getChallengeResponse() == null) {
            return RESULT_MISSING;
        }

        // Get the identifier and secret from the request.
        String identifier = getIdentifier(request);
        char[] secret = getSecret(request);

        // Blank passwords are not allowed.
        if (secret.length == 0) {
            return RESULT_INVALID;
        }

        if (isLdapPasswordCorrect(identifier, secret, request)) {
            setUser(identifier, request);
            return RESULT_VALID;
        } else {
            return RESULT_INVALID;
        }

    }

    private static String getIdentifier(Request request) {
        return request.getChallengeResponse().getIdentifier();
    }

    private static char[] getSecret(Request request) {
        return request.getChallengeResponse().getSecret();
    }

    private static void setUser(String identifier, Request request) {
        User user = new User(identifier);
        request.getClientInfo().setUser(user);
    }

    private static boolean isLdapPasswordCorrect(String identifier,
            char[] secret, Request request) {

        char[] password = getLdapPassword(identifier, request);
        return SecretVerifier.compare(secret, password);
    }

    private static char[] getLdapPassword(String identifier, Request request) {
        AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);
        Hashtable<String, String> ldapEnv = cfg.getLdapEnv();
        Attributes attrs = UserEntry.getUserAttributes(identifier, ldapEnv);
        return UserEntry.extractPassword(attrs).toCharArray();
    }

}
