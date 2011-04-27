package eu.stratuslab.registration.resources;

import java.util.Hashtable;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import eu.stratuslab.registration.actions.Action;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.RequestUtils;

public class ActionResource extends BaseResource {

    @Get("html")
    public Representation toHtml() {

        Request request = getRequest();

        String uuid = (String) request.getAttributes().get("uuid");

        Hashtable<String, String> ldapEnv = RequestUtils
                .extractLdapEnvironment(request);

        Action action = UserEntry.retrieveAction(uuid, ldapEnv);

        boolean abort = abortAction(request.getResourceRef());
        String msg = (abort) ? action.abort(request) : action.execute(request);

        Map<String, Object> info = createInfoStructure(NO_TITLE);
        info.put("message", msg);
        return templateRepresentation("action.ftl", info, MediaType.TEXT_HTML);
    }

    private static boolean abortAction(Reference ref) {
        if (ref.hasQuery()) {
            Form form = ref.getQueryAsForm();
            return (form.getFirstValue("abort") != null);
        }
        return false;
    }

}
