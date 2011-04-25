package eu.stratuslab.registration.main;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.resources.ForceTrailingSlashResource;
import eu.stratuslab.registration.resources.HomeResource;
import eu.stratuslab.registration.resources.PoliciesResource;
import eu.stratuslab.registration.resources.ProfileResource;
import eu.stratuslab.registration.resources.RegisterResource;
import eu.stratuslab.registration.resources.UsersResource;
import eu.stratuslab.registration.utils.RequestUtils;

public class RegistrationApplication extends Application {

    private AppConfiguration appConfiguration;

    public RegistrationApplication() {

        super();

        setName("StratusLab Registration Application");
        setDescription("StratusLab server to allow web-based registration.");
        setOwner("StratusLab");
        setAuthor("Charles Loomis");

        getTunnelService().setUserAgentTunnel(true);

    }

    @Override
    public Restlet createInboundRoot() {

        // Do not do this during the constructor because the context will not
        // yet have been initialized.
        appConfiguration = new AppConfiguration(getContext());

        Router router = new RootRouter(getContext());

        TemplateRoute route = null;

        router.attach("/users/", UsersResource.class);
        route = router.attach("/users", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/register/", RegisterResource.class);
        route = router.attach("/register", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/profile/", setupGuard(ProfileResource.class));
        route = router.attach("/profile", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/policies/", PoliciesResource.class);
        route = router.attach("/policies", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        Directory cssDir = new Directory(getContext(), "war:///css");
        cssDir.setNegotiatingContent(false);
        cssDir.setIndexName("index.html");
        router.attach("/css/", cssDir);
        router.attach("/css", ForceTrailingSlashResource.class);

        router.attachDefault(HomeResource.class);

        return router;
    }

    public ChallengeAuthenticator setupGuard(Class<?> targetClass) {

        ChallengeAuthenticator guard = new ChallengeAuthenticator(null,
                ChallengeScheme.HTTP_BASIC, "testRealm");

        guard.setVerifier(new TestVerifier());

        guard.setNext(targetClass);

        return guard;
    }

    public class RootRouter extends Router {

        public RootRouter(Context context) {
            super(context);
        }

        @Override
        public void doHandle(Restlet next, Request request, Response response) {
            RequestUtils.insertAppConfiguration(request, appConfiguration);
            super.doHandle(next, request, response);
        }

    }

    public static class TestVerifier extends LocalVerifier {

        @Override
        public char[] getLocalSecret(String identifier) {
            return "secret".toCharArray();
        }

    }

}
