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
package eu.stratuslab.registration.actions;

import java.util.logging.Logger;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.restlet.Request;

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.cfg.Parameter;
import eu.stratuslab.registration.data.GroupEntry;
import eu.stratuslab.registration.data.UserAttribute;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.LdapConfig;
import eu.stratuslab.registration.utils.Notifier;
import eu.stratuslab.registration.utils.RequestUtils;

@SuppressWarnings("serial")
public class ValidateAccount implements Action {

    private static final Logger LOGGER = Logger.getLogger("org.restlet");

    private static final String ACCOUNT_DENIED_MESSAGE_ADMIN = //
    "The user account %s has NOT been approved.\n" + //
            "An email to this effect has been sent to the user.\n\n";

    private static final String ACCOUNT_DENIED_MESSAGE_USER = //
    "The administrator has NOT approved your account.\n" + //
            "Contact the administrator at %s for more information.\n\n";

    private static final String ACCOUNT_APPROVED_MESSAGE_ADMIN = //
    "The user account %s has been approved.\n" + //
            "An email to this effect has been sent to the user.\n\n";

    private static final String ACCOUNT_APPROVED_MESSAGE_USER = //
    "The administrator has approved your account.\n" + //
            "You may now start using the cloud infrastructure.\n\n";

    private static final String EMAIL_SEND_ERROR = //
    "An error occurred when trying to send email: %s.\n";

    private final String identifier;

    private final String userEmail;

    private final String adminEmail;

    public ValidateAccount(String identifier, String userEmail,
            String adminEmail) {
        this.identifier = identifier;
        this.userEmail = userEmail;
        this.adminEmail = adminEmail;
    }

    public String abort(Request request) {

        AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);

        StringBuilder adminMessage = new StringBuilder(String.format(
                ACCOUNT_DENIED_MESSAGE_ADMIN, identifier));

        String message = String.format(ACCOUNT_DENIED_MESSAGE_USER, adminEmail);
        try {
            Notifier.sendNotification(userEmail, message, cfg);
        } catch (Exception e) {
            String msg = String.format(EMAIL_SEND_ERROR, e.getMessage());
            LOGGER.severe(msg);
            adminMessage.append(msg);
        }

        return adminMessage.toString();
    }

    public String execute(Request request) {

        LdapConfig ldapEnvUser = RequestUtils.extractLdapConfig(request,
                Parameter.LDAP_USER_BASE_DN);

        Attributes userAttrs = UserEntry.getUserAttributes(identifier,
                ldapEnvUser);

        Attribute userDn = userAttrs.get(UserAttribute.DN.key);

        LdapConfig ldapEnvGroup = RequestUtils.extractLdapConfig(request,
                Parameter.LDAP_GROUP_BASE_DN);

        GroupEntry.addUserToGroup("cloud-access", userDn, ldapEnvGroup);

        AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);

        StringBuilder adminMessage = new StringBuilder(String.format(
                ACCOUNT_APPROVED_MESSAGE_ADMIN, identifier));

        String message = String.format(ACCOUNT_APPROVED_MESSAGE_USER);

        try {
            Notifier.sendNotification(userEmail, message, cfg);
        } catch (Exception e) {
            String msg = String.format(EMAIL_SEND_ERROR, e.getMessage());
            LOGGER.severe(msg);
            adminMessage.append(msg);
        }

        return adminMessage.toString();
    }

}
