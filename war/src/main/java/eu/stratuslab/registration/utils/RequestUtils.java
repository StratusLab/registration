package eu.stratuslab.registration.utils;

import java.util.Hashtable;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.representation.Representation;

import eu.stratuslab.registration.data.UserEntry;

public class RequestUtils {

    private static final String LDAP_JNDI_ENV_KEY = "LDAP_JNDI_ENV";

    private RequestUtils() {

    }

    public static void insertLdapEnvironment(Request request,
            Hashtable<String, String> env) {

        Map<String, Object> attributes = request.getAttributes();

        attributes.put(LDAP_JNDI_ENV_KEY, env);
        request.setAttributes(attributes);
    }

    @SuppressWarnings("unchecked")
    public static Hashtable<String, String> extractLdapEnvironment(
            Request request) {

        Map<String, Object> attributes = request.getAttributes();

        return (Hashtable<String, String>) attributes.get(LDAP_JNDI_ENV_KEY);
    }

    public static Form processWebForm(Representation entity) {

        Form form = new Form(entity);
        Form sanitizedForm = UserEntry.sanitizeForm(form);

        UserEntry.checkCompleteForm(sanitizedForm);
        UserEntry.validateEntries(sanitizedForm);
        UserEntry.addDerivedAttributes(sanitizedForm);

        return sanitizedForm;
    }

}
