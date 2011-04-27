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

import static org.restlet.data.MediaType.TEXT_HTML;
import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.util.Map;

import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.data.UserAttribute;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.FormUtils;
import eu.stratuslab.registration.utils.LdapConfig;
import eu.stratuslab.registration.utils.RequestUtils;

public class ProfileResource extends BaseResource {

    private static final String MESSAGE = "profile updated";

    @Get("html")
    public Representation toHtml() {
        return toRepresentation("profile.ftl", TEXT_HTML);
    }

    private Representation toRepresentation(String templateName,
            MediaType mediaType) {

        String uid = getBasicUsername();

        LdapConfig ldapEnv = RequestUtils.extractLdapConfig(getRequest());

        Map<String, Object> info = createInfoStructure(NO_TITLE);
        info.put("properties", UserEntry.getUserProperties(uid, ldapEnv));

        return templateRepresentation(templateName, info, mediaType);
    }

    @Put
    public Representation updateUser(Representation entity) {

        Form form = FormUtils.processWebForm(entity);

        LdapConfig ldapEnv = RequestUtils.extractLdapConfig(getRequest());

        String userid = getBasicUsername();
        String formUserid = form.getFirstValue(UserAttribute.UID.key);

        if (!userid.equals(formUserid)) {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "cannot update another user's profile");
        }

        UserEntry.updateUser(form, ldapEnv);

        Reference redirectRef = getRequest().getResourceRef();
        redirectRef.addQueryParameter("message", MESSAGE);

        Response response = getResponse();
        response.redirectSeeOther(redirectRef);

        return new StringRepresentation(MESSAGE, TEXT_PLAIN);

    }

    private String getBasicUsername() {
        return getRequest().getClientInfo().getUser().getIdentifier();
    }

}
