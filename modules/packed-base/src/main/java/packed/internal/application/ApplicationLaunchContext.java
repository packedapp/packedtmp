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

import app.packed.application.ApplicationRuntime;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.Wirelet;
import app.packed.inject.ServiceLocator;
import app.packed.state.RunState;
import packed.internal.component.InternalWirelet;
import packed.internal.component.PackedApplicationRuntime;
import packed.internal.component.PackedComponent;
import packed.internal.component.WireletWrapper;
import packed.internal.container.ContainerSetup;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.invoke.constantpool.ConstantPool;
import packed.internal.invoke.constantpool.PoolWriteable;

/**
 * A temporary context object that is created when launching an application.
 * <p>
 * Describes which phases it is available from
 * <p>
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 */
// Ideen er vi skal bruge den til at registrere fejl...

// MethodHandle stableAccess(Object[] array) <-- returns 
public final class ApplicationLaunchContext implements PoolWriteable {

    public final ApplicationSetup application;

    /** The runtime component node we are building. */
    public PackedComponent component;

    final ContainerSetup container;

    public RunState launchMode;

    public String name;

    @Nullable
    private final WireletWrapper wirelets;

    final PackedApplicationRuntime runtime;

    private ApplicationLaunchContext(ApplicationSetup application, WireletWrapper wirelets) {
        this.application = requireNonNull(application);
        this.container = application.container;
        this.wirelets = wirelets;
        this.name = container.getName();
        this.launchMode = application.launchMode;
        this.runtime = application.runtimePoolIndex == -1 ? null : new PackedApplicationRuntime(this);
    }

    /**
     * Returns the top component.
     * 
     * @return the top component
     */
    Component component() {
        return component;
    }

    public ConstantPool pool() {
        return component.pool;
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
        ServiceManagerSetup sm = container.cis.getServiceManager();
        return sm == null ? ServiceLocator.of() : sm.newServiceLocator(application.driver, pool());
    }

    /**
     * Returns a list of wirelets that used to instantiate. This may include wirelets that are not present at build time if
     * using an image.
     * 
     * @return a list of wirelets that used to instantiate
     */
    public WireletWrapper wirelets() {
        return wirelets;
    }

    /**
     * @param <A>
     *            the type of application shell
     * @param driver
     *            the driver of the application
     * @param application
     * @param wirelets
     * @return
     */
    // Or is is a build we are launching???
    static <A> A launch(PackedApplicationDriver<A> driver, ApplicationSetup application, @Nullable WireletWrapper wirelets) {
        assert driver == application.driver;
        ContainerSetup container = application.container;

        ApplicationLaunchContext context = new ApplicationLaunchContext(application, wirelets);

        // Apply all internal wirelets
        if (wirelets != null) {
            for (Wirelet w : wirelets.wirelets) {
                if (w instanceof InternalWirelet iw) {
                    iw.onImageInstantiation(container, context);
                }
            }
        }

        // Instantiates the whole component tree (well @Initialize does not yet work)
        // pic.component is set from PackedComponent
        new PackedComponent(null, container, context);

        // TODO initialize

        if (context.runtime != null) {
            context.runtime.onInitialized(container, context);
        }

        return driver.newApplication(context);
    }

    @Override
    public void writeToPool(ConstantPool pool) {
        if (runtime != null) {
            pool.storeObject(application.runtimePoolIndex, runtime);
        }
    }
}
