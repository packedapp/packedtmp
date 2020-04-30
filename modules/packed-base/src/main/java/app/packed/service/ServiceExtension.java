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

import java.util.Optional;

import app.packed.analysis.BundleDescriptor;
import app.packed.base.Key;
import app.packed.base.Key.Qualifier;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.DescendentAdded;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.container.Wirelet;
import app.packed.container.WireletSupply;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;
import app.packed.inject.Factory;
import app.packed.inject.InjectionContext;
import app.packed.lifecycle.OnStart;
import app.packed.sidecar.Expose;
import app.packed.sidecar.ExtensionSidecar;
import app.packed.sidecar.PostSidecar;
import packed.internal.component.PackedSingletonConfiguration;
import packed.internal.container.TI.MyTestWirelet;
import packed.internal.container.WireletList;
import packed.internal.inject.ServiceDependency;
import packed.internal.inject.util.InjectConfigSiteOperations;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.buildtime.service.AtProvidesHook;
import packed.internal.service.buildtime.wirelets.ServiceWireletPipeline;
import packed.internal.service.runtime.AbstractInjector;

/**
 * This extension provides functionality for service management and dependency injection.
 * 
 * 
 */
// Functionality for
// * Explicitly requiring services: require, requiOpt & Manual Requirements Management
// * Exporting services: export, exportAll
// * Providing components or injectors (provideAll)
// * Manual Injection

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

// Taenker den kun bliver aktiveret hvis vi har en factory med mindste 1 unresolved dependency....
// D.v.s. install(Class c) -> aktivere denne extension, hvis der er unresolved dependencies...
// Ellers selvfoelgelig hvis man bruger provide/@Provides\
public final class ServiceExtension extends Extension {

    /** The service node that does most of the actual work. */
    final ServiceExtensionNode node;

    /** Should never be initialized by users. */
    ServiceExtension(ExtensionContext context, @WireletSupply Optional<MyTestWirelet> foo) {
        this.node = new ServiceExtensionNode(context);
    }

