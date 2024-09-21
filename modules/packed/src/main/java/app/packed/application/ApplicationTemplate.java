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
import app.packed.operation.Op;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.PackedApplicationTemplate.PackedApplicationTemplateConfigurator;
import sandbox.extension.container.ContainerTemplate;

/**
 *
 */

// Altsaa som jeg ser det kan det vaere vi bliver noedt til at faa den injected
// Paa en eller anden maade bliver vi noedt til at faa noget runtime

public sealed interface ApplicationTemplate<A> permits PackedApplicationTemplate {

    ApplicationTemplate<Void> DEFAULT = ApplicationTemplate.of(c -> {});

    ApplicationTemplate<Void> UNMANGED = ApplicationTemplate.of(c -> {});

    // Hmm descriptors....
    /**
     * A bootstrap app is a special type of application that can be used to create other (non-bootstrap) application.
     * <p>
     * Bootstrap apps cannot directly modify the applications that it bootstraps. It cannot, for example, install an
     * extension in the application. However, it can say it can only bootstrap applications that have the extension
     * installed, failing with a build exception if the developer does not install the extension. As such, the bootstrap app
     * can only setup requirements for the application that it bootstraps. It cannot directly make the needed changes to the
     * bootstrapped application.
     * <p>
     * Bootstrap applications are rarely used directly by users. Instead users typically use thin wrappers such as
     * {@link App} or {@link app.packed.service.ServiceLocator} to create new applications. However, if greater control of
     * the application is needed users may create their own bootstrap application.
     * <p>
     * Normally, you never create more than a single instance of a bootstrap app. Bootstrap applications are, unless
     * otherwise specified, safe to use concurrently.
     *
     * @return
     */
    BootstrapApp<A> newBootstrapApp();

    ApplicationTemplate.Installer<A> newInstaller(BuildGoal goal, Wirelet... wirelets);

    static <A> ApplicationTemplate<A> of(Class<A> hostClass, Consumer<? super Configurator> configure) {
        PackedApplicationTemplate<A> pat = new PackedApplicationTemplate<>(hostClass, null);

        PackedApplicationTemplateConfigurator<A> c = new PackedApplicationTemplateConfigurator<>(pat);
        configure.accept(c);
        return c.pbt;
    }

    static ApplicationTemplate<Void> of(Consumer<? super Configurator> configure) {
        return of(Void.class, configure);
    }

    static <A> ApplicationTemplate<A> of(Op<A> hostClass, Consumer<? super Configurator> configure) {
        Class<?> type = hostClass.type().returnRawType();
        PackedApplicationTemplate<A> pat = new PackedApplicationTemplate<>(type, hostClass, null);

        PackedApplicationTemplateConfigurator<A> c = new PackedApplicationTemplateConfigurator<>(pat);
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

        Configurator container(Consumer<? super ContainerTemplate.Configurator> configure);

        // Configuration of the root container
        Configurator container(ContainerTemplate template);

        Configurator managedLifetime();

        // Mark the application as removable()
        Configurator removeable();

        <T> Configurator setLocal(ApplicationLocal<T> local, T value);
    }

    sealed interface Installer<A> permits PackedApplicationInstaller {

        // return value.getClass() from newHandle must match handleClass
        <H extends ApplicationHandle<?, A>> H install(Assembly assembly, Function<? super ApplicationTemplate.Installer<A>, H> newHandle, Wirelet... wirelets);

        <T> Configurator setLocal(ApplicationLocal<T> local, T value);
    }
}
