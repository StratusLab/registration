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
