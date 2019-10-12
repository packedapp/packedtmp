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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.function.BiConsumer;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.Wirelet;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionComposer;
import app.packed.container.extension.UseExtension;
import app.packed.hook.OnHook;
import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Qualifier;
import packed.internal.container.WireletList;
import packed.internal.service.InjectConfigSiteOperations;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.build.ServiceWireletPipeline;
import packed.internal.service.build.service.AtProvidesGroup;
import packed.internal.service.run.AbstractInjector;

/**
 * This extension provides functionality for service management and dependency injection.
 * 
 * <p>
 * 
 * 
 */
// Functionality for
// * Explicitly requiring services: require, requiOpt & Manual Requirements Management
// * Exporting services: export, exportAll
// * Providing components or injectors (provideAll)

// Future potential functionality
/// Contracts
/// Security for public injector.... Maaske skal man explicit lave en public injector???
/// Transient requirements Management (automatic require unresolved services from children)
/// Integration pits
// MHT til Manuel Requirements Management
// (Hmm, lugter vi noget profile?? Nahh, folk maa extende BaseBundle og vaelge det..
// Hmm saa auto instantiere vi jo injector extensionen
//// Det man gerne vil kunne sige er at hvis InjectorExtensionen er aktiveret. Saa skal man
// altid bruge Manual Requirements
// contracts bliver installeret direkte paa ContainerConfiguration

// Profile virker ikke her. Fordi det er ikke noget man dynamisk vil switche on an off..
// Maybe have an Bundle.onExtensionActivation(Extension e) <- man kan overskrive....
// Eller @BundleStuff(onActivation = FooActivator.class) -> ForActivator extends BundleController

@UseExtension(ComponentExtension.class)
public final class ServiceExtension extends Extension {

    /** The extension node, initialized in {@link ServiceExtension.Composer#configure()}. */
    @Nullable
    private ServiceExtensionNode node;

    /** Should never be initialized by users. */
    ServiceExtension() {}

    /**
     * Exports a service of the specified type.
     * 
     * @param <T>
     *            the type of service to export
     * @param key
     *            the key of the service to export
     * @return a configuration for the exported service
     * @see #export(Key)
     */
    public <T> ServiceConfiguration<T> export(Class<T> key) {
        return export(Key.of(key));
    }

