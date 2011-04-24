package eu.stratuslab.registration.main;

import java.util.Hashtable;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.LocalReference;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;

import eu.stratuslab.registration.resources.ForceTrailingSlashResource;
import eu.stratuslab.registration.resources.ProfileResource;
import eu.stratuslab.registration.resources.RegisterResource;
import eu.stratuslab.registration.resources.UsersResource;
import eu.stratuslab.registration.utils.LDAPUtils;
import eu.stratuslab.registration.utils.RequestUtils;
import freemarker.template.Configuration;

public class RegistrationApplication extends Application {

    private final Hashtable<String, String> LDAP_JNDI_ENV;

    private Configuration freeMarkerConfig;

    public RegistrationApplication() {

        super();

        setName("StratusLab Registration Application");
        setDescription("StratusLab server to allow web-based registration.");
        setOwner("StratusLab");
        setAuthor("Charles Loomis");

        getTunnelService().setUserAgentTunnel(true);

        LDAP_JNDI_ENV = LDAPUtils.createLdapConnectionEnvironment();

    }

    public Configuration getFreeMarkerConfig() {
        return freeMarkerConfig;
    }

    public static Configuration createFreeMarkerConfig(Context context) {

        Configuration cfg = new Configuration();
        cfg.setLocalizedLookup(false);

        LocalReference fmBaseRef = LocalReference
                .createClapReference("/freemarker/");
        cfg.setTemplateLoader(new ContextTemplateLoader(context, fmBaseRef));

        return cfg;
    }

    @Override
    public Restlet createInboundRoot() {

        // Do not do this during the constructor because the context will not
        // yet have been initialized.
        freeMarkerConfig = createFreeMarkerConfig(getContext());

        Router router = new RootRouter(getContext());

        router.attach("/users/", UsersResource.class);
        router.attach("/users", ForceTrailingSlashResource.class);

        router.attach("/register/", RegisterResource.class);
        router.attach("/register", ForceTrailingSlashResource.class);

        router.attach("/profile/", setupGuard(ProfileResource.class));
        router.attach("/profile", ForceTrailingSlashResource.class);

        Directory docsDir = new Directory(getContext(), "war:///docs");
        docsDir.setNegotiatingContent(false);
        docsDir.setIndexName("index.html");
        router.attach("/docs/", docsDir);
        router.attach("/docs", ForceTrailingSlashResource.class);

        Directory cssDir = new Directory(getContext(), "war:///css");
        docsDir.setNegotiatingContent(false);
        docsDir.setIndexName("index.html");
        router.attach("/css/", cssDir);
        router.attach("/css", ForceTrailingSlashResource.class);

        Directory indexDir = new Directory(getContext(), "war:///");
        indexDir.setNegotiatingContent(false);
        indexDir.setIndexName("index.html");
        router.attach("/", indexDir);

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
            RequestUtils.insertLdapEnvironment(request, LDAP_JNDI_ENV);
            RequestUtils.insertFreeMarkerConfig(request, freeMarkerConfig);
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
