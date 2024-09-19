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

import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

import app.packed.application.ApplicationLocal;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import internal.app.packed.util.ThrowableUtil;
import sandbox.extension.container.ContainerTemplate;

/**
 *
 */
public record PackedApplicationTemplate(PackedContainerTemplate containerTemplate, MethodHandle applicationLauncher) implements ApplicationTemplate {

    public PackedApplicationTemplate(PackedContainerTemplate containerTemplate) {
        this(containerTemplate, null);
    }

    /**
     * Create a new application interface using the specified launch context.
     *
     * @param context
     *            the launch context to use for creating the application instance
     * @return the new application instance
     */
    public Object newHolder(ApplicationLaunchContext context) {
        try {
            return applicationLauncher.invokeExact(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    // De her er ikke public fordi de kun kan bruges fra Bootstrap App
    // Hvor ikke specificere en template direkte. Fordi den kun skal bruges en gang
    // Til at lave selve bootstrap applicationene.
    static PackedApplicationTemplate ROOT_MANAGED = null;

    static PackedApplicationTemplate ROOT_UNMANAGED = null;

    private static final PackedApplicationTemplate BOOTSTRAP_APP = new PackedApplicationTemplate(
            new PackedContainerTemplate(PackedContainerKind.BOOTSTRAP_APPLICATION, BootstrapApp.class));

    public static PackedApplicationInstaller newBootstrapAppInstaller() {
        return new PackedApplicationInstaller(PackedApplicationTemplate.BOOTSTRAP_APP, BuildGoal.LAUNCH);
    }

    // Skal ikke baade have det paa install metoden og den her syntes jeg
    @Override
    public PackedApplicationInstaller newInstaller(BuildGoal goal, Wirelet... wirelets) {
        PackedApplicationInstaller installer = new PackedApplicationInstaller(this, goal);
        installer.container.processBuildWirelets(wirelets);
        return installer;
    }

    public final static class PackedApplicationTemplateConfigurator implements ApplicationTemplate.Configurator {

        public PackedApplicationTemplate pbt;

        public PackedApplicationTemplateConfigurator(PackedApplicationTemplate pbt) {
            this.pbt = pbt;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator container(ContainerTemplate template) {
            this.pbt = new PackedApplicationTemplate((PackedContainerTemplate) template, pbt.applicationLauncher);
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator removeable() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public <T> Configurator setLocal(ApplicationLocal<T> local, T value) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator container(Consumer<? super sandbox.extension.container.ContainerTemplate.Configurator> configure) {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator guest(Class<?> clazz) {
            return null;
        }
    }
}
