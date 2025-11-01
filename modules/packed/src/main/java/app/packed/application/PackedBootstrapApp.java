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
package app.packed.application;

import static java.util.Objects.requireNonNull;

import app.packed.assembly.Assembly;
import app.packed.assembly.BuildableAssembly;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.runtime.RunState;
import internal.app.packed.ValueBased;
import internal.app.packed.application.GuestBeanHandle;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.ApplicationInstallingSource;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.invoke.MethodHandleInvoker.ApplicationBaseLauncher;
import internal.app.packed.lifecycle.runtime.ApplicationLaunchContext;

/** Implementation of {@link BootstrapApp}. */
@ValueBased
final class PackedBootstrapApp<A, H extends ApplicationHandle<A, ?>> implements BootstrapApp<A>, ApplicationInstallingSource {

    /** An application template that is used for the bootstrap app. */
    // TODO we need to restrict the extensions that can be used to BaseExtension
    // If the guest been uses hooks from various extensions
    private static final PackedApplicationTemplate<?> BOOTSTRAP_APP_TEMPLATE = (PackedApplicationTemplate<?>) ApplicationTemplate
            .ofManaged(PackedBootstrapApp.class).withComponentTags("bootstrap");

    /** The application launcher. */
    private final ApplicationBaseLauncher launcher;

    /** The application template for new applications. */
    private final PackedApplicationTemplate<H> template;

    /**
     * Create a new bootstrap app
     *
     * @param template
     *            the template for the apps that are being bootstrapped.
     */
    private PackedBootstrapApp(PackedApplicationTemplate<H> template, ApplicationBaseLauncher launcher) {
        this.template = requireNonNull(template);
        this.launcher = requireNonNull(launcher);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBootstrapApp<A, H> withExpectsResult(Class<?> resultType) {
        // Could just be a stored internal wirelet for now.
        // Ideen er bootstrapApp.expectsResult(FooBar.class).launch(...);
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Image<A> imageOf(Assembly assembly, Wirelet... wirelets) {
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

        // Creates a new application installer and installs the specified assembly and build the final bootstrap application
        BOOTSTRAP_APP_TEMPLATE.newInstaller(null, BuildGoal.LAUNCH, null).install(assembly);

        // Returned the bootstrap implementation (represented by a construcing method handle) wrapped in this class.
        return new PackedBootstrapApp<A, H>(template, assembly.launcher);
    }

    /** The assembly responsible for building the bootstrap app. */
    private static class BootstrapAppAssembly extends BuildableAssembly {

        /** The method handle to launch the application, the empty MH is used if A is Void.class */
        private ApplicationBaseLauncher launcher = ApplicationBaseLauncher.EMPTY;

        /** The application template for the application type we need to bootstrap. */
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

            // Get the internal configuration of BaseExtension
            ExtensionSetup es = ExtensionSetup.crack(assembly().containerRoot().use(BaseExtension.class));

            // Install the guest bean (code is shared with App-On-App) in the bootstrap application
            this.launcher = GuestBeanHandle.install(template, es, es.container.assembly);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Launcher<A> launcher(Assembly assembly, Wirelet... wirelets) {
        return imageOf(assembly, wirelets);
    }
}

//@Override
//public A checkedLaunch(RunState state, Assembly assembly, Wirelet... wirelets) throws UnhandledApplicationException {
//  ApplicationInstaller<H> installer = template.newInstaller(this, BuildGoal.LAUNCH, launcher, wirelets);
//
//  // Build the application
//  H handle = installer.install(assembly);
//
//  // Create and return an instance of the application interface, wirelets have already been specified in the installer
//  return ApplicationLaunchContext.checkedLaunch(handle, state);
//}
