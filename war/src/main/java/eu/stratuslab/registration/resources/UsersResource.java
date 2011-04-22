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

import static org.restlet.data.MediaType.APPLICATION_WWW_FORM;
import static org.restlet.data.MediaType.TEXT_HTML;
import static org.restlet.data.MediaType.TEXT_PLAIN;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

public class UsersResource extends BaseResource {

    private static final String UID_KEY = "uid";

    private static final String EMAIL_KEY = "mail";

    private static final String GIVEN_NAME_KEY = "givenName";

    private static final String SURNAME_KEY = "sn";

    private static final String PASSWORD_KEY = "userPassword";

    private static final String PASSWORD_CHK_KEY = "userPasswordCheck";

    private static final Pattern VALID_USERNAME = Pattern
            .compile("^\\w{3,20}$");

    private static final Pattern WHITESPACE_ONLY = Pattern.compile("^\\s*$");

    private static final Pattern VALID_PASSWORD = Pattern
            .compile("^\\S{8,20}$");

    private static final Set<String> ALLOWED_KEYS;

    static {
        Set<String> keys = new HashSet<String>();
        keys.add(UID_KEY);
        keys.add(EMAIL_KEY);
        keys.add(GIVEN_NAME_KEY);
        keys.add(SURNAME_KEY);
        keys.add(PASSWORD_KEY);
        keys.add(PASSWORD_CHK_KEY);
        ALLOWED_KEYS = Collections.unmodifiableSet(keys);
    }

    @Post
    public Representation createUser(Representation entity) {

        if (entity == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "post with null entity");
        }

        MediaType mediaType = entity.getMediaType();

        Properties userProperties = null;
        if (APPLICATION_WWW_FORM.equals(mediaType, true)) {
            userProperties = processWebForm();
        } else {
            throw new ResourceException(
                    Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, mediaType
                            .getName());
        }

        removeUnknownKeys(userProperties);
        validatePropertyValues(userProperties);

        createUser(userProperties);

        setStatus(Status.SUCCESS_CREATED);

        String userid = userProperties.getProperty(UID_KEY);

        Representation rep = new StringRepresentation("user created",
                TEXT_PLAIN);

        String diskRelativeUrl = "/users/" + userid;
        rep.setLocationRef(getRequest().getResourceRef().getIdentifier()
                + diskRelativeUrl);

        return rep;

    }

    @Get("txt")
    public Representation toText() {
        Map<String, Object> links = listUsers();
        return linksToText(links);
    }

    @Get("html")
    public Representation toHtml() {
        Map<String, Object> links = listUsers();
        return linksToHtml(links);
    }

    private Properties processWebForm() {
        Properties properties = initializeProperties();

        Request request = getRequest();
        Representation entity = request.getEntity();
        Form form = new Form(entity);
        for (String name : form.getNames()) {
            String value = form.getFirstValue(name);
            if (value != null) {
                properties.put(name, value);
            }
        }

        return properties;
    }

    public static Properties initializeProperties() {
        Properties properties = new Properties();
        return properties;
    }

    public static void removeUnknownKeys(Properties properties) {
        for (Object key : properties.keySet()) {
            if (!ALLOWED_KEYS.contains(key)) {
                properties.remove(key);
            }
        }
    }

    public static void validatePropertyValues(Properties properties) {
        if (!isValidUsername(properties.getProperty(UID_KEY))) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "invalid username");
        }
        if (!isValidEmailAddress(properties.getProperty(EMAIL_KEY))) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "invalid email address");
        }
        if (!isValidName(properties.getProperty(GIVEN_NAME_KEY))) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "invalid given name(s)");
        }
        if (!isValidName(properties.getProperty(SURNAME_KEY))) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "invalid given surname");
        }

        String password = properties.getProperty(PASSWORD_KEY);
        String passwordCheck = properties.getProperty(PASSWORD_CHK_KEY);
        if (!isValidPassword(password)) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "invalid password");
        }

        if (!password.equals(passwordCheck)) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "password mismatch");
        }
    }

    public static boolean isValidUsername(String username) {

        if (username == null) {
            return false;
        }

        Matcher matcher = VALID_USERNAME.matcher(username);
        if (!matcher.matches()) {
            return false;
        }

        return true;
    }

    public static boolean isValidEmailAddress(String email) {

        try {
            new InternetAddress(email);
            String[] parts = email.split("@");
            return parts.length == 2 && !"".equals(parts[0])
                    && !"".equals(parts[1]);
        } catch (AddressException e) {
            return false;
        }
    }

    public static boolean isValidName(String name) {
        if (name == null) {
            return false;
        }

        Matcher matcher = WHITESPACE_ONLY.matcher(name);
        if (matcher.matches()) {
            return false;
        }

        return true;
    }

    public static boolean isValidPassword(String password) {

        if (password == null) {
            return false;
        }

        Matcher matcher = VALID_PASSWORD.matcher(password);
        if (!matcher.matches()) {
            return false;
        }

        return true;
    }

    private static void createUser(Properties properties) {
        // TODO: Create implementation for LDAP.
    }

    private Representation linksToHtml(Map<String, Object> infoTree) {
        Representation tpl = templateRepresentation("/html/users.ftl");
        return new TemplateRepresentation(tpl, infoTree, TEXT_HTML);
    }

    private Representation linksToText(Map<String, Object> infoTree) {
        Representation tpl = templateRepresentation("/text/users.ftl");
        return new TemplateRepresentation(tpl, infoTree, TEXT_PLAIN);
    }

    private Map<String, Object> listUsers() {

        Map<String, Object> info = new HashMap<String, Object>();
        List<Properties> userInfoList = new LinkedList<Properties>();
        info.put("users", userInfoList);

        // TODO: Add listing of users.

        return info;
    }

}
