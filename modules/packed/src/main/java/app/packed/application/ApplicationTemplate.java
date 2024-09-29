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
package app.packed.application;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.assembly.Assembly;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.operation.Op;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.PackedApplicationTemplateConfigurator;

/**
 * A template for creating new applications.
 */
public sealed interface ApplicationTemplate<A> permits PackedApplicationTemplate {

    /**
     * {@return an application handle factory that is used if the template is used with a bootstrap app}
     * <p>
     * This method will only be called if used for a {@link BootstrapApp}.
     *
     * @see Configurator#bootstrapAppHandleFactory(Function)
     */
    Function<? super ApplicationTemplate.Installer<A>, ? extends ApplicationHandle<?, A>> bootstrapAppHandleFactory();

    /**
     * @param <A>
     * @param hostClass
     * @param configurator
     * @return the new template
     *
     * @throws IllegalStateException
     *             if attempting to create an application template without {@link Configurator#container(Consumer) setting}
     *             a container template for the application's root container
     */
    static <A> ApplicationTemplate<A> of(Class<A> hostClass, Consumer<? super Configurator<A>> configurator) {
        return new PackedApplicationTemplate<A>(hostClass, null).configure(configurator);
    }

    static ApplicationTemplate<Void> of(Consumer<? super Configurator<Void>> configure) {
        return of(Void.class, configure);
    }

    static <A> ApplicationTemplate<A> of(Op<A> hostOp, Consumer<? super Configurator<A>> configurator) {
        Class<?> type = hostOp.type().returnRawType();
        return new PackedApplicationTemplate<A>(type, hostOp, null).configure(configurator);
    }

    /** A configuration object for creating an {@link ApplicationTemplate}. */
    sealed interface Configurator<A> permits PackedApplicationTemplateConfigurator {

        /**
         * Registers a handle factory for use with bootstrapped applications.
         *
         * @param handleFactory
         *            the handle factory
         * @return this configurator
         */
        Configurator<A> bootstrapAppHandleFactory(Function<? super ApplicationTemplate.Installer<A>, ? extends ApplicationHandle<?, A>> handleFactory);

        /**
         * Add the specified tags to the application.
         *
         * @param tags
         *            the tags to add
         * @return this configurator
         * @see ApplicationMirror#componentTags()
         * @see ApplicationHandle#componentTag(String...)
         * @see ApplicationConfiguration#componentTag(String...)
         */
        Configurator<A> componentTag(String... tags);

//        // Mark the application as removable()
//        Configurator<A> removeable();

        /**
         * Configures the container template that should be used for the root container of the application.
         *
         * @param configure
         *            configure a new template
         * @return this configurator
         * @see #rootContainer(ContainerTemplate)
         */
        Configurator<A> rootContainer(Consumer<? super ContainerTemplate.Configurator> configure);

        /**
         * Configures the container template that should be used for the root container of the application.
         *
         * @param template
         *            a template for the root container of the application
         * @return this configurator
         * @see #rootContainer(Consumer)
         */
        Configurator<A> rootContainer(ContainerTemplate template);

        <T> Configurator<A> setLocal(ApplicationBuildLocal<T> local, T value);
    }

    /** An installer for applications. */
    sealed interface Installer<A> permits PackedApplicationInstaller {

        /**
         * Add the specified tags to the application.
         *
         * @param tags
         *            the tags to add
         * @return this configurator
         * @see ApplicationMirror#componentTags()
         * @see ApplicationHandle#componentTag(String...)
         * @see ApplicationConfiguration#componentTag(String...)
         */
        Installer<A> componentTag(String... tags);

        <H extends ApplicationHandle<?, A>> H install(Assembly assembly, Function<? super ApplicationTemplate.Installer<A>, H> handleFactory,
                Wirelet... wirelets);

        <T> Installer<A> setLocal(ApplicationBuildLocal<T> local, T value);
    }
}

// lazy start single application. and make the following services available
// Her taenker jeg fx ElasticSearch
// Som kunne vaere en kaempe kompliceret application.
// Som vi gerne vil starte foer hoved applicationen

// prestartIt. And then block operations?

// Bootstrap App har ogsaa en template
