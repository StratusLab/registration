package eu.stratuslab.registration.main;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

import eu.stratuslab.registration.resources.ForceTrailingSlashResource;
import eu.stratuslab.registration.resources.RegisterResource;
import eu.stratuslab.registration.resources.UserResource;
import eu.stratuslab.registration.resources.UsersResource;

public class RegistrationApplication extends Application {

    public RegistrationApplication() {

        setName("StratusLab Registration Application");
        setDescription("StratusLab server to allow web-based registration.");
        setOwner("StratusLab");
        setAuthor("Charles Loomis");

        getTunnelService().setUserAgentTunnel(true);
    }

    @Override
    public Restlet createInboundRoot() {

        Router router = new Router(getContext());

        router.attach("/users/{uid}/", UserResource.class);
        router.attach("/users/{uid}", ForceTrailingSlashResource.class);

        router.attach("/users/", UsersResource.class);
        router.attach("/users", ForceTrailingSlashResource.class);

        router.attach("/register/", RegisterResource.class);
        router.attach("/register", ForceTrailingSlashResource.class);

        Directory docsDir = new Directory(getContext(), "war:///docs");
        docsDir.setNegotiatingContent(false);
        docsDir.setIndexName("index.html");
        router.attach("/docs/", docsDir);
        router.attach("/docs", ForceTrailingSlashResource.class);

        Directory indexDir = new Directory(getContext(), "war:///");
        indexDir.setNegotiatingContent(false);
        indexDir.setIndexName("index.html");
        router.attach("/", indexDir);

        return router;
    }

}
