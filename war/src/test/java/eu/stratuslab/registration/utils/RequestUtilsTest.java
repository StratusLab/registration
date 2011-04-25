package eu.stratuslab.registration.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

public class RequestUtilsTest {

    @Test(expected = ResourceException.class)
    public void nullEntityThrowsException() {
        RequestUtils.validateInputForm(null);
    }

    @Test(expected = ResourceException.class)
    public void wrongMediaTypeThrowsException() {
        Representation entity = new StringRepresentation("",
                MediaType.TEXT_PLAIN);
        RequestUtils.validateInputForm(entity);
    }

    @Test
    public void checkIdenticalFormIsProduced() {

        Form form = new Form();
        form.add("key", "value");
        Form recoveredForm = RequestUtils.validateInputForm(form
                .getWebRepresentation());
        assertEquals(form, recoveredForm);
    }

}
