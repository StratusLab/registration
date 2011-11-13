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
package eu.stratuslab.registration.main;

import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.APPLICATION_XHTML;
import static org.restlet.data.MediaType.TEXT_HTML;
import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.util.List;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.StatusService;

import eu.stratuslab.registration.resources.BaseResource;
import eu.stratuslab.registration.utils.RequestUtils;
import freemarker.template.Configuration;

public class CommonStatusService extends StatusService {

    public CommonStatusService() {
    }

    @Override
    public Representation getRepresentation(Status status, Request request,
            Response response) {

        Configuration cfg = RequestUtils.extractFreeMarkerConfig(request);

        ClientInfo clientInfo = request.getClientInfo();
        List<Preference<MediaType>> mediaTypes = clientInfo
                .getAcceptedMediaTypes();

        Map<String, Object> info = getErrorInfo(status, request);

        for (Preference<MediaType> preference : mediaTypes) {

            MediaType desiredMediaType = preference.getMetadata();

            if (TEXT_HTML.isCompatible(desiredMediaType)) {

                return toHtml(cfg, info);

            } else if (APPLICATION_XHTML.isCompatible(desiredMediaType)) {

                return toHtml(cfg, info);

            } else if (TEXT_PLAIN.isCompatible(desiredMediaType)) {

                return toText(cfg, info);

            } else if (APPLICATION_JSON.isCompatible(desiredMediaType)) {

                return toJson(cfg, info);

            }
        }

        return toText(cfg, info);
    }

    private Representation toText(Configuration cfg, Map<String, Object> info) {
        return new TemplateRepresentation("text/error.ftl", cfg, info,
                TEXT_PLAIN);
    }

    private Representation toJson(Configuration cfg, Map<String, Object> info) {
        return new TemplateRepresentation("json/error.ftl", cfg, info,
                APPLICATION_JSON);
    }

    private Representation toHtml(Configuration cfg, Map<String, Object> info) {
        return new TemplateRepresentation("html/error.ftl", cfg, info,
                TEXT_HTML);
    }

    private static Map<String, Object> getErrorInfo(Status status,
            Request request) {

        Map<String, Object> info = BaseResource.createInfoStructure("Error",
                request);

        info.put("errorMsg", status.getDescription());
        info.put("errorCode", status.getCode());

        return info;
    }

}
