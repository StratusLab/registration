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

import static org.restlet.data.MediaType.APPLICATION_WWW_FORM;
import static org.restlet.data.MediaType.TEXT_HTML;
import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.data.UserAttribute;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.RequestUtils;

public class UserResource extends BaseResource {

    @Get("txt")
    public Representation toText() {
        return toRepresentation("/text/user.ftl", TEXT_PLAIN);
    }

    @Get("html")
    public Representation toHtml() {
        return toRepresentation("/html/user.ftl", TEXT_HTML);
    }

    private Representation toRepresentation(String templateName,
            MediaType mediaType) {

        Request request = getRequest();
        String uid = (String) request.getAttributes().get("uid");
        Representation tpl = templateRepresentation(templateName);

        Hashtable<String, String> ldapEnv = RequestUtils
                .extractLdapEnvironment(request);

        Map<String, Object> infoTree = new HashMap<String, Object>();
        infoTree.put("properties", UserEntry.getUserProperties(uid, ldapEnv));

        return new TemplateRepresentation(tpl, infoTree, mediaType);
    }

    @Delete
    public void removeUser() {

        // TODO: Actually remove the user from the database.
    }

    @Put
    public Representation updateUser(Representation entity) {

        if (entity == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "put with null entity");
        }

        MediaType mediaType = entity.getMediaType();
        if (!APPLICATION_WWW_FORM.equals(mediaType, true)) {
            System.err.println("DEBUG DEBUG DEBUG: " + mediaType.getName());
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, mediaType
                            .getName());
        }

        Form form = RequestUtils.processWebForm(entity);

        Hashtable<String, String> ldapEnv = RequestUtils
                .extractLdapEnvironment(getRequest());

        UserEntry.updateUser(form, ldapEnv);

        setStatus(Status.SUCCESS_OK);

        String userid = form.getFirstValue(UserAttribute.UID.key);

        Representation rep = new StringRepresentation("user updated",
                TEXT_PLAIN);

        String diskRelativeUrl = "/users/" + userid;
        rep.setLocationRef(getRequest().getResourceRef().getIdentifier()
                + diskRelativeUrl);

        return rep;

    }

}
