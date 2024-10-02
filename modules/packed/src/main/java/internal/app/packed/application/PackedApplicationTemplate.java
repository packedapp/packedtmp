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
import app.packed.application.ApplicationTemplate;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.build.BuildGoal;
import app.packed.component.guest.ComponentHostContext;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.context.ContextTemplate;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import app.packed.util.Nullable;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.component.ComponentTagHolder;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.context.PackedComponentHostContext;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;

/** Implementation of {@link ApplicationTemplate}. */
public record PackedApplicationTemplate<A, H extends ApplicationHandle<A, ?>>(Class<?> guestClass, @Nullable Op<?> op,
        Function<? super Installer<H>, ? extends ApplicationHandle<A, ?>> handleFactory, PackedContainerTemplate<?> containerTemplate, Set<String> componentTags)
        implements ApplicationTemplate<A, H> {

    static final ContextTemplate GB_CIT = ContextTemplate.of(ApplicationLaunchContext.class, c -> {});

    static final ContextTemplate GB_HIT = ContextTemplate.of(ComponentHostContext.class,
            c -> c.implementationClass(PackedComponentHostContext.class).bindAsConstant());

    static final OperationTemplate GB_CON = OperationTemplate.raw().reconfigure(c -> c.inContext(GB_CIT).inContext(GB_HIT).returnTypeObject());

    public static final PackedBeanTemplate GB = new PackedBeanTemplate(BeanKind.UNMANAGED).withOperationTemplate(GB_CON);

    public PackedApplicationTemplate(Class<?> guestClass, Function<? super Installer<H>, ? extends ApplicationHandle<A, ?>> handleFactory,
            PackedContainerTemplate<?> containerTemplate) {
        this(guestClass, null, handleFactory, containerTemplate);
    }

    public PackedApplicationTemplate(Class<?> guestClass, Op<?> op, Function<? super Installer<H>, ? extends ApplicationHandle<A, ?>> handleFactory,
            PackedContainerTemplate<?> containerTemplate) {
        this(guestClass, op, handleFactory, containerTemplate, Set.of());
    }

    // De her er ikke public fordi de kun kan bruges fra Bootstrap App
    // Hvor ikke specificere en template direkte. Fordi den kun skal bruges en gang
    // Til at lave selve bootstrap applicationene.
    static PackedApplicationTemplate<Void, ?> ROOT_MANAGED = null;

    static PackedApplicationTemplate<Void, ?> ROOT_UNMANAGED = null;

    public static final PackedApplicationTemplate<?, ?> BOOTSTRAP_APP = new PackedApplicationTemplate<>(PackedApplicationTemplate.class, ApplicationHandle::new,
            new PackedContainerTemplate<>(PackedContainerKind.BOOTSTRAP_APPLICATION, PackedBootstrapApp.class));

    public static <A> PackedApplicationInstaller<?> newBootstrapAppInstaller() {
        return new PackedApplicationInstaller<>(PackedApplicationTemplate.BOOTSTRAP_APP, null, BuildGoal.LAUNCH);
    }

    public void installGuestBean(BeanTemplate.Installer installer, Consumer<? super MethodHandle> assigner) {
        if (guestClass() == Void.class) {
            return;
        }
        GuestBeanHandle h;
        if (op() == null) {
            h = installer.install(guestClass(), GuestBeanHandle::new);
        } else {
            h = installer.install((Op<?>) op(), GuestBeanHandle::new);
        }

        h.lifetimeOperations().get(0).generateMethodHandleOnCodegen(m -> {
            m = m.asType(m.type().changeReturnType(Object.class));
            assigner.accept(m);
        });
    }

    /**
     * Creates a new {@link Installer} from this template.
     * <p>
     * NOTE: this method must not be on {@link ApplicationTemplate}.
     *
     * @param goal
     *            the build goal
     * @param wirelets
     *            optional wirelets
     * @return a new application installer
     */
    public PackedApplicationInstaller<H> newInstaller(BuildGoal goal, MethodHandle launcher, Wirelet... wirelets) {
        PackedApplicationInstaller<H> installer = new PackedApplicationInstaller<>(this, launcher, goal);
        installer.containerInstaller.processBuildWirelets(wirelets);
        return installer;
    }

    public ApplicationTemplate<A, H> configure(Consumer<? super Configurator> configure) {
        PackedApplicationTemplateConfigurator<A, H> c = new PackedApplicationTemplateConfigurator<>();
        c.t = this;
        configure.accept(c);
        if (c.t.containerTemplate == null) {
            throw new IllegalStateException("Must specify a container template for the root container");
        }
        return c.t;
    }

    /** Implementation of {@link ApplicationTemplate.Configurator} */
    public final static class PackedApplicationTemplateConfigurator<A, H extends ApplicationHandle<A, ?>> implements ApplicationTemplate.Configurator {

        private PackedApplicationTemplate<A, H> t;

        /** {@inheritDoc} */
        @Override
        public Configurator componentTag(String... tags) {
            this.t = new PackedApplicationTemplate<A, H>(t.guestClass(), t.op(), t.handleFactory, t.containerTemplate(),
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
        public PackedApplicationTemplateConfigurator<A, H> rootContainer(ContainerTemplate<?> template) {
            this.t = new PackedApplicationTemplate<>(t.guestClass(), t.op(), t.handleFactory(), (PackedContainerTemplate<?>) template, t.componentTags);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public <T> Configurator setLocal(ApplicationBuildLocal<T> local, T value) {
            throw new UnsupportedOperationException();
        }
    }
}
