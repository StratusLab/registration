package eu.stratuslab.registration.resources;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.ServerResource;

import eu.stratuslab.registration.main.RegistrationApplication;

public class BaseResource extends ServerResource {

    protected TemplateRepresentation templateRepresentation(String tpl,
            Map<String, Object> info, MediaType mediaType) {

        return new TemplateRepresentation(tpl,
                ((RegistrationApplication) getApplication())
                        .getFreeMarkerConfig(), info, mediaType);

    }

}
