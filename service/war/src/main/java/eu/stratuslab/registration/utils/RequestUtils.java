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

import java.util.Map;

import org.restlet.Request;

import eu.stratuslab.registration.cfg.AppConfiguration;
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

    public static LdapConfig extractLdapConfig(Request request) {

        AppConfiguration cfg = extractAppConfiguration(request);
        return cfg.getLdapConfig();
    }

    public static Configuration extractFreeMarkerConfig(Request request) {

        AppConfiguration cfg = extractAppConfiguration(request);
        return cfg.getFreeMarkerConfig();
    }

}
