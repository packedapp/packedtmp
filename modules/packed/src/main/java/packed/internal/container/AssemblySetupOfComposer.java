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
package packed.internal.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.application.ApplicationInfo.ApplicationBuildType;
import app.packed.container.Assembly;
import app.packed.container.AbstractComposer;
import app.packed.container.ComposerAction;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import packed.internal.application.ApplicationSetup;
import packed.internal.application.PackedApplicationDriver;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class AssemblySetupOfComposer extends AssemblySetup {

    /** A handle that can invoke {@link Assembly#doBuild()}. Is here because I have no better place to put it. */
    private static final MethodHandle MH_COMPOSER_DO_COMPOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), AbstractComposer.class, "doBuild", void.class,
            ContainerConfiguration.class, ComposerAction.class);

    final ContainerConfiguration componentConfiguration;

    final ComposerAction<?> composer;

    public final ApplicationSetup application;

    // Den giver kun mening for assemblies...
    /** The root component of this realm. */
    public final ContainerSetup container;
    
    public AssemblySetupOfComposer(PackedApplicationDriver<?> applicationDriver, ComposerAction<?> composer, Wirelet[] wirelets) {
        this.composer = composer;
        this.application = new ApplicationSetup(applicationDriver, ApplicationBuildType.INSTANCE, this, wirelets);
        this.container = application.container;
        this.componentConfiguration = new PackedContainerDriver(container).toConfiguration(container);
        wireCommit(container);
    }

    public <C extends AbstractComposer> void build(C composer, ComposerAction<? super C> consumer) {
        // Invoke Assembly::doBuild which in turn will invoke Assembly::build
        // This will recursively call down through any sub-containers that are linked

        // Invoke Composer#doCompose which in turn will invoke consumer.accept
        try {
            MH_COMPOSER_DO_COMPOSE.invoke(composer, componentConfiguration, consumer);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the realm, if the application has been built successfully (no exception was thrown)
        closeRealm();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> realmType() {
        return composer.getClass();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup container() {
        return container;
    }
}
