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

import static eu.stratuslab.registration.data.UserAttribute.GIVEN_NAME;
import static eu.stratuslab.registration.data.UserAttribute.NEW_PASSWORD;
import static eu.stratuslab.registration.data.UserAttribute.NEW_PASSWORD_CHECK;
import static eu.stratuslab.registration.data.UserAttribute.PASSWORD;
import static eu.stratuslab.registration.data.UserAttribute.SURNAME;
import static eu.stratuslab.registration.data.UserAttribute.X500_DN;
import static org.restlet.data.MediaType.APPLICATION_WWW_FORM;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.data.UserAttribute;

public final class FormUtils {

    public static final String COMMON_NAME_KEY = "cn";

    public static final String OBJECT_CLASS_KEY = "objectClass";

    private FormUtils() {

    }

    public static Form processWebForm(Representation entity) {

        Form form = validateInputForm(entity);

        Form sanitizedForm = sanitizeForm(form);

        validateEntries(sanitizedForm);
        addDerivedAttributes(sanitizedForm);

        return sanitizedForm;
    }

    public static Form validateInputForm(Representation entity) {

        if (entity == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "null entity is not permitted");
        }

        MediaType mediaType = entity.getMediaType();
        if (!APPLICATION_WWW_FORM.equals(mediaType, true)) {
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
                    mediaType.getName());
        }

        return new Form(entity);

    }

    public static void validateEntries(Form form) {

        for (Parameter parameter : form) {
            String key = parameter.getName();
            String value = parameter.getValue();

            UserAttribute attr = UserAttribute.valueWithKey(key);

            if (!attr.isValid(value)) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "invalid " + attr.name);
            }
        }

    }

    // Removes unknown keys from form and parameters with empty values.
    // This method does NOT validate the parameters.
    public static Form sanitizeForm(Form form) {

        Form sanitizedForm = new Form();

        for (Parameter parameter : form) {
            String key = parameter.getName();
            try {
                UserAttribute.valueWithKey(key);
                String value = parameter.getValue();
                if (UserAttribute.isNotWhitespace(value)) {
                    sanitizedForm.add(parameter);
                }
            } catch (IllegalArgumentException consumed) {
                // Do not copy the parameter into the new form.
            }
        }

        return sanitizedForm;
    }

    public static void allCreateAttributesExist(Form form) {
        for (UserAttribute attr : UserAttribute.values()) {
            if (attr.isRequiredForCreate) {
                if (form.getFirstValue(attr.key) == null) {
                    throw new ResourceException(
                            Status.CLIENT_ERROR_BAD_REQUEST,
                            attr.missingErrorMessage);
                }
            }
        }
    }

    public static void allUpdateAttributesExist(Form form) {
        for (UserAttribute attr : UserAttribute.values()) {
            if (attr.isRequiredForUpdate) {
                if (form.getFirstValue(attr.key) == null) {
                    throw new ResourceException(
                            Status.CLIENT_ERROR_BAD_REQUEST,
                            attr.missingErrorMessage);
                }
            }
        }
    }

    public static void removeUnmodifiableAttributes(Form form) {
        for (UserAttribute attr : UserAttribute.values()) {
            if (!attr.isModifiable) {
                form.removeAll(attr.key);
            }
        }
    }

    public static void stripNonLdapAttributes(Form form) {
        form.removeAll(NEW_PASSWORD.key);
        form.removeAll(NEW_PASSWORD_CHECK.key);
        form.removeAll(UserAttribute.AGREEMENT.key);
        form.removeAll(UserAttribute.MESSAGE.key);
    }

    public static void addDerivedAttributes(Form form) {

        String surname = form.getFirstValue(SURNAME.key);
        String givenName = form.getFirstValue(GIVEN_NAME.key);

        String cn = givenName + " " + surname;

        form.add(COMMON_NAME_KEY, cn);

        form.add(OBJECT_CLASS_KEY, "inetOrgPerson");

    }

    public static void removeIdenticalAttributes(Form currentForm,
            Form updatedForm) {

        Form intersection = new Form();
        intersection.addAll(updatedForm);
        intersection.retainAll(currentForm);

        currentForm.removeAll(intersection);
        updatedForm.removeAll(intersection);

    }

    public static Form removeAllNamedParameters(Form base,
            Form unwantedParameters) {

        Form form = new Form();
        form.addAll(base);
        for (String name : unwantedParameters.getNames()) {
            form.removeAll(name);
        }
        return form;
    }

    public static Form retainAllNamedParameters(Form base,
            Form retainedParameters) {

        Form form = new Form();
        form.addAll(base);
        for (String name : form.getNames()) {
            if (retainedParameters.getFirstValue(name) == null) {
                form.removeAll(name);
            }
        }
        return form;
    }

    public static String checkNewPasswords(Form form) {

        // Get the new password values.
        String pswd1 = form.getFirstValue(NEW_PASSWORD.key);
        String pswd2 = form.getFirstValue(NEW_PASSWORD_CHECK.key);

        // Check for consistency, keeping in mind that this is optional.
        if (pswd1 != null || pswd2 != null) {
            if (pswd1 == null || !pswd1.equals(pswd2)) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "mismatched passwords");
            }
        }

        // Remove the new password fields from the form.
        form.removeAll(NEW_PASSWORD.key);
        form.removeAll(NEW_PASSWORD_CHECK.key);

        return pswd1;
    }

    public static void canonicalizeCertificateDN(Form form) {

        // If certificate DN is just white space, then remove the attribute.
        String dn = form.getFirstValue(X500_DN.key);
        form.removeAll(X500_DN.key);
        if (dn != null && UserAttribute.isNotWhitespace(dn)) {
            try {
                LdapName name = new LdapName(dn);
                String canonicalizedDN = name.toString();
                form.add(X500_DN.key, canonicalizedDN);
            } catch (InvalidNameException consumed) {
                // Should have previously been detected. Just ignore here.
            }
        }
    }

    public static String checkCurrentPassword(Form currentForm, Form updateForm) {

        String currentPassword = currentForm.getFirstValue(PASSWORD.key);

        String updatePassword = updateForm.getFirstValue(PASSWORD.key);

        if (currentPassword == null
                || !HashUtils.comparePassword(updatePassword, currentPassword)) {
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED,
                    "incorrect password");
        }

        return currentPassword;
    }

    public static void copyNameAttributes(Form currentForm, Form updateForm) {

        String currentSurname = currentForm.getFirstValue(SURNAME.key);
        if (updateForm.getFirstValue(SURNAME.key) == null) {
            updateForm.add(SURNAME.key, currentSurname);
        }

        String currentGivenName = currentForm.getFirstValue(GIVEN_NAME.key);
        if (updateForm.getFirstValue(GIVEN_NAME.key) == null) {
            updateForm.add(GIVEN_NAME.key, currentGivenName);
        }

    }

    public static void setNewPasswordInForm(String currentPassword,
            String newPassword, Form form) {

        form.removeAll(PASSWORD.key);

        if (newPassword != null) {
            String hashedNewPassword = HashUtils.sshaHash(newPassword);
            form.set(PASSWORD.key, hashedNewPassword);
        } else if (currentPassword != null) {
            form.set(PASSWORD.key, currentPassword);
        }

    }

}