    <T> ServiceConfiguration<T> addOptional(Class<T> optional) {
        // @Inject is allowed, but other annotations, types und so weiter is not...

        throw new UnsupportedOperationException();
    }

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
        requireNonNull(key, "key is null");
        checkConfigurable();
        return node.exports().export(Key.of(key), captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
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

    <T> ServiceConfiguration<T> export(SingletonConfiguration<T> configuration) {
        // Ideen er at man kan ogsaa eksportere en service der overhoved ikke er
        // tilgaengelig internt, men kun externt...

        // export(installInstance("ffffo"));
        throw new UnsupportedOperationException();
    }

    // Hvis man skal eksportere noget under 2 nogler, maa man kalde export 2 gange...
    public <T> ServiceConfiguration<T> export(ServiceComponentConfiguration<T> configuration) {
        requireNonNull(configuration, "configuration is null");
        checkConfigurable();
        return node.exports().export(configuration, captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * 
     */
    public void exportAll() {
        checkConfigurable();
        node.exports().exportAll(captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * Requires that all requirements are explicitly added via either {@link #requireOptionally(Key...)},
     * {@link #require(Key...)} or via implementing a contract.
     */
    // Kan vi lave denne generisk paa tvaers af extensions...
    // disableAutomaticRequirements()
    // Jeg taenker lidt det er enten eller. Vi kan ikke goere det per component.
    // Problemet er dem der f.eks. har metoder
    //// Vil det ikke altid bliver efterfuldt af en contract?????
    // Ser ingen grund til baade at support
    // ManualRequirements management..
    // AutoExport with regards to contract???
    public void manualRequirementsManagement() {
        // explicitRequirementsManagement
        checkConfigurable();
        node.dependencies().manualRequirementsManagement();
    }

    /**
     * Invoked by the runtime for each component using {@link Provide}.
     * 
     * @param hook
     *            the cached hook
     * @param cc
     *            the configuration of the component that uses the annotation
     */
    @OnHook
    void onHook(AtProvidesHook hook, SingletonConfiguration<?> cc) {
        node.provider().addProvidesHook(hook, cc);
    }

    @OnHook
    void onHook(AnnotatedMethodHook<Provide> hook, SingletonConfiguration<?> cc) {
        // System.out.println("INVOKED " + hook.method());
    }

    /**
     * @param <T>
     *            the type of service to provide
     * @param implementation
     *            the type of service to provide
     * 
     * @return a configuration of the service
     */
    public <T> ServiceComponentConfiguration<T> provide(Class<T> implementation) {
        return provide(Factory.find(implementation));
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
    public <T> ServiceComponentConfiguration<T> provide(Factory<T> factory) {
        return node.provider().provideFactory((PackedSingletonConfiguration<T>) install(factory));
    }

    // Will install a ServiceStatelessConfiguration...
    <T> ServiceConfiguration<T> provideProtoype(Factory<T> factory) {
        // Hvordan FFF fungere det her???? Vi skal jo vaere knyttet til en component.
        throw new UnsupportedOperationException();
    }

//    public <T> ServiceComponentConfiguration<T> provide(Providable<T> c) {
//        throw new UnsupportedOperationException();
//    }

    <T> ServiceComponentConfiguration<T> provide(SingletonConfiguration<T> c) {
        // return node.provider().provideFactory(install(factory), factory, factory.factory.function);

        // IDeen er lidt at man f.eks. kan lave en ComponentExtension et andet sted, som saa kan bruges her.
        throw new UnsupportedOperationException();
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
    public <T> ServiceComponentConfiguration<T> provideConstant(T instance) {
        // configurability is checked by ComponentExtension
        SingletonConfiguration<T> cc = installInstance(instance);
        return node.provider().provideInstance(cc, instance);
    }

    public void require(Class<?>... keys) {
        checkConfigurable();
        ConfigSite cs = captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE);
        for (Class<?> key : keys) {
            node.dependencies().require(ServiceDependency.of(key), cs);
        }
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
     * @param keys
     *            the key(s) to add
     */
    public void require(Key<?>... keys) {
        checkConfigurable();
        ConfigSite cs = captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE);
        for (Key<?> key : keys) {
            node.dependencies().require(ServiceDependency.of(key), cs);
        }
    }

    /**
     * Adds the specified key to the list of optional services.
     * <p>
     * If a key is added optionally and the same key is later added as a normal (mandatory) requirement either explicitly
     * via # {@link #require(Key...)} or implicitly via, for example, a constructor dependency. The key will be removed from
     * the list of optional services and only be listed as a required key.
     * 
     * @param keys
     *            the key(s) to add
     */
    public void requireOptionally(Key<?>... keys) {
        checkConfigurable();
        ConfigSite cs = captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE_OPTIONAL);
        for (Key<?> key : keys) {
            node.dependencies().require(ServiceDependency.ofOptional(key), cs);
        }
    }

    /**
     * This method is invoked by the runtime after all children have been configured. But before any guests might have been
     * defined.
     */
    @PostSidecar(ExtensionSidecar.CHILDREN_CONFIGURED)
    void assemble(/* Optional<ServiceWireletPipeline> swp */ ) {
        node.build();
    }

    @Expose
    // Should be Optional<Pipeline>...
    ServiceContract con(ServiceWireletPipeline swp) {
        return node.newServiceContract(swp);
    }

    // Use pipeline???
    @Expose
    void con(BundleDescriptor.Builder builder) {
        node.buildDescriptor(builder);
    }

    @DescendentAdded
    void foo(ServiceExtension se, InjectionContext ic, ExtensionContext ec, @WireletSupply Optional<MyTestWirelet> mt) {
        node.link(se.node);
        System.out.println("MT " + mt);
//        // System.out.println("Got wirelets " + wirelets);
//        System.out.println("This " + this + " child = " + se);
//        System.out.println("GotIt " + ic.keys());
//
//        System.out.println(context().containerPath());
//        System.out.println(ec.containerPath());
    }
}
