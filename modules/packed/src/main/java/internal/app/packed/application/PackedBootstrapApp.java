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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.Set;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationMirror;
import app.packed.application.BaseImage;
import app.packed.application.BootstrapApp;
import app.packed.assembly.Assembly;
import app.packed.assembly.BuildableAssembly;
import app.packed.bean.BeanInstaller;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.runtime.RunState;
import internal.app.packed.ValueBased;
import internal.app.packed.application.PackedApplicationTemplate.ApplicationInstallingSource;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.lifecycle.lifetime.runtime.ApplicationLaunchContext;

/** Implementation of {@link BootstrapApp}. */
@ValueBased
public final class PackedBootstrapApp<A, H extends ApplicationHandle<A, ?>> implements BootstrapApp<A>, ApplicationInstallingSource {

    /** An application template that is used for the bootstrap app. */
    private static final PackedApplicationTemplate<?> BOOTSTRAP_APP_TEMPLATE = new PackedApplicationTemplate<>(Void.class, null, ApplicationHandle.class,
            ApplicationHandle::new, new PackedContainerTemplate<>(PackedContainerKind.BOOTSTRAP_APPLICATION, PackedBootstrapApp.class), Set.of("bootstrap"));

    /** The application launcher. */
    private final MethodHandle launcher;

    /** The application template for new applications. */
    private final PackedApplicationTemplate<H> template;

    /**
     * Create a new bootstrap app
     *
     * @param template
     *            the template for the apps that are being bootstrapped.
     */
    private PackedBootstrapApp(PackedApplicationTemplate<H> template, MethodHandle launcher) {
        this.template = requireNonNull(template);
        this.launcher = requireNonNull(launcher);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBootstrapApp<A, H> expectsResult(Class<?> resultType) {
        // Ideen er bootstrapApp.expectsResult(FooBar.class).launch(...);
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public BaseImage<A> imageOf(Assembly assembly, Wirelet... wirelets) {
        ApplicationInstaller<H> installer = template.newInstaller(this, BuildGoal.IMAGE, launcher, wirelets);

        // Build the application
        H handle = installer.install(assembly);

        // Returns an image for the application
        return handle.image();
    }

    /** {@inheritDoc} */
    @Override
    public A launch(RunState state, Assembly assembly, Wirelet... wirelets) {
        ApplicationInstaller<H> installer = template.newInstaller(this, BuildGoal.LAUNCH, launcher, wirelets);

        // Build the application
        H handle = installer.install(assembly);

        // Create and return an instance of the application interface, wirelets have already been specified in the installer
        return ApplicationLaunchContext.launch(handle, state);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        ApplicationInstaller<H> installer = template.newInstaller(this, BuildGoal.MIRROR, launcher, wirelets);

        // Build the application
        H handle = installer.install(assembly);

        // Returns a mirror for the application
        return handle.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public void verify(Assembly assembly, Wirelet... wirelets) {
        ApplicationInstaller<H> installer = template.newInstaller(this, BuildGoal.VERIFY, launcher, wirelets);

        // Builds (and verifies) the application
        installer.install(assembly);
    }

    /**
     * Builds a new bootstrap app for applications represented by the specified template.
     *
     * @param <A>
     * @param template
     *            the template for the type applications that should be bootstrapped
     * @return a new bootstrap app
     */

    public static <A, H extends ApplicationHandle<A, ?>> BootstrapApp<A> of(PackedApplicationTemplate<H> template) {
        // We need a an assembly to build the (bootstrap) application
        BootstrapAppAssembly assembly = new BootstrapAppAssembly(template);

        // Build the bootstrap application
        BOOTSTRAP_APP_TEMPLATE.newInstaller(null, BuildGoal.LAUNCH, null).install(assembly);

        // Returned the bootstrap implementation (represented by a construcing method handle) wrapped in this class.
        return new PackedBootstrapApp<A, H>(template, assembly.mh);
    }

    /** The assembly responsible for building the bootstrap app. */
    private static class BootstrapAppAssembly extends BuildableAssembly {

        /** The method handle to launch the application, the empty MH is used if A is Void.class */
        private MethodHandle mh = ApplicationLaunchContext.EMPTY_MH;

        private final PackedApplicationTemplate<?> template;

        private BootstrapAppAssembly(PackedApplicationTemplate<?> template) {
            this.template = requireNonNull(template);
        }

        /** {@inheritDoc} */
        @Override
        protected void build() {
            if (template.guestClass() == Void.class) {
                return;
            }

            // Get the internal BaseExtension, we need this to use a customer BeanTemplate
            BaseExtension base = assembly().containerRoot().use(BaseExtension.class);
            ExtensionSetup es = ExtensionSetup.crack(base);

            // Create a new installer
            BeanInstaller installer = GuestBeanHandle.GUEST_BEAN_TEMPLATE.newInstaller(es, es.container.assembly);

            // Install the guest bean if needed (code is shared with App-On-App)
            this.mh = GuestBeanHandle.installGuestBean(template, installer);
        }
    }
}
