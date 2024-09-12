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
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.PackedApplicationTemplateConfigurator;
import sandbox.extension.container.ContainerTemplate;

/**
 *
 */
// Her taenker jeg fx ElasticSearch
// Som kunne vaere en kaempe kompliceret application.
// Som vi gerne vil starte foer hoved applicationen

// prestartIt. And then block operations?

// Bootstrap App har ogsaa en template
public sealed interface ApplicationTemplate permits PackedApplicationTemplate {

    // Application Started before or after

    // lazy start single application. and make the following services available

    static ApplicationTemplate of(Consumer<? super Configurator> configure) {
        PackedApplicationTemplateConfigurator c = new PackedApplicationTemplateConfigurator(new PackedApplicationTemplate(null));
        configure.accept(c);
        return c.template();
    }

    sealed interface Configurator permits PackedApplicationTemplateConfigurator {

        // Configuration of the root container
        Configurator container(ContainerTemplate template);

        Configurator container(Consumer<? super ContainerTemplate.Configurator> configure);

        // Mark the application as removable()
        Configurator removeable();

        <T> Configurator setLocal(ApplicationLocal<T> local, T value);
    }

    sealed interface Installer permits PackedApplicationInstaller {
        Configurator hostedBy(BeanConfiguration bean);

        <T> Configurator setLocal(ApplicationLocal<T> local, T value);

        /**
         * Sets a supplier that creates a special container mirror instead of the generic {@code ContainerMirror} when
         * requested.
         *
         * @param supplier
         *            the supplier used to create the application mirror
         * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
         *          must be returned
         */
        // What about the container mirror?????
        Installer specializeMirror(Supplier<? extends ApplicationMirror> supplier);
    }

}

// ApplicationBridge