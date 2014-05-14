/*
 Created as part of the StratusLab project (http://stratuslab.eu),
 co-funded by the European Commission under the Grant Agreement
 INFSO-RI-261552.

 Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package eu.stratuslab.registration.resources;

import eu.stratuslab.registration.utils.RequestUtils;
import freemarker.template.Configuration;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.ServerResource;

import java.util.HashMap;
import java.util.Map;

public class BaseResource extends ServerResource {

    protected static final String NO_TITLE = null;

    protected TemplateRepresentation templateRepresentation(String tpl, Map<String, Object> info, MediaType mediaType) {

        Request request = getRequest();

        Configuration freeMarkerConfig = RequestUtils.extractFreeMarkerConfig(request);

        return new TemplateRepresentation(tpl, freeMarkerConfig, info, mediaType);
    }

    public static String getBaseUrl(Request request) {

        Form headers = (Form) request.getAttributes().get("org.restlet.http.headers");
        String scheme = headers.getFirstValue("X-Forwarded-Scheme");
        Integer port = null;
        try {
            String sport = headers.getFirstValue("X-Forwarded-Port");
            if (sport != null) {
                port = Integer.parseInt(sport);
            }
        } catch (NumberFormatException consumed) {
            port = null;
        }

        Reference ref = request.getRootRef();
        if (port != null) {
            ref.setHostPort(port);
        }
        if (scheme != null) {
            ref.setScheme(scheme);
        }

        String url = ref.toString();
        if (url.endsWith("/")) {
            return url;
        } else {
            return url + "/";
        }
    }

    protected Map<String, Object> createInfoStructure(String title) {
        return createInfoStructure(title, getRequest());
    }

    public static Map<String, Object> createInfoStructure(String title, Request request) {

        Map<String, Object> info = new HashMap<String, Object>();

        // Add the standard base URL declaration.
        info.put("baseurl", getBaseUrl(request));

        // Add the title if appropriate.
        if (title != null && !"".equals(title)) {
            info.put("title", title);
        }

        return info;
    }
}
