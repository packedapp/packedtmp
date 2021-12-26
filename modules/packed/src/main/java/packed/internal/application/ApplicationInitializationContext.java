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
package packed.internal.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.container.Wirelet;
import app.packed.inject.service.ServiceLocator;
import app.packed.lifecycle.LifecycleApplicationController;
import app.packed.lifecycle.RunState;
import packed.internal.container.InternalWirelet;
import packed.internal.container.WireletWrapper;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.lifetime.LifetimePool;
import packed.internal.lifetime.LifetimePoolWriteable;
import packed.internal.util.ThrowableUtil;

/**
 * A temporary context object that is created whenever we launch an application.
 */
public final class ApplicationInitializationContext implements LifetimePoolWriteable {

    /** The configuration of the application we are launching. */
    public final ApplicationSetup application;

    /** The launch mode of the application. */
    final RunState launchMode;

    /** The name of the application. May be overridden via {@link Wirelet#named(String)} if image. */
    public String name;

    /** The runtime component node we are building. */
    private LifetimePool pool;

    /** If the application is stateful, the applications runtime. */
    @Nullable
    final PackedApplicationRuntime runtime;

    /** Wirelets specified if instantiating an image. */
    @Nullable
    private final WireletWrapper wirelets;

    private ApplicationInitializationContext(ApplicationSetup application, WireletWrapper wirelets) {
        this.application = application;
        this.wirelets = wirelets;
        this.name = requireNonNull(application.container.getName());
        this.launchMode = requireNonNull(application.launchMode);
        this.runtime = application.runtimeAccessor == null ? null : new PackedApplicationRuntime(this);
    }

    /** {@return the name of the application} */
    public String name() {
        return name;
    }

    public LifetimePool pool() {
        return pool;
    }

    LifecycleApplicationController runtime() {
        if (runtime != null) {
            return runtime;
        }
        throw new UnsupportedOperationException("This component does not have a runtime");
    }

    /**
     * Returns a service locator for the system. If a service extension is not installed, returns
     * {@link ServiceLocator#of()}.
     * 
     * @return a service locator for the application
     */
    public ServiceLocator services() {
        ServiceManagerSetup sm = application.container.injection.getServiceManager();
        return sm == null ? ServiceLocator.of() : sm.newServiceLocator(application.driver, pool);
    }

    @Override
    public void writeToPool(LifetimePool pool) {
        if (runtime != null) {
            application.runtimeAccessor.store(pool, runtime);
        }
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
    static <A> A launch(PackedApplicationDriver<A> driver, ApplicationSetup application, @Nullable WireletWrapper wirelets) {
        assert driver == application.driver; // it is just here because of <A>

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

        LifetimePool pool = context.pool = application.container.lifetime.pool.newPool(context);

        // Run all initializers
        for (MethodHandle mh : application.container.lifetime.initializers) {
            try {
                mh.invoke(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
        
        // INITIALIZATION IS DONE

        if (context.runtime != null) {
            context.runtime.launch(application, context);
        }

        return driver.newInstance(context);
    }
}