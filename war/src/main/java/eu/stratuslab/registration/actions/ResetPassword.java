package eu.stratuslab.registration.actions;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Hashtable;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.data.UserAttribute;
import eu.stratuslab.registration.data.UserEntry;
import eu.stratuslab.registration.utils.Notifier;
import eu.stratuslab.registration.utils.RequestUtils;

@SuppressWarnings("serial")
public class ResetPassword implements Action {

    private String identifier;

    private String email;

    public ResetPassword(String identifier, String email) {
        this.identifier = identifier;
        this.email = email;
    }

    public String abort(Request request) {
        return "The request to update your password has been aborted.\n"
                + "You password has NOT been changed.\n";
    }

    public String execute(Request request) {

        String newPassword = randomPassword();

        Form form = new Form();
        form.add(UserAttribute.UID.key, identifier);
        form.add(UserAttribute.PASSWORD.key, newPassword);

        Hashtable<String, String> ldapEnv = RequestUtils
                .extractLdapEnvironment(request);

        UserEntry.rawUpdateUser(form, ldapEnv);

        AppConfiguration cfg = RequestUtils.extractAppConfiguration(request);

        String message = String.format("Your new password is '%s'.",
                newPassword);

        try {
            Notifier.sendNotification(email, message, cfg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "error sending email");
        }

        return String.format("An email with your new password has been sent.");
    }

    private static String randomPassword() {
        SecureRandom randomSource = new SecureRandom();
        BigInteger value = new BigInteger(60, randomSource);
        return value.toString(Character.MAX_RADIX);
    }

}
