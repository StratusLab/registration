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

import java.util.Hashtable;
import java.util.Map;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.data.UserAttribute;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.RequestUtils;

public class UsersResource extends BaseResource {

    @Post
    public Representation createUser(Representation entity) {

        if (entity == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "post with null entity");
        }

        MediaType mediaType = entity.getMediaType();
        if (!APPLICATION_WWW_FORM.equals(mediaType, true)) {
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, mediaType
                            .getName());
        }

        Form form = RequestUtils.processWebForm(entity);

        Hashtable<String, String> ldapEnv = RequestUtils
                .extractLdapEnvironment(getRequest());

        UserEntry.createUser(form, ldapEnv);

        setStatus(Status.SUCCESS_CREATED);

        String userid = form.getFirstValue(UserAttribute.UID.key);

        Representation rep = new StringRepresentation("user created",
                TEXT_PLAIN);

        String diskRelativeUrl = "/users/" + userid;
        rep.setLocationRef(getRequest().getResourceRef().getIdentifier()
                + diskRelativeUrl);

        return rep;

    }

    @Get("txt")
    public Representation toText() {
        return toRepresentation("/text/users.ftl", TEXT_PLAIN);
    }

    @Get("html")
    public Representation toHtml() {
        return toRepresentation("/html/users.ftl", TEXT_HTML);
    }

    private Representation toRepresentation(String template, MediaType mediaType) {

        Hashtable<String, String> ldapEnv = RequestUtils
                .extractLdapEnvironment(getRequest());

        Map<String, Object> userInfo = UserEntry.listUsers(ldapEnv);

        return templateRepresentation(template, userInfo, mediaType);
    }

}
