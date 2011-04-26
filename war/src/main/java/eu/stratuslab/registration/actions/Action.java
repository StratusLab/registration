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
    public String execute(Request request);

    /**
     * Abort the defined action. A message should be returned indicating what
     * action was aborted.
     * 
     * @return message about what action was aborted
     */
    public String abort(Request request);

}
