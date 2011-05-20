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

import static eu.stratuslab.registration.data.UserAttribute.PASSWORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.data.UserAttribute;

public class FormUtilsTest {

    @Test(expected = ResourceException.class)
    public void nullEntityThrowsException() {
        FormUtils.validateInputForm(null);
    }

    @Test(expected = ResourceException.class)
    public void wrongMediaTypeThrowsException() {
        Representation entity = new StringRepresentation("",
                MediaType.TEXT_PLAIN);
        FormUtils.validateInputForm(entity);
    }

    @Test
    public void checkIdenticalFormIsProduced() {

        Form form = new Form();
        form.add("key", "value");
        Form recoveredForm = FormUtils.validateInputForm(form
                .getWebRepresentation());
        assertEquals(form, recoveredForm);
    }

    @Test
    public void checkKnownAttrsAreNotRemoved() {

        Form form = new Form();

        for (UserAttribute attr : UserAttribute.values()) {
            form.add(attr.key, "value");
        }

        form = FormUtils.sanitizeForm(form);

        for (UserAttribute attr : UserAttribute.values()) {
            "value".equals(form.getFirstValue(attr.key));
        }

    }

    @Test
    public void checkEmptyAttrsAreRemoved() {

        Form form = new Form();

        // Values with just whitespace should be removed.
        for (UserAttribute attr : UserAttribute.values()) {
            form.add(attr.key, "\t \f");
        }

        form = FormUtils.sanitizeForm(form);

        for (UserAttribute attr : UserAttribute.values()) {
            if (form.getFirstValue(attr.key) != null) {
                fail(attr.key + " exists but should have been removed");
            }
        }

    }

    @Test
    public void checkUnknownAttrsAreRemoved() {

        Form form = new Form();

        // Values with just whitespace should be removed.
        for (UserAttribute attr : UserAttribute.values()) {
            form.add(attr.key + "-unknown", "value");
        }

        form = FormUtils.sanitizeForm(form);

        // Will throw an exception if unknown key is found.
        for (String key : form.getNames()) {
            UserAttribute.valueWithKey(key);
        }

    }

    @Test(expected = ResourceException.class)
    public void checkInvalidKey() {

        Form form = new Form();

        form.add(UserAttribute.UID.key, "bad username");

        FormUtils.validateEntries(form);

    }

    @Test(expected = IllegalArgumentException.class)
    public void checkUnknownKey() {

        Form form = new Form();

        form.add("badkey", "bad value");

        FormUtils.validateEntries(form);

    }

    @Test
    public void unmodifiableAttributesRemoved() {
        Form form = new Form();
        for (UserAttribute attr : UserAttribute.values()) {
            form.add(attr.key, "dummy");
        }

        FormUtils.removeUnmodifiableAttributes(form);

        for (String name : form.getNames()) {
            UserAttribute attr = UserAttribute.valueWithKey(name);
            if (!attr.isModifiable) {
                fail("unmodifiable entry not removed: " + attr.key);
            }
        }

    }

    @Test
    public void knownAttributesNotRemoved() {
        Form form = new Form();
        for (UserAttribute attr : UserAttribute.values()) {
            form.add(attr.key, "dummy");
        }

        Form cleanedForm = FormUtils.sanitizeForm(form);

        assertEquals(form.size(), cleanedForm.size());
    }

    @Test
    public void unknownAttributesRemoved() {
        Form form = new Form();

        form.add("XXX-UNKNOWN-ATTR-XXX", "dummy");

        Form cleanedForm = FormUtils.sanitizeForm(form);

        assertEquals(0, cleanedForm.size());
    }

    @Test
    public void checkFormKeepsParametersWithDifferentValues() {
        Form currentForm = new Form();
        Form updateForm = new Form();

        String key = "key";

        currentForm.add(new Parameter(key, "value1"));
        updateForm.add(new Parameter(key, "value2"));

        updateForm.removeAll(currentForm);

        assertEquals(1, updateForm.size());
    }

    @Test
    public void checkFormRemovesIdenticalParameters() {
        Form currentForm = new Form();
        Form updateForm = new Form();

        String key = "key";

        currentForm.add(new Parameter(key, "value1"));
        updateForm.add(new Parameter(key, "value1"));

        updateForm.removeAll(currentForm);

        assertEquals(0, updateForm.size());
    }

    @Test
    public void checkNamedValuesAreRemoved() {

        Form baseForm = new Form();
        baseForm.add(new Parameter("a", "1a"));
        baseForm.add(new Parameter("a", "1b"));
        baseForm.add(new Parameter("b", "2a"));
        baseForm.add(new Parameter("b", "2b"));

        Form unwantedParametersForm = new Form();
        unwantedParametersForm.add(new Parameter("b", "3a"));
        unwantedParametersForm.add(new Parameter("b", "3b"));

        Form cleanedForm = FormUtils.removeAllNamedParameters(baseForm,
                unwantedParametersForm);

        assertNull(cleanedForm.getFirstValue("b"));
        assertNotNull(cleanedForm.getFirstValue("a"));
        assertEquals(2, cleanedForm.getValuesArray("a").length);
    }

