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

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BaseImage;
import app.packed.application.BootstrapApp;
import app.packed.assembly.Assembly;
import app.packed.assembly.BuildableAssembly;
import app.packed.bean.BeanTemplate;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.runtime.RunState;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;

/**
 * A bootstrap app is a special type of application that can be used to create other (non-bootstrap) application.
 * <p>
 * Bootstrap apps cannot directly modify the applications that it bootstraps. It cannot, for example, install an
 * extension in the application. However, it can say it can only bootstrap applications that have the extension
 * installed, failing with a build exception if the developer does not install the extension. As such, the bootstrap app
 * can only setup requirements for the application that it bootstraps. It cannot directly make the needed changes to the
 * bootstrapped application.
 * <p>
 * Bootstrap applications are rarely used directly by users. Instead users typically use thin wrappers such as
 * {@link App} or {@link app.packed.service.ServiceLocator} to create new applications. However, if greater control of
 * the application is needed users may create their own bootstrap application.
 * <p>
 * Normally, you never create more than a single instance of a bootstrap app. Bootstrap applications are, unless
 * otherwise specified, safe to use concurrently.
 *
 * @param <A>
 *            the type of application this bootstrap app creates.
 * @see App
 * @see JobApp
 * @see DaemonApp
 * @see app.packed.cli.CliApp
 * @see app.packed.service.ServiceLocator
 */
// ProducerApp??? It just generatesX
public final /* value */ class PackedBootstrapApp<A> implements BootstrapApp<A> {

    /** The application template for new applications. */
    private final PackedApplicationTemplate<A> template;

    /**
     * Create a new bootstrap app
     *
     * @param template
     *            the template for the apps that are being bootstrapped.
     */
    private PackedBootstrapApp(PackedApplicationTemplate<A> template) {
        this.template = requireNonNull(template);
    }

    /** {@inheritDoc} */
    @Override
    public BootstrapApp<A> expectsResult(Class<?> resultType) {
        // Ideen er bootstrapApp.expectsResult(FooBar.class).launch(...);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public BaseImage<A> imageOf(Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer<A> installer = template.newInstaller(BuildGoal.IMAGE, wirelets);

        // Build the application
        ApplicationHandle<?, ?> handle = installer.install(assembly, ApplicationHandle::new);

        // Returns an image for the application
        return (BaseImage<A>) handle.image();
    }

    /** {@inheritDoc} */
    @Override
    public A launch(RunState state, Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer<A> installer = template.newInstaller(BuildGoal.LAUNCH, wirelets);

        // Build the application
        ApplicationHandle<?, ?> handle = installer.install(assembly, ApplicationHandle::new);

        // Launch the application
        ApplicationLaunchContext aic = ApplicationLaunchContext.launch(state, handle, null);

        // Create and return an instance of the application interface
        return template.newHolder(aic);
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer<A> installer = template.newInstaller(BuildGoal.MIRROR, wirelets);

        // Build the application
        ApplicationHandle<?, ?> handle = installer.install(assembly, ApplicationHandle::new);

        // Returns a mirror for the application
        return handle.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public void verify(Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer<A> installer = template.newInstaller(BuildGoal.VERIFY, wirelets);

        // Builds (and verifies) the application
        installer.install(assembly, ApplicationHandle::new);
    }

    static <A> BootstrapApp<A> fromTemplate(PackedApplicationTemplate<A> template) {
        // Create new installer for the bootstrap app
        PackedApplicationInstaller<A> installer = PackedApplicationTemplate.newBootstrapAppInstaller();

        // We need a an assembly to build the (bootstrap) application
        BootstrapAppAssembly assembly = new BootstrapAppAssembly(template);

        // Build the bootstrap application
        installer.buildApplication(assembly);

        // Create the application template.
        PackedApplicationTemplate<A> t = new PackedApplicationTemplate<>(template.guestClass(), null, template.containerTemplate(), assembly.mh);
        return new PackedBootstrapApp<>(t);
    }

    /** The assembly responsible for building the bootstrap app. */
    private static class BootstrapAppAssembly extends BuildableAssembly {

        private MethodHandle mh = ApplicationLaunchContext.EMPTY_MH;

        private final PackedApplicationTemplate<?> template;

        private BootstrapAppAssembly(PackedApplicationTemplate<?> template) {
            this.template = template;
        }

        /** {@inheritDoc} */
        @Override
        protected void build() {
            if (template.guestClass() == Void.class) {
                return;
            }
            BaseExtension base = assembly().containerRoot().use(BaseExtension.class);
            ExtensionSetup es = ExtensionSetup.crack(base);

            // Create a new installer
            BeanTemplate.Installer installer = PackedApplicationTemplate.GB.newInstaller(es, es.container.assembly);

            // Install it (code is shared with App-On-App)
            template.installGuestBean(installer, m -> mh = m);
        }
    }
}
