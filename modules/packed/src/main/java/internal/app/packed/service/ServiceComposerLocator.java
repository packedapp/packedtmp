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
package internal.app.packed.service;

import app.packed.application.ApplicationTemplate;
import app.packed.application.BootstrapApp;
import app.packed.assembly.AbstractComposer;
import app.packed.assembly.AbstractComposer.ComposableAssembly;
import app.packed.assembly.AbstractComposer.ComposerAction;
import app.packed.assembly.Assembly;
import app.packed.component.guest.GuestBinding;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.operation.Op;
import app.packed.operation.Op1;
import app.packed.runtime.RunState;
import app.packed.service.ProvidableBeanConfiguration;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class ServiceComposerLocator {
    /**
     * Returns the bootstrap app. If interfaces allowed non-public fields we would have stored it in a field instead of this
     * method.
     */
    private static BootstrapApp<ServiceLocator> bootstrap() {
        class ServiceLocatorBootstrap {
            private static final BootstrapApp<ServiceLocator> APP = BootstrapApp
                    .of(ApplicationTemplate.ofUnmanaged(new Op1<@GuestBinding ServiceLocator, ServiceLocator>(e -> e) {}));
        }
        return ServiceLocatorBootstrap.APP;
    }

    public static ServiceLocator of(ComposerAction<? super Composer> action, Wirelet... wirelets) {
        class ServiceLocatorAssembly extends ComposableAssembly<Composer> {

            ServiceLocatorAssembly(ComposerAction<? super Composer> action) {
                super(new Composer(), action);
            }
        }

        return bootstrap().launch(RunState.INITIALIZED, new ServiceLocatorAssembly(action), wirelets);
    }

    /**
     * A lightweight configuration object that can be used to create injectors via. This is thought of alternative to using
     * a {@link BaseAssembly}. Unlike assemblies all services are automatically exported once defined. For example useful in
     * tests.
     *
     * <p>
     * The main difference compared assemblies is that there is no concept of encapsulation. All services are exported by
     * default.
     */
    public static final class Composer extends AbstractComposer {

        /** For internal use only. */
        private Composer() {}

        public <T> ProvidableBeanConfiguration<T> install(Class<T> op) {
            return base().install(op);
        }

        public <T> ProvidableBeanConfiguration<T> install(Op<T> op) {
            return base().install(op);
        }

        public <T> ProvidableBeanConfiguration<T> installInstance(T instance) {
            return base().installInstance(instance);
        }

        /**
         * @param assembly
         *            the assembly to bind
         * @param wirelets
         *            optional import/export wirelets
         */
        public void link(String name, Assembly assembly, Wirelet... wirelets) {
            base().link(name, assembly, wirelets);
        }

        @Override
        protected void preCompose() {
            base().exportAll();
        }

        /**
         * Provides the specified implementation as a new singleton service. An instance of the implementation will be created
         * together with the injector. The runtime will use {@link Op#factoryOf(Class)} to find the constructor or method used
         * for instantiation.
         * <p>
         * The default key for the service will be the specified {@code implementation}. If the
         * {@code implementation.getClass()} is annotated with a {@link Qualifier qualifier annotation}, the default key will
         * include the qualifier. For example, given this implementation: <pre>
         * &#64;SomeQualifier
         * public class SomeService {}
         * </pre>
         * <p>
         * The following two example are equivalent
         * </p>
         * <pre>
         * Injector i = Injector.of(c -&gt; {
         *    c.provide(SomeService.class);
         * });
         * </pre> <pre>
         * Injector i = Injector.of(c -&gt; {
         *   c.provide(SomeService.class).as(new Key&lt;&#64;SomeQualifier SomeService&gt;() {});
         * });
         * </pre>
         *
         * @param <T>
         *            the type of service to provide
         * @param implementation
         *            the implementation to provide a singleton instance of
         * @return a service configuration for the service
         */
        public <T> ProvidableBeanConfiguration<T> provide(Class<T> implementation) {
            return base().install(implementation).provide();
        }

        /**
         * Binds the specified factory to a new service. When the injector is created the factory will be invoked <b>once</b> to
         * instantiate the service instance.
         * <p>
         * The default key for the service is determined by {@link Op#toKey()}.
         *
         * @param <T>
         *            the type of service to bind
         * @param op
         *            the factory to bind
         * @return a service configuration for the service
         */
        public <T> ProvidableBeanConfiguration<T> provide(Op<T> op) {
            return base().install(op).provide();
        }

        /**
         * Binds all services from the specified injector.
         * <p>
         * A simple example, importing a singleton {@code String} service from one injector into another:
         *
         * <pre> {@code
         * Injector importFrom = Injector.of(c -&gt; c.bind("foostring"));
         *
         * Injector importTo = Injector.of(c -&gt; {
         *   c.bind(12345);
         *   c.provideAll(importFrom);
         * });
         *
         * IO.println(importTo.with(String.class));// prints "foostring"}}
         * </pre>
         * <p>
         * It is possible to specify one or import stages that can restrict or transform the imported services.
         * <p>
         * For example, the following example takes the injector we created in the previous example, and creates a new injector
         * that only imports the {@code String.class} service.
         *
         * <pre>
         * Injector i = Injector.of(c -&gt; {
         *   c.injectorBind(importTo, InjectorImportStage.accept(String.class));
         * });
         * </pre> Another way of writing this would be to explicitly reject the {@code Integer.class} service. <pre>
         * Injector i = Injector.of(c -&gt; {
         *   c.provideAll(importTo, InjectorImportStage.reject(Integer.class));
         * });
         * </pre> @param injector the injector to bind services from
         *
         * @param injector
         *            the injector to import services from
         */
        // maybe bindAll()... Syntes man burde hedde det samme som Bindable()
        // Er ikke sikker paa vi skal have wirelets her....
        // Hvis det er noedvendigt saa maa man lave en ny injector taenker jeg....
        public void provideAll(ServiceLocator injector) {
            throw new UnsupportedOperationException();
        }

        /**
         * Binds the specified instance as a new service.
         * <p>
         * The default key for the service will be {@code instance.getClass()}. If the type returned by
         * {@code instance.getClass()} is annotated with a {@link Qualifier qualifier annotation}, the default key will have the
         * qualifier annotation added.
         *
         * @param <T>
         *            the type of service to bind
         * @param instance
         *            the instance to bind
         * @return a service configuration for the service
         */
        // Rename to instant
        // All annotations will be processed like provide() except that constructors will not be processed
        // Ohh we need to analyze them differently, because we should ignore all constructors.
        // Should not fail if we fx have two public constructors of equal lenght
        public <T> ProvidableBeanConfiguration<T> provideInstance(T instance) {
            return base().installInstance(instance).provide();
        }

        public <T> ProvidableBeanConfiguration<T> providePrototype(Class<T> implementation) {
            return use(BaseExtension.class).installPrototype(implementation);
        }

        public <T> ProvidableBeanConfiguration<T> providePrototype(Op<T> factory) {
            return use(BaseExtension.class).installPrototype(factory);
        }
    }
}
