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
package eu.stratuslab.registration.utils;

import static org.restlet.data.MediaType.APPLICATION_WWW_FORM;

import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.data.UserEntry;
import freemarker.template.Configuration;

public final class RequestUtils {

    private static final String APP_CONFIGURATION = "APP_CONFIGURATION";

    private RequestUtils() {

    }

    public static void insertAppConfiguration(Request request,
            AppConfiguration appConfiguration) {

        Map<String, Object> attributes = request.getAttributes();
        attributes.put(APP_CONFIGURATION, appConfiguration);
    }

    public static AppConfiguration extractAppConfiguration(Request request) {

        Map<String, Object> attributes = request.getAttributes();
        return (AppConfiguration) attributes.get(APP_CONFIGURATION);
    }

    public static LdapConfig extractLdapEnvironment(Request request) {

        AppConfiguration cfg = extractAppConfiguration(request);
        return cfg.getLdapConfig();
    }

    public static Configuration extractFreeMarkerConfig(Request request) {

        AppConfiguration cfg = extractAppConfiguration(request);
        return cfg.getFreeMarkerConfig();
    }

    public static Form processWebForm(Representation entity) {

        Form form = validateInputForm(entity);

        Form sanitizedForm = UserEntry.sanitizeForm(form);

        UserEntry.validateEntries(sanitizedForm);
        UserEntry.addDerivedAttributes(sanitizedForm);

        return sanitizedForm;
    }

    public static Form validateInputForm(Representation entity) {

        if (entity == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "null entity is not permitted");
        }

        MediaType mediaType = entity.getMediaType();
        if (!APPLICATION_WWW_FORM.equals(mediaType, true)) {
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, mediaType
                            .getName());
        }

        return new Form(entity);

    }

}
