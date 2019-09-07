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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentExtension;
import app.packed.container.BundleDescriptor;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionContext;
import app.packed.container.extension.OnHook;
import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import app.packed.util.Qualifier;
import packed.internal.access.AppPackedInjectAccess;
import packed.internal.access.SharedSecrets;
import packed.internal.inject.InjectConfigSiteOperations;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.build.service.AtProvidesGroup;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.inject.run.AbstractInjector;
import packed.internal.inject.util.AtInjectGroup;

/**
 * This extension provides functionality for injection and service management.
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

// Profile virker ikke her. Fordi det er ikke noget man dynamisk vil switche on an off..
// Maybe have an Bundle.onExtensionActivation(Extension e) <- man kan overskrive....
// Eller @BundleStuff(onActivation = FooActivator.class) -> ForActivator extends BundleController
public final class InjectionExtension extends Extension {

    static {
        SharedSecrets.initialize(AppPackedInjectAccess.class, new AppPackedInjectAccess() {

            /** {@inheritDoc} */
            @Override
            public InjectorBuilder getBuilder(InjectionExtension extension) {
                return extension.builder;
            }

            /** {@inheritDoc} */
            @Override
            public <T> FactoryHandle<T> toInternalFunction(Factory<T> factory) {
                return factory.factory.function;
            }
        });
    }

    /** The injector builder, initialized via {@link #onAdded()}. */
    private InjectorBuilder builder;

    /** Should never be initialized by users. */
    InjectionExtension() {}

    /** {@inheritDoc} */
    @Override
    protected void buildDescriptor(BundleDescriptor.Builder descriptor) {
        builder.buildDescriptor(descriptor);
    }

    @Override
    protected InjectorBuilder initialize(ExtensionContext context) {
        return builder = new InjectorBuilder(context);
    }

    /**
     * Exports a service with the specified type.
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
     * @param <T>
     *            the type of service the configuration creates
     * @param configuration
     *            the service to export
     * @return a new service configuration object representing the exported service
     * @throws IllegalArgumentException
     *             if the specified configuration object was created by another injection extension instance .
     */
    public <T> ServiceConfiguration<T> export(ComponentServiceConfiguration<T> configuration) {
        requireNonNull(configuration, "configuration is null");
        checkConfigurable();
        return builder.exports().export(configuration, captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
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
        return builder.exports().export(key, captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
    }

    /**
     * 
     */
    public void exportAll() {
        checkConfigurable();
        builder.exports().exportAll(captureStackFrame(InjectConfigSiteOperations.INJECTOR_EXPORT_SERVICE));
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
        builder.dependencies().manualRequirementsManagement();
    }

    /** {@inheritDoc} */
    @Override
    protected void onAdded() {
        builder = new InjectorBuilder(context());
    }

    /** {@inheritDoc} */
    @Override
    protected void onConfigured() {
        builder.build(context().buildContext());
    }

    /**
     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Inject}.
     * 
     * @param cc
     *            the configuration of the annotated component
     * @param group
     *            a inject group object
     */
    @OnHook(AtInjectGroup.Builder.class)
    void onHookAtInjectGroup(ComponentConfiguration cc, AtInjectGroup group) {
        builder.onInjectGroup(cc, group);
    }

    /**
     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Provide}.
     * 
     * @param cc
     *            the configuration of the annotated component
     * @param group
     *            a provides group object
     */
    @OnHook(AtProvidesGroup.Builder.class)
    void onHookAtProvidesGroup(ComponentConfiguration cc, AtProvidesGroup group) {
        builder.provider().onProvidesGroup(cc, group);
    }

    /** {@inheritDoc} */
    @Override
    protected void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        builder.onPrepareContainerInstantiation(context);
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
        return builder.provider().provideFactory(use(ComponentExtension.class).install(factory), factory, factory.factory.function);
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
    public <T> ComponentServiceConfiguration<T> provide(T instance) {
        // configurability is checked by ComponentExtension
        return builder.provider().provideInstance(use(ComponentExtension.class).install(instance), instance);
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
            throw new IllegalArgumentException("Custom implementations of Injector are currently not supported, injector type = " + injector.getClass());
        }
        checkConfigurable();
        builder.provider().provideAll((AbstractInjector) injector, captureStackFrame(InjectConfigSiteOperations.INJECTOR_PROVIDE_ALL),
                WireletList.of(wirelets));
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
        builder.dependencies().require(ServiceDependency.of(key), captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE));
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
        builder.dependencies().require(ServiceDependency.ofOptional(key), captureStackFrame(InjectConfigSiteOperations.INJECTOR_REQUIRE_OPTIONAL));
    }
}
