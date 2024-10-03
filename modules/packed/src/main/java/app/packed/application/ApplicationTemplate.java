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
 *
 * @param <H>
 *            the type of application handles the template creates
 */
public sealed interface ApplicationTemplate<H extends ApplicationHandle<?, ?>> permits PackedApplicationTemplate {

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
    static <A> ApplicationTemplate<ApplicationHandle<A, ApplicationConfiguration>> of(Class<A> hostClass, Consumer<? super Configurator> configurator) {
        return of(hostClass, configurator, ApplicationHandle::new);
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationTemplate<H> of(Class<A> hostClass, Consumer<? super Configurator> configurator,
            Function<? super ApplicationTemplate.Installer<H>, ? extends H> handleFactory) {
        return new PackedApplicationTemplate<H>(hostClass, handleFactory, null).configure(configurator);
    }

    static <A> ApplicationTemplate<ApplicationHandle<A, ApplicationConfiguration>> of(Op<A> hostOp, Consumer<? super Configurator> configurator) {
        return of(hostOp, configurator, ApplicationHandle::new);
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationTemplate<H> of(Op<A> hostOp, Consumer<? super Configurator> configurator,
            Function<? super ApplicationTemplate.Installer<H>, ? extends H> handleFactory) {
        Class<?> type = hostOp.type().returnRawType();
        return new PackedApplicationTemplate<>(type, hostOp, handleFactory, null).configure(configurator);
    }

    /** A configuration object for creating an {@link ApplicationTemplate}. */
    sealed interface Configurator permits PackedApplicationTemplateConfigurator {

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
        Configurator componentTag(String... tags);

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
        Configurator rootContainer(Consumer<? super ContainerTemplate.Configurator> configure);

        /**
         * Configures the container template that should be used for the root container of the application.
         *
         * @param template
         *            a template for the root container of the application
         * @return this configurator
         * @see #rootContainer(Consumer)
         */
        Configurator rootContainer(ContainerTemplate<?> template);

        <T> Configurator setLocal(ApplicationBuildLocal<T> local, T value);
    }

    /** An installer for applications. */
    sealed interface Installer<H extends ApplicationHandle<?, ?>> permits PackedApplicationInstaller {

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
        Installer<H> componentTag(String... tags);

        /**
         * <p>
         * The handle that is returned will be non-configurable.
         *
         * @param assembly
         * @param wirelets
         * @return
         */
        H install(Assembly assembly, Wirelet... wirelets);

        Installer<H> named(String name);

        <T> Installer<H> setLocal(ApplicationBuildLocal<T> local, T value);
    }
}

// lazy start single application. and make the following services available
// Her taenker jeg fx ElasticSearch
// Som kunne vaere en kaempe kompliceret application.
// Som vi gerne vil starte foer hoved applicationen

// prestartIt. And then block operations?

// Bootstrap App har ogsaa en template
