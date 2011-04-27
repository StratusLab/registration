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
package eu.stratuslab.registration.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

import eu.stratuslab.registration.utils.FormUtils;

public class UserEntryTest {

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
    public void checkDerivedAttributes() {

        Form form = new Form();

        String last = "Last";
        String first = "First";
        String expected = first + " " + last;

        form.add(UserAttribute.GIVEN_NAME.key, first);
        form.add(UserAttribute.SURNAME.key, last);

        FormUtils.addDerivedAttributes(form);

        // Common name (CN) is correct.
        assertEquals(expected, form.getFirstValue(FormUtils.COMMON_NAME_KEY));

        // Object class attribute was added.
        assertNotNull(form.getFirstValue(FormUtils.OBJECT_CLASS_KEY));

    }

}
