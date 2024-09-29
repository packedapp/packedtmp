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
import internal.app.packed.ValueBased;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;

/** Implementation of {@link BootstrapApp}. */
@ValueBased
public final class PackedBootstrapApp<A> implements BootstrapApp<A> {

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
    public PackedBootstrapApp<A> expectsResult(Class<?> resultType) {
        // Ideen er bootstrapApp.expectsResult(FooBar.class).launch(...);
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public BaseImage<A> imageOf(Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer<A> installer = template.newInstaller(BuildGoal.IMAGE, wirelets);

        // Build the application
        ApplicationHandle<?, ?> handle = installer.install(assembly, template.bootstrapAppHandleFactory());

        // Returns an image for the application
        return (BaseImage<A>) handle.image();
    }

    /** {@inheritDoc} */
    @Override
    public A launch(RunState state, Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer<A> installer = template.newInstaller(BuildGoal.LAUNCH, wirelets);

        // Build the application
        // Okay vi maa jo kunne saette default handle... Fx specificer mirror
        ApplicationHandle<?, ?> handle = installer.install(assembly, template.bootstrapAppHandleFactory());

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
        ApplicationHandle<?, ?> handle = installer.install(assembly, template.bootstrapAppHandleFactory());

        // Returns a mirror for the application
        return handle.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public void verify(Assembly assembly, Wirelet... wirelets) {
        ApplicationTemplate.Installer<A> installer = template.newInstaller(BuildGoal.VERIFY, wirelets);

        // Builds (and verifies) the application
        installer.install(assembly, template.bootstrapAppHandleFactory());
    }

    /**
     * Builds a new bootstrap app for applications represented by the specified template.
     *
     * @param <A>
     * @param template
     *            the template for the type applications that should be bootstrapped
     * @return a new bootstrap app
     */
    public static <A> BootstrapApp<A> of(PackedApplicationTemplate<A> template) {
        // Create new installer for the bootstrap app
        PackedApplicationInstaller<A> installer = PackedApplicationTemplate.newBootstrapAppInstaller();

        // We need a an assembly to build the (bootstrap) application
        BootstrapAppAssembly assembly = new BootstrapAppAssembly(template);

        // Build the bootstrap application
        installer.install(assembly, ApplicationHandle::new);

        // Create the application template.
        PackedApplicationTemplate<A> t = new PackedApplicationTemplate<>(template.guestClass(), null, ApplicationHandle::new, template.containerTemplate(),
                template.componentTags(), assembly.mh);
        return new PackedBootstrapApp<>(t);
    }

    /** The assembly responsible for building the bootstrap app. */
    private static class BootstrapAppAssembly extends BuildableAssembly {

        /** The method handle to launch the application, the empty MH is used if A is Void.class */
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

            // Get the internal BaseExtension, we need this to use a customer BeanTemplate
            BaseExtension base = assembly().containerRoot().use(BaseExtension.class);
            ExtensionSetup es = ExtensionSetup.crack(base);

            // Create a new installer
            BeanTemplate.Installer installer = PackedApplicationTemplate.GB.newInstaller(es, es.container.assembly);

            // Install the guest bean if needed (code is shared with App-On-App)
            template.installGuestBean(installer, m -> mh = m);
        }
    }
}
