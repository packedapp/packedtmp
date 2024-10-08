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

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.container.ContainerTemplate;
import app.packed.operation.Op;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.PackedApplicationTemplateConfigurator;

/**
 * A template for creating new applications. It is typically only used by {@link app.packed.extension.Extension
 * extensions} and normal users will rarely have any use for it.
 *
 * @param <H>
 *            the type of application handles the template creates
 */
public sealed interface ApplicationTemplate<H extends ApplicationHandle<?, ?>> permits PackedApplicationTemplate {

    /**
     * Create a new application template, that will use
     *
     * <p>
     * This method calls {@link #of(Class, Consumer, Class, Function)} with
     * {@code ApplicationHandle.class, ApplicationHandle::new} as respectively the handler class and the handler factory.
     *
     * @param <A>
     *            the type of host class that are returned when launching the application.
     * @param hostClass
     * @param configurator
     * @return the new template
     *
     * @throws IllegalStateException
     *             if attempting to create an application template without {@link Configurator#container(Consumer) setting}
     *             a container template for the application's root container
     */
    static <I> ApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> of(Class<I> hostClass, Consumer<? super Configurator> configurator) {
        return of(hostClass, configurator, ApplicationHandle.class, ApplicationHandle::new);
    }

    static <I, H extends ApplicationHandle<I, ?>> ApplicationTemplate<H> of(Class<I> hostClass, Consumer<? super Configurator> configurator,
            Class<? super H> handleClass, Function<? super ApplicationInstaller<H>, ? extends H> handleFactory) {
        return new PackedApplicationTemplate<H>(hostClass, null, handleClass, handleFactory, null, Set.of()).configure(configurator);
    }

    static <I> ApplicationTemplate<ApplicationHandle<I, ApplicationConfiguration>> of(Op<I> hostOp, Consumer<? super Configurator> configurator) {
        return of(hostOp, configurator, ApplicationHandle.class, ApplicationHandle::new);
    }

    static <I, H extends ApplicationHandle<I, ?>> ApplicationTemplate<H> of(Op<I> hostOp, Consumer<? super Configurator> configurator,
            Class<? super H> handleClass, Function<? super ApplicationInstaller<H>, ? extends H> handleFactory) {
        Class<?> type = hostOp.type().returnRawType();
        return new PackedApplicationTemplate<>(type, hostOp, handleClass, handleFactory, null, Set.of()).configure(configurator);
    }

    /** A configuration object for creating an {@link ApplicationTemplate}. */
    sealed interface Configurator permits PackedApplicationTemplateConfigurator {

        /**
         * Add the specified tag(s) to the application.
         *
         * @param tags
         *            the tag(s) to add
         * @return this configurator
         * @see ApplicationMirror#componentTags()
         * @see ApplicationHandle#componentTag(String...)
         * @see ApplicationConfiguration#componentTag(String...)
         */
        Configurator componentTag(String... tags);

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
}
