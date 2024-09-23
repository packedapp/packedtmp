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
 *
 */
public sealed interface ApplicationTemplate<A> permits PackedApplicationTemplate {

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

    /**
     * {@return an application handle factory that is used if the template is used with a bootstrap app}
     * <p>
     * If the template is used to create an application that is non-bootstrap app this method will not be called.
     */
    Function<? super ApplicationTemplate.Installer<A>, ? extends ApplicationHandle<?, A>> bootstrapHandleFactory();

    sealed interface Configurator<A> permits PackedApplicationTemplateConfigurator {

        Configurator<A> bootstrapHandleFactory(Function<? super ApplicationTemplate.Installer<A>, ? extends ApplicationHandle<?, A>> handleFactory);

        /**
         * Add the specified tags to the application
         *
         * @param tags
         *            the tags to add
         * @return this configurator
         */
        Configurator<A> componentTag(String... tags);

        Configurator<A> container(Consumer<? super ContainerTemplate.Configurator> configure);

        // Configuration of the root container
        Configurator<A> container(ContainerTemplate template);

        // Mark the application as removable()
        Configurator<A> removeable();

        <T> Configurator<A> setLocal(ApplicationBuildLocal<T> local, T value);
    }

    /** An installer for applications. */
    sealed interface Installer<A> permits PackedApplicationInstaller {

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
