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

import static java.util.Objects.requireNonNull;

import app.packed.container.Wirelet;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.lifetime.sandbox.ManagedLifetimeController;
import app.packed.service.ServiceLocator;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationDriver;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.InternalWirelet;
import internal.app.packed.container.WireletWrapper;
import internal.app.packed.lifetime.sandbox.OldLifetimeKind;

/**
 * A temporary context object that is created whenever we launch an application.
 */
public final class ApplicationInitializationContext implements Context<BaseExtension>{

    /** The configuration of the application we are launching. */
    public final ApplicationSetup application;

    public final ContainerRunner cr;

    /** The launch mode of the application. */
    final OldLifetimeKind lifetimeKind;

    /** The name of the application. May be overridden via {@link Wirelet#named(String)} if image. */
    public String name;

    /** Wirelets specified if instantiating an image. */
    @Nullable
    private final WireletWrapper wirelets;

    private ApplicationInitializationContext(ApplicationSetup application, WireletWrapper wirelets) {
        this.application = application;
        this.wirelets = wirelets;
        this.name = requireNonNull(application.container.name);
        this.lifetimeKind = requireNonNull(application.driver.lifetimeKind());
        this.cr = new ContainerRunner(application);
    }

    /** {@return the name of the application} */
    public String name() {
        return name;
    }

    ManagedLifetimeController runtime() {
        if (cr.runtime != null) {
            return cr.runtime;
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
        return application.container.sm.newExportedServiceLocator(cr.pool());
    }

    /**
     * Launches the application. Either directly or from an image
     *
     * @param <A>
     *            the type of application shell
     * @param driver
     *            the driver of the application.
     * @param application
     *            the application we are launching
     * @param wirelets
     *            optional wirelets is always null if not launched from an image
     * @return the application instance
     */
    public static <A> A launch(ApplicationDriver<A> driver, ApplicationSetup application, @Nullable WireletWrapper wirelets) {

        // Create a launch context
        ApplicationInitializationContext context = new ApplicationInitializationContext(application, wirelets);

        // Apply all internal wirelets
        if (wirelets != null) {
            for (Wirelet w : wirelets.wirelets) {
                if (w instanceof InternalWirelet iw) {
                    iw.onImageInstantiation(application.container, context);
                }
            }
        }

        context.cr.run(application.container);

        return driver.newInstance(context);
    }

}
