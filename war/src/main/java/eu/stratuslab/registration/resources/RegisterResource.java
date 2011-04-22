package eu.stratuslab.registration.resources;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class RegisterResource extends BaseResource {

    @Get("html")
    public Representation toHtml() {
        Representation tpl = templateRepresentation("/html/register.ftl");
        return new TemplateRepresentation(tpl, MediaType.TEXT_HTML);
    }

}
