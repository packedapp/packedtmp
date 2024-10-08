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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.application.ApplicationBuildLocal;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.build.BuildGoal;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.operation.Op;
import app.packed.util.Nullable;
import internal.app.packed.component.ComponentTagHolder;
import internal.app.packed.container.PackedContainerTemplate;

/** Implementation of {@link ApplicationTemplate}. */
public record PackedApplicationTemplate<H extends ApplicationHandle<?, ?>>(
        Class<?> guestClass,
        @Nullable Op<?> op, Class<? super H> handleClass,
        Function<? super ApplicationInstaller<H>, ? extends ApplicationHandle<?, ?>> handleFactory,
        PackedContainerTemplate<?> containerTemplate,
        Set<String> componentTags) implements ApplicationTemplate<H> {

    /**
     * Creates a new {@link ApplicationInstaller} from this template.
     * <p>
     * NOTE: this method must not be on {@link ApplicationTemplate}.
     *
     * @param goal
     *            the build goal
     * @param wirelets
     *            optional wirelets
     * @return a new application installer
     */
    public PackedApplicationInstaller<H> newInstaller(@Nullable ApplicationInstallingSource source, BuildGoal goal, MethodHandle launcher,
            Wirelet... wirelets) {
        PackedApplicationInstaller<H> installer = new PackedApplicationInstaller<>(this, launcher, goal);
        installer.containerInstaller.processBuildWirelets(wirelets);
        return installer;
    }

    public ApplicationTemplate<H> configure(Consumer<? super Configurator> configure) {
        PackedApplicationTemplateConfigurator<H> c = new PackedApplicationTemplateConfigurator<>();
        c.t = this;
        configure.accept(c);
        if (c.t.containerTemplate == null) {
            throw new IllegalStateException("Must specify a container template for the root container");
        }
        return c.t;
    }

    /** Implementation of {@link ApplicationTemplate.Configurator} */
    public final static class PackedApplicationTemplateConfigurator<H extends ApplicationHandle<?, ?>> implements ApplicationTemplate.Configurator {

        private PackedApplicationTemplate<H> t;

        /** {@inheritDoc} */
        @Override
        public Configurator componentTag(String... tags) {
            this.t = new PackedApplicationTemplate<>(t.guestClass(), t.op(), t.handleClass, t.handleFactory, t.containerTemplate(),
                    ComponentTagHolder.copyAndAdd(t.componentTags, tags));
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator rootContainer(Consumer<? super ContainerTemplate.Configurator> configure) {
            if (t.containerTemplate == null) {
                t = rootContainer(ContainerTemplate.GATEWAY).t;
            }
            PackedContainerTemplate<?> pct = PackedContainerTemplate.configure(t.containerTemplate, configure);
            return rootContainer(pct);
        }

        /** {@inheritDoc} */
        @Override
        public PackedApplicationTemplateConfigurator<H> rootContainer(ContainerTemplate<?> template) {
            this.t = new PackedApplicationTemplate<>(t.guestClass(), t.op(), t.handleClass, t.handleFactory(), (PackedContainerTemplate<?>) template,
                    t.componentTags);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public <T> Configurator setLocal(ApplicationBuildLocal<T> local, T value) {
            throw new UnsupportedOperationException();
        }
    }

    public interface ApplicationInstallingSource {}
}
