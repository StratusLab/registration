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

import java.util.Map;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;

public class ProfileUpdatedResource extends BaseResource {

    @Get("html")
    public Representation toHtml() {

        Map<String, Object> info = createInfoStructure(NO_TITLE);

        return templateRepresentation("html/profile_updated.ftl", info,
                TEXT_HTML);
    }

}
