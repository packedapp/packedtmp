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
import app.packed.build.BuildGoal;
import app.packed.container.Wirelet;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.PackedApplicationTemplateConfigurator;
import sandbox.extension.container.ContainerTemplate;

/**
 *
 */

// Altsaa som jeg ser det kan det vaere vi bliver noedt til at faa den injected
// Paa en eller anden maade bliver vi noedt til at faa noget runtime

public sealed interface ApplicationTemplate permits PackedApplicationTemplate {


    ApplicationTemplate DEFAULT = ApplicationTemplate.of(c -> {});

    ApplicationTemplate UNMANGED = ApplicationTemplate.of(c -> {});

    ApplicationTemplate.Installer newInstaller(BuildGoal goal, Wirelet... wirelets);

    static ApplicationTemplate of(Consumer<? super Configurator> configure) {
        PackedApplicationTemplateConfigurator c = new PackedApplicationTemplateConfigurator(new PackedApplicationTemplate(null));
        configure.accept(c);
        return c.pbt;
    }

    // Application Started before or after

    // lazy start single application. and make the following services available
    // Her taenker jeg fx ElasticSearch
    // Som kunne vaere en kaempe kompliceret application.
    // Som vi gerne vil starte foer hoved applicationen

    // prestartIt. And then block operations?

    // Bootstrap App har ogsaa en template

    sealed interface Configurator permits PackedApplicationTemplateConfigurator {

        Configurator guest(Class<?> clazz);

        Configurator container(Consumer<? super ContainerTemplate.Configurator> configure);

        // Configuration of the root container
        Configurator container(ContainerTemplate template);

        // Mark the application as removable()
        Configurator removeable();

        <T> Configurator setLocal(ApplicationLocal<T> local, T value);
    }

    sealed interface Installer permits PackedApplicationInstaller {

        // return value.getClass() from newHandle must match handleClass
        <H extends ApplicationHandle<?,?>> H install(Assembly assembly, Function<? super ApplicationTemplate.Installer, H> newHandle, Wirelet... wirelets);

        <T> Configurator setLocal(ApplicationLocal<T> local, T value);

    }

}

// ApplicationBridge