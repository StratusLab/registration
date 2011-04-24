package eu.stratuslab.registration.resources;

import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.ServerResource;

import eu.stratuslab.registration.utils.RequestUtils;
import freemarker.template.Configuration;

public class BaseResource extends ServerResource {

    protected TemplateRepresentation templateRepresentation(String tpl,
            Map<String, Object> info, MediaType mediaType) {

        Request request = getRequest();

        Configuration freeMarkerConfig = RequestUtils
                .extractFreeMarkerConfig(request);

        return new TemplateRepresentation(tpl, freeMarkerConfig, info,
                mediaType);
    }

    protected Map<String, Object> createInfoStructure(String title) {

        Map<String, Object> info = new HashMap<String, Object>();

        // Add the standard base URL declaration.
        info.put("baseurl", getRequest().getRootRef().toString());

        // Add the title if appropriate.
        if (title != null && !"".equals(title)) {
            info.put("title", title);
        }

        return info;
    }

}
