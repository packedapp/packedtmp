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

import app.packed.application.ApplicationInfo.ApplicationBuildType;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.BuildAction;
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

    public ComposerUserRealmSetup(PackedApplicationDriver<?> applicationDriver, BuildAction<?> consumer, Wirelet[] wirelets) {
        this.consumer = requireNonNull(consumer);
        this.application = new ApplicationSetup(applicationDriver, ApplicationBuildType.INSTANCE, this, wirelets);
    }

    public <C extends AbstractComposer> void build(C composer) {
        ContainerConfiguration componentConfiguration = new PackedContainerDriver(null).toConfiguration(application.container);

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
}
