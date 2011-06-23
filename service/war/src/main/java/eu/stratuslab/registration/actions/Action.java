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
package eu.stratuslab.registration.actions;

import java.io.Serializable;

import org.restlet.Request;

public interface Action extends Serializable {

    /**
     * Execute the defined action. A message about what was performed should be
     * returned. An exception should be thrown if a problem performing the
     * action occurred.
     * 
     * @return message about the action that was performed
     */
    String execute(Request request);

    /**
     * Abort the defined action. A message should be returned indicating what
     * action was aborted.
     * 
     * @return message about what action was aborted
     */
    String abort(Request request);

}
