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

import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.util.Hashtable;

import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.RequestUtils;

public class UsersResource extends BaseResource {

    private static final String MESSAGE = "user created";

    @Post
    public Representation createUser(Representation entity) {

        Form form = RequestUtils.processWebForm(entity);

        Hashtable<String, String> ldapEnv = RequestUtils
                .extractLdapEnvironment(getRequest());

        UserEntry.createUser(form, ldapEnv);

        Reference redirectRef = getRequest().getResourceRef();
        redirectRef.addQueryParameter("message", MESSAGE);

        Response response = getResponse();
        response.redirectTemporary(redirectRef);

        return new StringRepresentation(MESSAGE, TEXT_PLAIN);

    }

}
