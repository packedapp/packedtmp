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
import internal.app.packed.util.ThrowableUtil;

/** Implementation of {@link ApplicationTemplate}. */
public record PackedApplicationTemplate<A>(Class<?> guestClass, @Nullable Op<?> op,
        Function<? super Installer<A>, ? extends ApplicationHandle<?, A>> handleFactory, PackedContainerTemplate containerTemplate, Set<String> componentTags,
        MethodHandle applicationLauncher) implements ApplicationTemplate<A> {

    static final ContextTemplate GB_CIT = ContextTemplate.of(ApplicationLaunchContext.class, c -> {});

    static final ContextTemplate GB_HIT = ContextTemplate.of(ComponentHostContext.class, c->c.implementationClass(PackedComponentHostContext.class).bindAsConstant());

    static final OperationTemplate GB_CON = OperationTemplate.raw().reconfigure(c -> c.inContext(GB_CIT).inContext(GB_HIT).returnTypeObject());

    public static final PackedBeanTemplate GB = new PackedBeanTemplate(BeanKind.UNMANAGED).withOperationTemplate(GB_CON);

    public PackedApplicationTemplate(Class<?> guestClass, PackedContainerTemplate containerTemplate) {
        this(guestClass, null, containerTemplate);
    }

    public PackedApplicationTemplate(Class<?> guestClass, Op<?> op, PackedContainerTemplate containerTemplate) {
        this(guestClass, op, ApplicationHandle::new, containerTemplate, Set.of(), null);
    }

    /**
     * Create a new application interface using the specified launch context.
     *
     * @param context
     *            the launch context to use for creating the application instance
     * @return the new application instance
     */
    public A newHolder(ApplicationLaunchContext context) {
        try {
            return (A) applicationLauncher.invokeExact(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    // De her er ikke public fordi de kun kan bruges fra Bootstrap App
    // Hvor ikke specificere en template direkte. Fordi den kun skal bruges en gang
    // Til at lave selve bootstrap applicationene.
    static PackedApplicationTemplate<Void> ROOT_MANAGED = null;

    static PackedApplicationTemplate<Void> ROOT_UNMANAGED = null;

    public static final PackedApplicationTemplate<?> BOOTSTRAP_APP = new PackedApplicationTemplate<>(PackedApplicationTemplate.class,
            new PackedContainerTemplate(PackedContainerKind.BOOTSTRAP_APPLICATION, PackedBootstrapApp.class));

    public static <A> PackedApplicationInstaller<A> newBootstrapAppInstaller() {
        return new PackedApplicationInstaller<>(PackedApplicationTemplate.BOOTSTRAP_APP, BuildGoal.LAUNCH);
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
//            if (guestClass() == Void.class) {
//                // Produces null always. Expected signature BootstrapApp<Void>
//                m = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class));
//            } else {
                m = m.asType(m.type().changeReturnType(Object.class));
//            }
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
    public PackedApplicationInstaller<A> newInstaller(BuildGoal goal, Wirelet... wirelets) {
        PackedApplicationInstaller<A> installer = new PackedApplicationInstaller<>(this, goal);
        installer.containerInstaller.processBuildWirelets(wirelets);
        return installer;
    }

    public ApplicationTemplate<A> configure(Consumer<? super Configurator<A>> configure) {
        PackedApplicationTemplateConfigurator<A> c = new PackedApplicationTemplateConfigurator<>(this);
        configure.accept(c);
        if (c.pbt.containerTemplate == null) {
            throw new IllegalStateException("Must specify a container template for the root container");
        }
        return c.pbt;
    }

    /** {@inheritDoc} */
    @Override
    public Function<? super Installer<A>, ? extends ApplicationHandle<?, A>> bootstrapAppHandleFactory() {
        return handleFactory;
    }

    public final static class PackedApplicationTemplateConfigurator<A> implements ApplicationTemplate.Configurator<A> {

        public PackedApplicationTemplate<A> pbt;

        public PackedApplicationTemplateConfigurator(PackedApplicationTemplate<A> pbt) {
            this.pbt = pbt;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator<A> bootstrapAppHandleFactory(Function<? super Installer<A>, ? extends ApplicationHandle<?, A>> handleFactory) {
            requireNonNull(handleFactory);
            this.pbt = new PackedApplicationTemplate<>(pbt.guestClass(), pbt.op(), handleFactory, pbt.containerTemplate(), pbt.componentTags,
                    pbt.applicationLauncher);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator<A> componentTag(String... tags) {
            this.pbt = new PackedApplicationTemplate<>(pbt.guestClass(), pbt.op(), pbt.handleFactory, pbt.containerTemplate(),
                    ComponentTagHolder.copyAndAdd(pbt.componentTags, tags), pbt.applicationLauncher);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public Configurator<A> rootContainer(Consumer<? super ContainerTemplate.Configurator> configure) {
            if (pbt.containerTemplate == null) {
                pbt = rootContainer(ContainerTemplate.GATEWAY).pbt;
            }
            PackedContainerTemplate pct = PackedContainerTemplate.configure(pbt.containerTemplate, configure);
            return rootContainer(pct);
        }

        /** {@inheritDoc} */
        @Override
        public PackedApplicationTemplateConfigurator<A> rootContainer(ContainerTemplate template) {
            this.pbt = new PackedApplicationTemplate<>(pbt.guestClass(), pbt.op(), pbt.handleFactory(), (PackedContainerTemplate) template, pbt.componentTags,
                    pbt.applicationLauncher);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public <T> Configurator<A> setLocal(ApplicationBuildLocal<T> local, T value) {
            return null;
        }
    }
}
