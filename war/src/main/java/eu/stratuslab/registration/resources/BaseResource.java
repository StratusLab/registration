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

import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.ServerResource;

import eu.stratuslab.registration.utils.RequestUtils;
import freemarker.template.Configuration;

public class BaseResource extends ServerResource {

    protected static final String NO_TITLE = null;

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
