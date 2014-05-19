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

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.cfg.Parameter;
import freemarker.template.Configuration;
import org.restlet.Request;
import org.restlet.data.Reference;
import org.restlet.util.Series;

import java.util.Map;

public final class RequestUtils {

    private static final String APP_CONFIGURATION = "APP_CONFIGURATION";

    private RequestUtils() {

    }

    public static void insertAppConfiguration(Request request, AppConfiguration appConfiguration) {

        Map<String, Object> attributes = request.getAttributes();
        attributes.put(APP_CONFIGURATION, appConfiguration);
    }

    public static AppConfiguration extractAppConfiguration(Request request) {

        Map<String, Object> attributes = request.getAttributes();
        return (AppConfiguration) attributes.get(APP_CONFIGURATION);
    }

    public static LdapConfig extractLdapConfig(Request request, Parameter baseDnParameter) {

        AppConfiguration cfg = extractAppConfiguration(request);
        return cfg.getLdapConfig(baseDnParameter);
    }

    public static Configuration extractFreeMarkerConfig(Request request) {

        AppConfiguration cfg = extractAppConfiguration(request);
        return cfg.getFreeMarkerConfig();
    }

    // always has a trailing slash!
    public static String getBaseUrl(Request request) {

        Series headers = (Series) request.getAttributes().get("org.restlet.http.headers");

        String scheme = null;
        String authority = null;
        if (headers != null) {
            scheme = headers.getFirstValue("X-Forwarded-Scheme");
            authority = headers.getFirstValue("Host");
        }

        Reference ref = request.getRootRef();
        if (authority != null) {
            ref.setAuthority(authority);
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

}
