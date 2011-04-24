package eu.stratuslab.registration.resources;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class RegisterResource extends BaseResource {

    @Get("html")
    public Representation toHtml() {

        Map<String, Object> info = createInfoStructure(null);

        return templateRepresentation("register.ftl", info, MediaType.TEXT_HTML);
    }

}