    @Test
    public void checkNamedValuesAreRetained() {

        Form baseForm = new Form();
        baseForm.add(new Parameter("a", "1a"));
        baseForm.add(new Parameter("a", "1b"));
        baseForm.add(new Parameter("b", "2a"));
        baseForm.add(new Parameter("b", "2b"));

        Form retainedParametersForm = new Form();
        retainedParametersForm.add(new Parameter("b", "3a"));
        retainedParametersForm.add(new Parameter("b", "3b"));
        retainedParametersForm.add(new Parameter("c", "4a"));
        retainedParametersForm.add(new Parameter("c", "4b"));

        Form cleanedForm = FormUtils.retainAllNamedParameters(baseForm,
                retainedParametersForm);

        assertNull(cleanedForm.getFirstValue("a"));
        assertNull(cleanedForm.getFirstValue("c"));
        assertNotNull(cleanedForm.getFirstValue("b"));
        assertEquals(2, cleanedForm.getValuesArray("b").length);
    }

    @Test
    public void checkIntersectionIsRemoved() {

        Form form1 = new Form();
        form1.add(new Parameter("a", "1a"));
        form1.add(new Parameter("a", "1b"));
        form1.add(new Parameter("b", "2a"));
        form1.add(new Parameter("b", "2b"));
        form1.add(new Parameter("b", "2c"));

        Form form2 = new Form();
        form2.add(new Parameter("b", "2a"));
        form2.add(new Parameter("b", "2b"));
        form2.add(new Parameter("c", "3a"));
        form2.add(new Parameter("c", "3b"));

        FormUtils.removeIdenticalAttributes(form1, form2);

        assertEquals(3, form1.size());
        assertEquals(2, form2.size());
    }

    @Test
    public void matchingPasswordsCheckIsOk() {
        Form currentForm = new Form();
        Form updateForm = new Form();

        String plainTextPassword = "ok";
        String hashedPassword = HashUtils.sshaHash(plainTextPassword);

        currentForm.add(PASSWORD.key, hashedPassword);
        updateForm.add(PASSWORD.key, plainTextPassword);

        FormUtils.checkCurrentPassword(currentForm, updateForm);
    }

    @Test(expected = ResourceException.class)
    public void mismatchingPasswordsThrowException() {
        Form currentForm = new Form();
        Form updateForm = new Form();

        String plainTextPassword = "ok";
        String hashedPassword = HashUtils.sshaHash("bad");

        currentForm.add(PASSWORD.key, hashedPassword);
        updateForm.add(PASSWORD.key, plainTextPassword);

        FormUtils.checkCurrentPassword(currentForm, updateForm);
    }

    @Test(expected = ResourceException.class)
    public void nullCurrentPassword() {
        Form currentForm = new Form();
        Form updateForm = new Form();

        String plainTextPassword = "ok";

        updateForm.add(PASSWORD.key, plainTextPassword);

        FormUtils.checkCurrentPassword(currentForm, updateForm);
    }

    @Test(expected = ResourceException.class)
    public void nullUpdatePassword() {
        Form currentForm = new Form();
        Form updateForm = new Form();

        String plainTextPassword = "ok";
        String hashedPassword = HashUtils.sshaHash(plainTextPassword);

        currentForm.add(PASSWORD.key, hashedPassword);

        FormUtils.checkCurrentPassword(currentForm, updateForm);
    }

    @Test
    public void nullPasswordsRemovesAttribute() {
        Form form = new Form();
        form.add(PASSWORD.key, "dummy");

        FormUtils.setNewPasswordInForm(null, null, form);

        assertNull(form.getFirstValue(PASSWORD.key));
    }

    @Test
    public void currentPasswordIsSet() {
        Form form = new Form();
        form.add(PASSWORD.key, "dummy");

        String correctValue = "ok";

        FormUtils.setNewPasswordInForm(correctValue, null, form);

        assertEquals(correctValue, form.getFirstValue(PASSWORD.key));
    }

    @Test
    public void newPasswordIsSet() {
        Form form = new Form();
        form.add(PASSWORD.key, "dummy");

        String correctValue = "ok";

        FormUtils.setNewPasswordInForm(null, correctValue, form);

        assertTrue(correctValue, HashUtils.comparePassword(correctValue, form
                .getFirstValue(PASSWORD.key)));
    }

    @Test
    public void preferNewPasswordToCurrent() {
        Form form = new Form();
        form.add(PASSWORD.key, "dummy");

        String correctValue = "ok";
        String badValue = "bad";

        FormUtils.setNewPasswordInForm(badValue, correctValue, form);

        assertTrue(correctValue, HashUtils.comparePassword(correctValue, form
                .getFirstValue(PASSWORD.key)));
    }

}
