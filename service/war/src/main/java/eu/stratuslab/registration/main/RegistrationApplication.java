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

import eu.stratuslab.registration.cfg.AppConfiguration;
import eu.stratuslab.registration.guards.LdapVerifier;
import eu.stratuslab.registration.resources.ActionResource;
import eu.stratuslab.registration.resources.ForceTrailingSlashResource;
import eu.stratuslab.registration.resources.HomeResource;
import eu.stratuslab.registration.resources.PoliciesResource;
import eu.stratuslab.registration.resources.ProfileResource;
import eu.stratuslab.registration.resources.ProfileUpdatedResource;
import eu.stratuslab.registration.resources.RegisterResource;
import eu.stratuslab.registration.resources.ResetResource;
import eu.stratuslab.registration.resources.ResetStartedResource;
import eu.stratuslab.registration.resources.SuccessResource;
import eu.stratuslab.registration.resources.UsersResource;
import eu.stratuslab.registration.utils.RequestUtils;

public class RegistrationApplication extends Application {

    public RegistrationApplication() {

        super();

        setName("StratusLab Registration Application");
        setDescription("StratusLab server to allow web-based registration.");
        setOwner("StratusLab");
        setAuthor("Charles Loomis");

        getTunnelService().setUserAgentTunnel(true);

        setStatusService(new CommonStatusService());

    }

    @Override
    public Restlet createInboundRoot() {

        // Do not do this during the constructor because the context will not
        // yet have been initialized.
        AppConfiguration appConfiguration = new AppConfiguration(getContext());

        Router router = new RootRouter(getContext(), appConfiguration);

        TemplateRoute route = null;

        router.attach("/users/", UsersResource.class);
        route = router.attach("/users", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/register/", RegisterResource.class);
        route = router.attach("/register", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/success/", SuccessResource.class);
        route = router.attach("/success", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/profile/", setupGuard(ProfileResource.class));
        route = router.attach("/profile", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/profile_updated/", ProfileUpdatedResource.class);
        route = router.attach("/profile_updated",
                ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/policies/", PoliciesResource.class);
        route = router.attach("/policies", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/reset/", ResetResource.class);
        route = router.attach("/reset", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/reset_started/", ResetStartedResource.class);
        route = router.attach("/reset_started",
                ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/action/", ActionResource.class);
        route = router.attach("/action", ForceTrailingSlashResource.class);
        route.setMatchingMode(Template.MODE_EQUALS);

        router.attach("/action/{uuid}", ActionResource.class);

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
                ChallengeScheme.HTTP_BASIC, "StratusLab");

        guard.setVerifier(new LdapVerifier());

        guard.setNext(targetClass);

        return guard;
    }

    public static class RootRouter extends Router {

        private final AppConfiguration appConfiguration;

        public RootRouter(Context context, AppConfiguration appConfiguration) {
            super(context);
            this.appConfiguration = appConfiguration;
        }

        @Override
        public void doHandle(Restlet next, Request request, Response response) {
            RequestUtils.insertAppConfiguration(request, appConfiguration);
            super.doHandle(next, request, response);
        }

    }

}