    /**
     * Exports a service represented by the specified service configuration. Is typically used together with
     * {@link #provide(Class)} to export and: <pre>
     * {@code  
     * export(provide(Service.class));
     * }
     * </pre> or <pre>
     * {@code  
     * export(provide(InternalClass.class)).as(ExternalInterface.class);
     * }
     * </pre>
     * 
     * @param <T>
     *            the type of service the configuration creates
     * @param configuration
     *            the service to export
     * @return a new service configuration object representing the exported service
     * @throws IllegalArgumentException
     *             if the specified configuration object was created by another injection extension instance .
     */
    // TODO provide(Foo.class).export instead????
    public <T> ServiceConfiguration<T> export(ComponentServiceConfiguration<T> configuration) {
        requireNonNull(configuration, "configuration is null");
        checkConfigurable();
        return node.exports().export(configuration, captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * Exposes an internal service outside of this bundle.
     * 
     * <pre>
     *  {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class);}
     * </pre>
     * 
     * You can also choose to expose a service under a different key then what it is known as internally in the
     * 
     * <pre>
     *  {@code  
     * bind(ServiceImpl.class);
     * expose(ServiceImpl.class).as(Service.class);}
     * </pre>
     * 
     * @param <T>
     *            the type of the service to export
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    public <T> ServiceConfiguration<T> export(Key<T> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        return node.exports().export(key, captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * 
     */
    public void exportAll() {
        checkConfigurable();
        node.exports().exportAll(captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * Requires that all requirements are explicitly added via either {@link #requireOptionally(Key)}, {@link #require(Key)}
     * or via implementing a contract.
     */
    // Kan vi lave denne generisk paa tvaers af extensions...
    // disableAutomaticRequirements()
    // Jeg taenker lidt det er enten eller. Vi kan ikke goere det per component.
    // Problemet er dem der f.eks. har metoder
    public void manualRequirementsManagement() {
        // explicitRequirementsManagement
        checkConfigurable();
        node.dependencies().manualRequirementsManagement();
    }

    /**
     * @param <T>
     *            the type of service to provide
     * @param implementation
     *            the type of service to provide
     * 
     * @return a configuration of the service
     */
    public <T> ComponentServiceConfiguration<T> provide(Class<T> implementation) {
        return provide(Factory.findInjectable(implementation));
    }

    /**
     *
     * <p>
     * Factory raw type will be used for scanning for annotations such as {@link OnStart} and {@link Provide}.
     *
     * @param <T>
     *            the type of component to install
     * @param factory
     *            the factory used for creating the component instance
     * @return the configuration of the component that was installed
     */
    public <T> ComponentServiceConfiguration<T> provide(Factory<T> factory) {
        // configurability is checked by ComponentExtension
        return node.provider().provideFactory(use(ComponentExtension.class).install(factory), factory, factory.factory.function);
    }

    /**
     * Imports all the services from the specified injector and make each service available to other services in the
     * injector being build.
     * <p>
     * Wirelets can be used to transform and filter the services from the specified injector.
     * 
     * @param injector
     *            the injector to import services from
     * @param wirelets
     *            any wirelets used to filter and transform the provided services
     * @throws IllegalArgumentException
     *             if specifying wirelets that are not defined via {@link ServiceWirelets}
     */
    public void provideAll(Injector injector, Wirelet... wirelets) {
        if (!(requireNonNull(injector, "injector is null") instanceof AbstractInjector)) {
            throw new IllegalArgumentException(
                    "Custom implementations of Injector are currently not supported, injector type = " + injector.getClass().getName());
        }
        checkConfigurable();
        node.provider().provideAll((AbstractInjector) injector, captureStackFrame(InjectConfigSiteOperations.INJECTOR_PROVIDE_ALL), WireletList.of(wirelets));
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
    public <T> ComponentServiceConfiguration<T> provideInstance(T instance) {
        // configurability is checked by ComponentExtension
        ComponentConfiguration<T> cc = use(ComponentExtension.class).installInstance(instance);
        return node.provider().provideInstance(cc, instance);
    }

    /**
     * Explicitly adds the specified key to the list of required services. There are typically two situations in where
     * explicitly adding required services can be useful:
     * <p>
     * First, services that are cannot be specified at build time. But is needed later... Is mainly useful when we the
     * services to. For example, importAll() that injector might not a service itself. But other that make use of the
     * injector might.
     * 
     * 
     * <p>
     * Second, for manual service requirement, although it is often preferable to use contracts here
     * <p>
     * In any but the simplest of cases, contracts are useful
     * 
     * @param key
     *            the key to add
     */
    public void require(Key<?> key) {
        checkConfigurable();
        node.dependencies().require(ServiceDependency.of(key), captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE));
    }

    /**
     * Adds the specified key to the list of optional services.
     * <p>
     * If a key is added optionally and the same key is later added as a normal (mandatory) requirement either explicitly
     * via # {@link #require(Key)} or implicitly via, for example, a constructor dependency. The key will be removed from
     * the list of optional services and only be listed as a required key.
     * 
     * @param key
     *            the key to add
     */
    // Should be have varargs???, or as a minimum support method chaining
    // MethodChaining does not work with bundles...
    public void requireOptionally(Key<?> key) {
        checkConfigurable();
        node.dependencies().require(ServiceDependency.ofOptional(key), captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE_OPTIONAL));
    }

    public <T, C> void inject(Key<T> key, ComponentConfiguration<C> into, BiConsumer<? super C, ? super T> action) {

    }

    @OnHook
    void onHook(AtProvidesGroup g, ComponentConfiguration<?> cc) {
        node.provider().addProvidesGroup(cc, g);
    }

    /** The composer for the service extension. */
    static final class Composer extends ExtensionComposer<ServiceExtension> {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            onExtensionInstantiated(e -> e.node = new ServiceExtensionNode(e.context()));

            onConfigured(e -> e.node.build());
            onInstantiation((e, c) -> e.node.onInstantiate(c));
            onLinkage((p, c) -> p.node.link(c.node));

            // Descriptors and contracts
            // What about runtime????
            exposeContract(ServiceContract.class, (e, c) -> e.node.newServiceContract(c));

            exposeDescriptor((e, b) -> e.node.buildDescriptor(b));

            onAddPostProcessor(p -> {
                p.root().use(ServiceExtension.class).provideInstance("fooo");
            });

            useWirelets(ServiceWireletPipeline.class, (e, w) -> new ServiceWireletPipeline(e, w, e.node));
            // Dies

            // Needing wirelet
            // contract, bundle, features?

            // Runtime stuff

            // Vi kan faktisk have flere graph modeller nu....
            // Vi kan understoette build, instantiate, osv.
            // ved at give den rigtige context med til hvert kald.
            // f.eks. InstantionContext til instantiate
            // WithPipelines to buildDescriptor()
            // o.s.v.
        }
    }
}
