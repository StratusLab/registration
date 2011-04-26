package eu.stratuslab.registration.resources;

import java.util.Hashtable;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import eu.stratuslab.registration.actions.Action;
import eu.stratuslab.registration.actions.DummyAction;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.RequestUtils;

public class ActionResource extends BaseResource {

    @Get("html")
    public Representation toHtml() {

        Request request = getRequest();

        String uuid = (String) request.getAttributes().get("uuid");

        Hashtable<String, String> ldapEnv = RequestUtils
                .extractLdapEnvironment(getRequest());

        String message = "";

        if (uuid == null) {
            String actionId = UserEntry
                    .createAction(new DummyAction(), ldapEnv);
            message = "action created: " + actionId;
        } else {
            Action action = UserEntry.retrieveAction(uuid, ldapEnv);
            message = action.execute(request);
        }

        Map<String, Object> info = createInfoStructure(null);
        info.put("message", message);
        return templateRepresentation("action.ftl", info, MediaType.TEXT_HTML);
    }

}
