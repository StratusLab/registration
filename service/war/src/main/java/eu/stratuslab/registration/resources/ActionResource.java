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

import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import eu.stratuslab.registration.actions.Action;
import eu.stratuslab.registration.data.ActionEntry;
import eu.stratuslab.registration.utils.LdapConfig;
import eu.stratuslab.registration.utils.RequestUtils;

public class ActionResource extends BaseResource {

    @Get("html")
    public Representation toHtml() {

        Request request = getRequest();

        String uuid = (String) request.getAttributes().get("uuid");

        LdapConfig ldapEnv = RequestUtils.extractLdapConfig(request);

        Action action = ActionEntry.retrieveAction(uuid, ldapEnv);

        boolean abort = abortAction(request.getResourceRef());
        String msg = (abort) ? action.abort(request) : action.execute(request);

        Map<String, Object> info = createInfoStructure(NO_TITLE);
        info.put("message", msg);
        return templateRepresentation("action.ftl", info, MediaType.TEXT_HTML);
    }

    private static boolean abortAction(Reference ref) {
        if (ref.hasQuery()) {
            Form form = ref.getQueryAsForm();
            return (form.getFirstValue("abort") != null);
        }
        return false;
    }

}
