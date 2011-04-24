package eu.stratuslab.registration.utils;

import static org.restlet.data.MediaType.APPLICATION_WWW_FORM;

import java.util.Hashtable;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.data.UserEntry;
import freemarker.template.Configuration;

public class RequestUtils {

    private static final String LDAP_JNDI_ENV_KEY = "LDAP_JNDI_ENV";

    private static final String FREE_MARKER_CONFIG = "FREE_MARKER_CONFIG";

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

    public static void insertFreeMarkerConfig(Request request,
            Configuration config) {

        Map<String, Object> attributes = request.getAttributes();

        attributes.put(FREE_MARKER_CONFIG, config);
        request.setAttributes(attributes);
    }

    public static Configuration extractFreeMarkerConfig(Request request) {

        Map<String, Object> attributes = request.getAttributes();

        return (Configuration) attributes.get(FREE_MARKER_CONFIG);
    }

    public static Form processWebForm(Representation entity) {

        checkForInvalidForm(entity);

        Form form = new Form(entity);
        Form sanitizedForm = UserEntry.sanitizeForm(form);

        UserEntry.checkCompleteForm(sanitizedForm);
        UserEntry.validateEntries(sanitizedForm);
        UserEntry.addDerivedAttributes(sanitizedForm);

        return sanitizedForm;
    }

    public static void checkForInvalidForm(Representation entity) {

        if (entity == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "null entity is not permitted");
        }

        MediaType mediaType = entity.getMediaType();
        if (!APPLICATION_WWW_FORM.equals(mediaType, true)) {
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, mediaType
                            .getName());
        }

    }

}
