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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Post;

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.cfg.Parameter;
import eu.stratuslab.registration.data.UserAttribute;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.FormUtils;
import eu.stratuslab.registration.utils.LdapConfig;
import eu.stratuslab.registration.utils.Notifier;
import eu.stratuslab.registration.utils.RequestUtils;

public class UsersResource extends BaseResource {

    private static final String MESSAGE = "account created";

    private static final String ADMIN_NOTIFICATION_MESSAGE = //
    "A new user has registered (%s, %s).\n\n%s\n\n" + //
            "Please review the users' information.\n" + //
            "Send an email to the user when the account has been activated.\n";

    private static final String NO_USER_MESSAGE = //
    "User did not provide a message.";

    @Post
    public Representation createUser(Representation entity) {

        Request request = getRequest();

        Form form = FormUtils.processWebForm(entity);

        String userEmail = form.getFirstValue(UserAttribute.EMAIL.key);
        String userMsg = form.getFirstValue(UserAttribute.MESSAGE.key);
        if (userMsg == null) {
            userMsg = NO_USER_MESSAGE;
        }

        AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);
        LdapConfig ldapConfig = cfg.getLdapConfig(Parameter.LDAP_USER_BASE_DN);

        String userId = UserEntry.createUser(form, ldapConfig);

        Reference redirectRef = getRequest().getRootRef();
        redirectRef.addSegment("success");

        Response response = getResponse();
        response.redirectSeeOther(redirectRef);

        String message = String.format(ADMIN_NOTIFICATION_MESSAGE, userId,
                userEmail, userMsg);

        Notifier.sendAdminNotification(message, cfg);

        return new StringRepresentation(MESSAGE, TEXT_PLAIN);

    }

}
