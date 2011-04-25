package eu.stratuslab.registration.utils;

import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import freemarker.template.Configuration;

public class RequestUtilsTest {

    @Test
    public void insertAndExtractLdapEnv() {

        Request request = new Request();
        Hashtable<String, String> env = new Hashtable<String, String>();

        RequestUtils.insertLdapEnvironment(request, env);
        Hashtable<String, String> recoveredEnv = RequestUtils
                .extractLdapEnvironment(request);

        assertEquals(env, recoveredEnv);
    }

    @Test
    public void insertAndExtractFreeMarkerConfig() {

        Request request = new Request();
        Configuration config = new Configuration();

        RequestUtils.insertFreeMarkerConfig(request, config);
        Configuration recoveredConfig = RequestUtils
                .extractFreeMarkerConfig(request);

        assertEquals(config, recoveredConfig);
    }

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
