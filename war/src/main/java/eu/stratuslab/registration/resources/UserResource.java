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
package eu.stratuslab.registration.resources;

import static org.restlet.data.MediaType.TEXT_HTML;
import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.restlet.Request;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;

public class UserResource extends BaseResource {

    @Get("txt")
    public Representation toText() {
        Representation tpl = templateRepresentation("/text/user.ftl");

        Map<String, Object> infoTree = new HashMap<String, Object>();
        infoTree.put("properties", loadProperties());

        return new TemplateRepresentation(tpl, infoTree, TEXT_PLAIN);
    }

    @Get("html")
    public Representation toHtml() {
        Representation tpl = templateRepresentation("/html/user.ftl");

        Map<String, Object> infoTree = new HashMap<String, Object>();
        infoTree.put("properties", loadProperties());

        return new TemplateRepresentation(tpl, infoTree, TEXT_HTML);
    }

    @Delete
    public void removeUser() {

        String userid = getUserId();
        System.err.println("DELETE USER: " + userid);

        // TODO: Actually remove the user from the database.
    }

    private static Properties loadProperties() {
        return new Properties();
    }

    private String getUserId() {

        Request request = getRequest();

        Map<String, Object> attributes = request.getAttributes();

        return attributes.get("userid").toString();
    }

}
