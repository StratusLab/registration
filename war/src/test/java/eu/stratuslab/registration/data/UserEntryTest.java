package eu.stratuslab.registration.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;

public class UserEntryTest {

    @Test
    public void checkKnownAttrsAreNotRemoved() {

        Form form = new Form();

        for (UserAttribute attr : UserAttribute.values()) {
            form.add(attr.key, "value");
        }

        form = UserEntry.sanitizeForm(form);

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

        form = UserEntry.sanitizeForm(form);

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

        form = UserEntry.sanitizeForm(form);

        // Will throw an exception if unknown key is found.
        for (String key : form.getNames()) {
            UserAttribute.valueWithKey(key);
        }

    }

    @Test(expected = ResourceException.class)
    public void checkInvalidKey() {

        Form form = new Form();

        form.add(UserAttribute.UID.key, "bad username");

        UserEntry.validateEntries(form);

    }

    @Test(expected = IllegalArgumentException.class)
    public void checkUnknownKey() {

        Form form = new Form();

        form.add("badkey", "bad value");

        UserEntry.validateEntries(form);

    }

    @Test
    public void checkDerivedAttributes() {

        Form form = new Form();

        String last = "Last";
        String first = "First";
        String expected = first + " " + last;

        form.add(UserAttribute.GIVEN_NAME.key, first);
        form.add(UserAttribute.SURNAME.key, last);

        UserEntry.addDerivedAttributes(form);

        // Common name (CN) is correct.
        assertEquals(expected, form.getFirstValue(UserEntry.COMMON_NAME_KEY));

        // Object class attribute was added.
        assertNotNull(form.getFirstValue(UserEntry.OBJECT_CLASS_KEY));

    }

}
