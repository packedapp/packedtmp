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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import app.packed.application.ApplicationLocal;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.build.BuildGoal;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.container.PackedContainerKind;
import internal.app.packed.container.PackedContainerTemplate;
import internal.app.packed.context.publish.ContextTemplate;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public record PackedApplicationTemplate<A>(Class<?> guestClass, Op<?> op, PackedContainerTemplate containerTemplate, MethodHandle applicationLauncher)
        implements ApplicationTemplate<A> {

    static final ContextTemplate GB_CIT = ContextTemplate.of(MethodHandles.lookup(), ApplicationLaunchContext.class, ApplicationLaunchContext.class);

    static final OperationTemplate GB_CON = OperationTemplate.raw().reconfigure(c -> c.inContext(GB_CIT).returnTypeObject());

    public static final PackedBeanTemplate GB = new PackedBeanTemplate(BeanKind.UNMANAGED).withOperationTemplate(GB_CON);

    public PackedApplicationTemplate(Class<?> guestClass, PackedContainerTemplate containerTemplate) {
        this(guestClass, null, containerTemplate);
    }

    public PackedApplicationTemplate(Class<?> guestClass, Op<?> op, PackedContainerTemplate containerTemplate) {
        this(guestClass, op, containerTemplate, null);
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
        BeanHandle<BeanConfiguration> h;
        if (guestClass() == Void.class) {
            return;
        }
        if (op() == null) {
            h = installer.install(guestClass(), BeanHandle::new);
        } else {
            h = installer.install((Op<?>) op(), BeanHandle::new);
        }
        h.lifetimeOperations().get(0).generateMethodHandleOnCodegen(m -> {
            if (guestClass() == Void.class) {
                // Produces null always. Expected signature BootstrapApp<Void>
                m = MethodHandles.empty(MethodType.methodType(Object.class, ApplicationLaunchContext.class));
            } else {
                m = m.asType(m.type().changeReturnType(Object.class));
            }
            assigner.accept(m);
        });
    }

    // Skal ikke baade have det paa install metoden og den her syntes jeg
    @Override
    public PackedApplicationInstaller<A> newInstaller(BuildGoal goal, Wirelet... wirelets) {
        PackedApplicationInstaller<A> installer = new PackedApplicationInstaller<>(this, goal);
        installer.container.processBuildWirelets(wirelets);
        return installer;
    }

    /** {@inheritDoc} */
    @Override
    public BootstrapApp<A> newBootstrapApp() {
        return PackedBootstrapApp.fromTemplate(this);
    }

    public ApplicationTemplate<A> configure(Consumer<? super Configurator> configure) {
        PackedApplicationTemplateConfigurator<A> c = new PackedApplicationTemplateConfigurator<>(this);
        configure.accept(c);
        if (c.pbt.containerTemplate == null) {
            throw new IllegalStateException("Must specify a container template for the root container");
        }
        return c.pbt;
    }

    public final static class PackedApplicationTemplateConfigurator<A> implements ApplicationTemplate.Configurator {

        public PackedApplicationTemplate<A> pbt;

        public PackedApplicationTemplateConfigurator(PackedApplicationTemplate<A> pbt) {
            this.pbt = pbt;
        }

        /** {@inheritDoc} */
        @Override
        public PackedApplicationTemplateConfigurator<A> container(ContainerTemplate template) {
            this.pbt = new PackedApplicationTemplate<>(pbt.guestClass(), pbt.op(), (PackedContainerTemplate) template, pbt.applicationLauncher);
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
        public Configurator container(Consumer<? super ContainerTemplate.Configurator> configure) {
            if (pbt.containerTemplate == null) {
                pbt = container(ContainerTemplate.GATEWAY).pbt;
            }
            PackedContainerTemplate pct = PackedContainerTemplate.configure(pbt.containerTemplate, configure);
            return container(pct);
        }

    }
}
