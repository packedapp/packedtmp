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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.application.BuildTaskGoal;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.BuildAction;
import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationDriver;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public final class ComposerUserRealmSetup extends UserRealmSetup {

    /** A handle that can invoke {@link AbstractComposer#doBuild(ContainerConfiguration, BuildAction)}. */
    private static final MethodHandle MH_COMPOSER_DO_COMPOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), AbstractComposer.class, "doBuild",
            void.class, ContainerConfiguration.class, BuildAction.class);

    private final BuildAction<?> consumer;

    /** The application we are building. */
    public final ApplicationSetup application;

    private final AbstractComposer composer;

    private final Class<? extends ComposerAssembly> assemblyClass;

    public ComposerUserRealmSetup(PackedApplicationDriver<?> driver, Class<? extends ComposerAssembly> assemblyClass, AbstractComposer composer,
            BuildAction<?> action, Wirelet[] wirelets) {
        this.composer = requireNonNull(composer, "composer is null");
        this.consumer = requireNonNull(action, "action is null");
        this.application = new ApplicationSetup(driver, BuildTaskGoal.LAUNCH, this, wirelets);
        // Maybe it needs to be a proper subclass?
        this.assemblyClass = requireNonNull(assemblyClass, "assemblyClass is null");
        if (!ComposerAssembly.class.isAssignableFrom(assemblyClass)) {
            throw new ClassCastException(assemblyClass + " must be assignable to " + ComposerAssembly.class);
        }
    }

    public void build() {
        ContainerConfiguration componentConfiguration = new PackedContainerHandle(null).toConfiguration(application.container);

        // Invoke AbstractComposer#doBuild which in turn will invoke consumer.accept
        try {
            MH_COMPOSER_DO_COMPOSE.invoke(composer, componentConfiguration, consumer);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the realm, if the application has been built successfully (no exception was thrown)
        close();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup container() {
        return application.container;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> realmType() {
        return consumer.getClass();
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends Assembly> assemblyClass() {
        return assemblyClass;
    }
}
