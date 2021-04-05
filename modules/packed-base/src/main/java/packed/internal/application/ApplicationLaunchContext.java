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

import app.packed.application.ApplicationRuntime;
import app.packed.application.ApplicationWirelets;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.inject.ServiceLocator;
import app.packed.state.RunState;
import packed.internal.component.InternalWirelet;
import packed.internal.component.PackedApplicationRuntime;
import packed.internal.component.WireletWrapper;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.invoke.constantpool.ConstantPool;
import packed.internal.invoke.constantpool.PoolWriteable;
import packed.internal.util.ThrowableUtil;

/**
 * A temporary context object that is created whenever we launch an application.
 */
public final class ApplicationLaunchContext implements PoolWriteable {

    /** The application we are launching */
    public final ApplicationSetup application;

    /**
     * The launch mode of the application. May be overridden via {@link ApplicationWirelets#launchMode(RunState)} if image.
     */
    RunState launchMode;

    /** The name of the application. May be overridden via {@link Wirelet#named(String)} if image. */
    public String name;

    /** The runtime component node we are building. */
    private ConstantPool pool;

    /** If the application is stateful, the applications runtime. */
    @Nullable
    final PackedApplicationRuntime runtime;

    /** Wirelets specified if instantiating an image. */
    @Nullable
    private final WireletWrapper wirelets;

    private ApplicationLaunchContext(ApplicationSetup application, WireletWrapper wirelets) {
        this.application = requireNonNull(application);
        this.wirelets = wirelets;
        this.name = requireNonNull(application.container.getName());
        this.launchMode = requireNonNull(application.launchMode);
        this.runtime = application.runtimePoolIndex == -1 ? null : new PackedApplicationRuntime(this);
    }

    public String name() {
        return name;
    }

    public ConstantPool pool() {
        return pool;
    }

    ApplicationRuntime runtime() {
        if (runtime != null) {
            return runtime;
        }
        throw new UnsupportedOperationException("This component does not have a runtime");
    }

    /**
     * Returns a service locator for the system. If the service extension is not installed, returns
     * {@link ServiceLocator#of()}.
     * 
     * @return a service locator for the system
     */
    public ServiceLocator services() {
        ServiceManagerSetup sm = application.container.injection.getServiceManager();
        return sm == null ? ServiceLocator.of() : sm.newServiceLocator(application.driver, pool());
    }

    @Override
    public void writeToPool(ConstantPool pool) {
        if (runtime != null) {
            pool.storeObject(application.runtimePoolIndex, runtime);
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
        ApplicationLaunchContext context = new ApplicationLaunchContext(application, wirelets);

        // Apply all internal wirelets
        if (wirelets != null) {
            for (Wirelet w : wirelets.wirelets) {
                if (w instanceof InternalWirelet iw) {
                    iw.onImageInstantiation(application.container, context);
                }
            }
        }

        ConstantPool pool = context.pool = application.constantPool.newPool(context);

        // Run all initializers
        for (MethodHandle mh : application.initializers) {
            try {
                mh.invoke(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        if (context.runtime != null) {
            context.runtime.launch(application, context);
        }

        return driver.newApplication(context);
    }
}
