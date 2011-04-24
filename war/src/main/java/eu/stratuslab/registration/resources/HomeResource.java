package eu.stratuslab.registration.resources;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class HomeResource extends BaseResource {

    @Get("html")
    public Representation toHtml() {

        Map<String, Object> info = createInfoStructure(null);

        return templateRepresentation("home.ftl", info, MediaType.TEXT_HTML);
    }

}
