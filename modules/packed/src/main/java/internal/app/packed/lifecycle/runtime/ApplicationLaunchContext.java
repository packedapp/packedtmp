/*
 * Copyright (c) 2026 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package internal.app.packed.lifecycle.runtime;

import static java.util.Objects.requireNonNull;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationMirror;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.lifecycle.RunState;
import app.packed.lifecycle.runtime.ManagedLifecycle;
import app.packed.service.ServiceLocator;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.ApplicationSetup.ApplicationBuildPhase;
import internal.app.packed.application.PackedBootstrapLauncher;

/**
 * A temporary context object that is created whenever we launch an application.
 */
@AutoService(introspector = ApplicationLaunchContextBeanIntrospector.class)
// Wait a bit with transforming this class to a record.
// We might have some mutable fields such as name
public final class ApplicationLaunchContext implements Context<BaseExtension> {

    /** The configuration of the application we are launching. */
    public final ApplicationSetup application;

    public final ContainerRunner runner;

    private ApplicationLaunchContext(ContainerRunner runner, ApplicationSetup application) {
        this.application = application;
        this.runner = runner;
    }

    public ApplicationMirror mirror() {
        return application.mirror();
    }

    /** {@return the name of the application} */
    public String name() {
        return application.container().name();
    }

    ManagedLifecycle runtime() {
        if (runner.runtime != null) {
            return runner.runtime;
        }
        throw new UnsupportedOperationException("This component does not have a runtime");
    }

    /**
     * Returns a service locator for the system. If a service extension is not installed, returns
     * {@link ServiceLocator#of()}.
     *
     * @return a service locator for the application
     */
    public ServiceLocator serviceLocator() {
        return application.container().servicesMain().newExportedServiceLocator(runner.pool());
    }

//    @SuppressWarnings("unused")
//    public static final <A> A checkedLaunch(ApplicationHandle<A, ?> handle, RunState state, Wirelet... wirelets) throws UnhandledApplicationException {
//        return launch(handle, state, wirelets);
//    }

    public static <A> A launch(PackedBootstrapLauncher<A> launcher, ApplicationHandle<A, ?> handle, RunState state) {
       return launch(handle, state);
    }

    /**
     * Launches an instance of the application this handle represents.
     * <p>
     * A handle can be used multiple types.
     *
     * @param state
     *            the state to launch the application in
     * @param wirelets
     *            optional wirelets
     * @return the application instance
     *
     * @throws UnsupportedOperationException
     *             if managed and not a root container\
     * @see #launch(GuestManager, Wirelet...)
     */
    @SuppressWarnings("unchecked")
    public static <A> A launch(ApplicationHandle<A, ?> handle, RunState state) {
        ApplicationSetup application = ApplicationSetup.crack(handle);
        requireNonNull(state, "state is null");
        if (application.phase != ApplicationBuildPhase.COMPLETED) {
            throw new IllegalStateException("Cannot launch the application before it has finished building");
        }

        ContainerRunner runner = new ContainerRunner(application);

        // Create a launch context
        ApplicationLaunchContext context = new ApplicationLaunchContext(runner, application);

        context.runner.run(state);

        Object result = application.launcher.launch(context);
        return (A) result;
    }
}

final class ApplicationLaunchContextBeanIntrospector extends BeanIntrospector<BaseExtension> {

    @Override
    public void onAutoService(Key<?> key, OnAutoService service) {
        if (ApplicationLaunchContext.class.isAssignableFrom(key.rawType())) {
            service.binder().bindContext(ApplicationLaunchContext.class);
        } else {
            super.onAutoService(key, service);
        }
    }
}
