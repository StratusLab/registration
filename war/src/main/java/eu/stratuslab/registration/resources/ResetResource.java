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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.actions.Action;
import eu.stratuslab.registration.actions.ResetPassword;
import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.data.UserAttribute;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.FormUtils;
import eu.stratuslab.registration.utils.LdapConfig;
import eu.stratuslab.registration.utils.Notifier;
import eu.stratuslab.registration.utils.RequestUtils;

public class ResetResource extends BaseResource {

    private static final String MESSAGE = "password reset message sent";

    private static final String EMAIL_MESSAGE_TEMPLATE = //
    "Visit this URL to reset your password: \n\n%1$s\n\n" + //
            "To cancel the request, follow this link: \n\n%1$s?abort=true\n\n";

    @Get("html")
    public Representation toHtml() {
        return toRepresentation("reset.ftl", TEXT_HTML);
    }

    private Representation toRepresentation(String templateName,
            MediaType mediaType) {

        Map<String, Object> info = createInfoStructure(NO_TITLE);
        return templateRepresentation(templateName, info, mediaType);
    }

    @Post
    public Representation resetPassword(Representation entity) {

        Request request = getRequest();

        Form form = FormUtils.processWebForm(entity);

        LdapConfig ldapEnv = RequestUtils.extractLdapConfig(request);

        String formUserid = form.getFirstValue(UserAttribute.UID.key);

        String userEmail = UserEntry.getEmailAddress(formUserid, ldapEnv);

        Action action = new ResetPassword(formUserid, userEmail);

        String actionId = UserEntry.storeAction(action, ldapEnv);

        AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);

        String msg = getResetMessage(request.getRootRef().toString(), actionId);

        try {
            Notifier.sendNotification(userEmail, msg, cfg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "cannot send reset email to user");
        }

        Reference redirectRef = getRequest().getResourceRef();
        redirectRef.addQueryParameter("message", MESSAGE);

        Response response = getResponse();
        response.redirectSeeOther(redirectRef);

        return new StringRepresentation(MESSAGE, TEXT_PLAIN);

    }

    private static String getResetMessage(String rootRef, String actionId) {
        StringBuilder url = new StringBuilder(rootRef);
        if (!rootRef.endsWith("/")) {
            url.append("/");
        }
        url.append("action/");
        url.append(actionId);
        return String.format(EMAIL_MESSAGE_TEMPLATE, url.toString());
    }
}
