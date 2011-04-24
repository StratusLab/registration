package eu.stratuslab.registration.resources;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class LoginResource extends BaseResource {

    @Get("html")
    public Representation toHtml() {

        Map<String, Object> info = new HashMap<String, Object>();
        info.put("baseurl", getRequest().getRootRef().toString());

        return templateRepresentation("/html/login.ftl", info,
                MediaType.TEXT_HTML);
    }

}
