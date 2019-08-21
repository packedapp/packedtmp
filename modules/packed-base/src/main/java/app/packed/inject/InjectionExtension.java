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
import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;
import app.packed.container.extension.OnHook;
import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import app.packed.util.Qualifier;
import packed.internal.config.site.ConfigSiteType;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.build.AtProvidesGroup;
import packed.internal.inject.build.InjectorBuilder;

/**
 * This extension provides functionality for injection and service management.
 */

// Functionality for
// * Explicitly requiring services: require, requiOpt
// * Exporting services: export
// * Importing injectors : importEx
// * Providing components
// * Manual Requirements Management

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

    /** The injector builder that does the hard work. */
    private final InjectorBuilder builder;

    /** The configuration of the container in which this extension is registered. */
    private final PackedContainerConfiguration configuration;

    /** Creates a new injector extension. */
    InjectionExtension(PackedContainerConfiguration configuration) {
        this.configuration = requireNonNull(configuration);
        this.builder = new InjectorBuilder(configuration);
    }

    /** {@inheritDoc} */
    @Override
    protected void buildBundle(Builder descriptor) {
        builder.buildBundle(descriptor);
    }

    /**
     * @param <T>
     * @param key
     *            the key to export
     * @return a configuration for the exported service
     */
    public <T> ServiceConfiguration<T> export(Class<T> key) {
        return export(Key.of(key));
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
     *            the type of the exposed service
     * @param key
     *            the key of the internal service to expose
     * @return a service configuration for the exposed service
     * @see #export(Key)
     */
    public <T> ServiceConfiguration<T> export(Key<T> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        InternalConfigSite cs = configuration.configSite().thenStack(ConfigSiteType.BUNDLE_EXPOSE);
        return builder.export(key, cs);
    }

    public <T> ServiceConfiguration<T> export(ProvidedComponentConfiguration<T> configuration) {
        requireNonNull(configuration, "configuration is null");
        checkConfigurable();
        InternalConfigSite cs = this.configuration.configSite().thenStack(ConfigSiteType.BUNDLE_EXPOSE);
        return builder.export(cs, configuration);
    }

    /**
     * Imports all the services from the specified injector and makes available.
     * <p>
     * Wirelets can be used to transform and filter the services from the specified injector.
     * 
     * @param injector
     *            the injector to provide services from
     * @param wirelets
     *            any wirelets used to filter and transform the provided services
     */
    public void importAll(Injector injector, Wirelet... wirelets) {
        requireNonNull(injector, "injector is null");
        checkConfigurable();
        builder.importAll(injector, WireletList.of(wirelets));
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
        checkConfigurable();
        builder.manualRequirementsManagement();
    }

    /** {@inheritDoc} */
    @Override
    protected void onConfigured() {
        builder.build(buildContext());
    }

    /** {@inheritDoc} */
    @Override
    protected void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        builder.onPrepareContainerInstantiation(context);
    }

    /**
     * Invoked by the runtime when a component has members (fields or methods) that are annotated with {@link Provide}.
     * 
     * @param cc
     *            the configuration of the annotated component
     * @param group
     *            a provides aggregate object
     */
    @OnHook(AtProvidesGroup.Builder.class)
    void onProvidedMembers(ComponentConfiguration cc, AtProvidesGroup group) {
        builder.onProvidedMembers(cc, group);
    }

    /**
     * @param <T>
     *            the type of service to provide
     * @param implementation
     *            the type of service to provide
     * 
     * @return a configuration of the service
     */
    public <T> ProvidedComponentConfiguration<T> provide(Class<T> implementation) {
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
    public <T> ProvidedComponentConfiguration<T> provide(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        checkConfigurable();
        ComponentConfiguration cc = use(ComponentExtension.class).install(factory);
        return builder.provideFactory(cc, factory, factory.factory.function);
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
    public <T> ProvidedComponentConfiguration<T> provide(T instance) {
        requireNonNull(instance, "instance is null");
        checkConfigurable();
        ComponentConfiguration cc = use(ComponentExtension.class).install(instance);
        return builder.provideInstance(cc, instance);
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
        builder.requireExplicit(key, false);
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
    public void requireOptionally(Key<?> key) {
        checkConfigurable();
        builder.requireExplicit(key, true);
    }
}
// manualRequirementManagement(); Do we need or can we just say that we should extend this contract exactly?
// Registered registererUnqualifiedAnnotation <---
// Tror kun det ville skabe en masse problemer, en bundle der registrere den, men en anden hvor man glemmer det.
// Man faar ikke nogle fejl fordi runtimen i det "glemte" bundle ikke er klar over den har nogen betydning.
