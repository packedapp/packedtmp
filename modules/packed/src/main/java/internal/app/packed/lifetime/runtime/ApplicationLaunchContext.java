/*
 * Copyright (c) 2008 Kasper Nielsen.
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
package internal.app.packed.lifetime.runtime;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import app.packed.application.ApplicationMirror;
import app.packed.context.Context;
import app.packed.context.ContextualServiceProvider;
import app.packed.extension.BaseExtension;
import app.packed.runtime.ManagedLifecycle;
import app.packed.service.ServiceLocator;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.wirelets.WireletSelectionArray;

/**
 * A temporary context object that is created whenever we launch an application.
 */
@ContextualServiceProvider(extension = BaseExtension.class)
// Wait a bit with transforming this class to a record.
// We might have some mutable fields such as name
public final class ApplicationLaunchContext implements Context<BaseExtension> {

    public static final MethodHandle EMPTY_MH = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class));

    /** The configuration of the application we are launching. */
    public final ApplicationSetup application;

    public final ContainerRunner runner;

    /** Wirelets specified if instantiating an image. */
    @Nullable
    private final WireletSelectionArray<?> wirelets;

    public ApplicationLaunchContext(ContainerRunner runner, ApplicationSetup application, WireletSelectionArray<?> wirelets) {
        this.application = application;
        this.wirelets = wirelets;
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
//
//    /**
//     * Launches the application. Either directly or from an image
//     *
//     * @param <A>
//     *            the type of application shell
//     * @param driver
//     *            the driver of the application.
//     * @param application
//     *            the application we are launching
//     * @param wirelets
//     *            optional wirelets is always null if not launched from an image
//     * @return the application instance
//     */
//    public static ApplicationLaunchContext newLaunchContext(RunState state, ApplicationSetup application, @Nullable WireletSelectionArray<?> wirelets) {
//        // Create a launch context
//        ApplicationLaunchContext context = new ApplicationLaunchContext(application, wirelets);
//
//        // Apply all internal wirelets
//        if (wirelets != null) {
//            for (Wirelet w : wirelets) {
//                if (w instanceof InternalBuildWirelet iw) {
//                    iw.onImageLaunch(application.container(), context);
//                }
//            }
//        }
//
//        context.runner.run(state);
//
//        return context;
//    }
}
