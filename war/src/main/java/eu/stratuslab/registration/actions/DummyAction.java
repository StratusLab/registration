package eu.stratuslab.registration.actions;

import org.restlet.Request;

@SuppressWarnings("serial")
public class DummyAction implements Action {

    private int serialNumber;

    public DummyAction() {
        serialNumber = (int) (Math.random() * 10000.);
    }

    public String abort(Request request) {
        return String.format("dummy action (%d) was aborted", serialNumber);
    }

    public String execute(Request request) {
        return String.format("dummy action (%d) was executed", serialNumber);
    }

}
