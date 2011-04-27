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

import static org.restlet.data.MediaType.APPLICATION_WWW_FORM;

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
                    Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, mediaType
                            .getName());
        }

        return new Form(entity);

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

    public static void addDerivedAttributes(Form form) {

        String surname = form.getFirstValue(UserAttribute.SURNAME.key);
        String givenName = form.getFirstValue(UserAttribute.GIVEN_NAME.key);

        String cn = givenName + " " + surname;

        form.add(COMMON_NAME_KEY, cn);

        form.add(OBJECT_CLASS_KEY, "inetOrgPerson");

    }

}
