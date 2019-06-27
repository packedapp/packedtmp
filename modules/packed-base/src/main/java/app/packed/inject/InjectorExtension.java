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

import java.util.List;

import app.packed.container.BundleDescriptor.Builder;
import app.packed.container.Extension;
import app.packed.container.InstantiationContext;
import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.contract.Contract;
import app.packed.lifecycle.OnStart;
import app.packed.util.Key;
import app.packed.util.Qualifier;
import packed.internal.annotations.AtProvides;
import packed.internal.annotations.AtProvidesGroup;
import packed.internal.classscan.ServiceClassDescriptor;
import packed.internal.config.site.ConfigSiteType;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.container.DefaultComponentConfiguration;
import packed.internal.container.DefaultContainerConfiguration;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.buildtime.BuildtimeServiceNode;
import packed.internal.inject.buildtime.BuildtimeServiceNodeDefault;
import packed.internal.inject.buildtime.BuildtimeServiceNodeExported;
import packed.internal.inject.buildtime.DefaultProvidedComponentConfiguration;
import packed.internal.inject.buildtime.DefaultServiceConfiguration;
import packed.internal.inject.buildtime.DependencyGraph;
import packed.internal.inject.buildtime.ImportAllFromInjector;
import packed.internal.inject.buildtime.InjectorBuilder;
import packed.internal.invokable.InternalFunction;

/**
 * This extension provides functionality for injection and service management.
 */
// manualRequirementManagement(); Do we need or can we just say that we should extend this contract exactly?

// Registered registererUnqualifiedAnnotation <---
// Tror kun det ville skabe en masse problemer, en bundle der registrere den, men en anden hvor man glemmer det.
// Man faar ikke nogle fejl fordi runtimen i det "glemte" bundle ikke er klar over den har nogen betydning.
public final class InjectorExtension extends Extension<InjectorExtension> {

    static final String CONFIG_SITE_PROVIDE = "injector.provide";

    @SuppressWarnings("exports")
    public final InjectorBuilder builder = new InjectorBuilder();

    /** Creates a new injector extension. */
    InjectorExtension() {}

    /**
     * Adds the specified key to the list of optional services.
     * <p>
     * If a key is added optionally and the same key is later added as a normal (mandatory) requirement either explicitly
     * via # {@link #addRequired(Key)} or implicitly via, for example, a constructor dependency. The key will be removed
     * from the list of optional services and only be listed as a required key.
     * 
     * @param key
     *            the key to add
     */
    // Should be have varargs???, or as a minimum support method chaining
    public void addOptional(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        builder.addOptional(key);
        // return this;
    }

    /**
     * Explicitly adds the specified key to the list of required services for the underlying container.
     * 
     * @param key
     *            the key to add
     */
    // Contracts as well??? Would be nice to get out of the way..On the other hand its two methods...
    // And I don't know if you publically want to display the contracts you implement????
    public void addRequired(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        builder.addRequired(key);
    }

    <T> ProvidedComponentConfiguration<T> alias(Class<T> key) {
        // Hvorfor har vi brug for alias????
        // provide(BigFatClass.class);
        // provide(BigFatClass.class).as(X.class);
        // provide(BigFatClass.class).as(Y.class);

        // Den er ikke super brugbar..
        // Smid en static provides paa bundlen...
        // Og saa provide
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void onContainerInstantiate(InstantiationContext context) {
        context.put(configuration(), builder.publicInjector);
    }

    /** {@inheritDoc} */
    @Override
    public void buildBundle(Builder descriptor) {
        for (ServiceNode<?> n : builder.nodes) {
            if (n instanceof BuildtimeServiceNode) {
                descriptor.addServiceDescriptor(((BuildtimeServiceNode<?>) n).toDescriptor());
            }
        }

        for (BuildtimeServiceNode<?> n : builder.exportedNodes) {
            if (n instanceof BuildtimeServiceNodeExported) {
                descriptor.contract().services().addProvides(n.getKey());
            }
        }

        builder.buildContract(descriptor.contract().services());
    }

    private DefaultContainerConfiguration configuration0() {
        return (DefaultContainerConfiguration) configuration();
    }

    // Why export
    // Need to export

    public <T> ServiceConfiguration<T> export(Class<T> key) {
        requireNonNull(key, "key is null");
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

        InternalConfigSite cs = configuration0().configSite().thenStack(ConfigSiteType.BUNDLE_EXPOSE);

        // ServiceNode<T> node = box.services().nodes.getRecursive(key);
        // if (node == null) {
        // throw new IllegalArgumentException("Cannot expose non existing service, key = " + key);
        // }
        BuildtimeServiceNodeExported<T> bn = new BuildtimeServiceNodeExported<>(builder, cs);
        bn.as(key);
        builder.exportedNodes.add(bn);
        return new DefaultServiceConfiguration<>(configuration0(), bn);
    }

    @SuppressWarnings("unchecked")
    public <T> ServiceConfiguration<T> export(ProvidedComponentConfiguration<T> configuration) {
        // Skal skrives lidt om, det her burde virke, f.eks. som export(provide(ddd).asNone).as(String.class)
        return (ServiceConfiguration<T>) export(configuration.getKey());
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
        ImportAllFromInjector pfi = new ImportAllFromInjector(configuration0(), builder, injector, WireletList.of(wirelets)); // Validates arguments
        checkConfigurable();
        pfi.process(); // Will create the necessary nodes.
    }

    /**
     * Requires that all requirements are explicitly added via either {@link #addOptional(Key)}, {@link #addRequired(Key)}
     * or via implementing a {@link Contract}.
     */
    // Kan vi lave denne generisk paa tvaers af extensions...
    // disableAutomaticRequirements()
    public void manualRequirementsManagement() {
        builder.autoRequires = false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void onContainerConfigured() {
        for (BuildtimeServiceNode<?> e : builder.nodes2) {
            if (!builder.nodes.putIfAbsent(e)) {
                System.err.println("OOPS " + e.getKey());
            }
        }
        for (BuildtimeServiceNodeExported<?> e : builder.exportedNodes) {
            ServiceNode<?> sn = builder.nodes.getRecursive(e.getKey());
            if (sn == null) {
                throw new IllegalStateException("Could not find node to export " + e.getKey());
            }
            e.exposureOf = (ServiceNode) sn;
            builder.exports.put(e);
        }
        DependencyGraph dg = new DependencyGraph(configuration0(), builder);

        if (buildContext().outputType().isInstantiating()) {
            dg.instantiate();
        } else {
            dg.analyze();
        }
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> ProvidedComponentConfiguration<T> provide(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        checkConfigurable();
        InternalConfigSite configSite = configuration0().configSite().thenStack(ConfigSiteType.INJECTOR_CONFIGURATION_BIND);

        // Okay det her fra Factory skal caches....Med methodHandle....
        InternalFunction<T> func = factory.factory.function; // AppPackedInjectSupport.toInternalFunction(factory);
        ServiceClassDescriptor desc = configuration0().oldAccessor.serviceDescriptorFor(func.getReturnTypeRaw());

        BuildtimeServiceNodeDefault<T> node = new BuildtimeServiceNodeDefault<>(builder, configSite, desc, InstantiationMode.SINGLETON,
                configuration0().oldAccessor.readable(func), (List) factory.dependencies());
        scanForProvides(func.getReturnTypeRaw(), node);
        node.as(factory.defaultKey());
        builder.nodes2.add(node);
        return new DefaultProvidedComponentConfiguration<>(configuration0(), new DefaultComponentConfiguration(configSite, configuration0(), null), node);
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> ProvidedComponentConfiguration<T> provide(T instance) {
        requireNonNull(instance, "instance is null");
        checkConfigurable();
        InternalConfigSite configSite = configuration0().configSite().thenStack(ConfigSiteType.INJECTOR_CONFIGURATION_BIND);

        ServiceClassDescriptor sdesc = configuration0().oldAccessor.serviceDescriptorFor(instance.getClass());
        BuildtimeServiceNodeDefault<T> sc = new BuildtimeServiceNodeDefault<T>(builder, configSite, sdesc, instance);

        scanForProvides(instance.getClass(), sc);
        sc.as((Key) Key.of(instance.getClass()));
        builder.nodes2.add(sc);
        return new DefaultProvidedComponentConfiguration<>(configuration0(), new DefaultComponentConfiguration(configSite, configuration0(), null), sc);
    }

    // ServicesDescriptor descriptor (extends Contract????) <- What we got so far....

    // public void provideAll(Consumer<? super InjectorConfigurator> configurator, Wirelet... wirelets) {
    // // Hmm, hvor er wirelets'ene til????
    // // Maaske bare bedst at droppe den????
    //
    // Injector injector = Injector.of(configurator, wirelets);
    // }

    // Services are the default implementation of injection....

    // Export

    // Outer.. checker configurable, node. finish den sidste o.s.v.
    // Saa kalder vi addNode(inner.foo);

    public <T> ProvidedComponentConfiguration<T> provideMany(Class<T> implementation) {
        // Installs as a static component.... new instance every time it is requested...
        throw new UnsupportedOperationException();
    }

    public <T> ProvidedComponentConfiguration<T> provideMany(Factory<T> factory) {
        throw new UnsupportedOperationException();
    }

    public <T> void provideOptionalFallback(Class<T> t, T instance) {
        // Ideen er vi kan lave en default service...
    }

    private void scanForProvides(Class<?> type, BuildtimeServiceNodeDefault<?> owner) {
        AtProvidesGroup provides = configuration0().oldAccessor.serviceDescriptorFor(type).provides;
        if (!provides.members.isEmpty()) {
            owner.hasInstanceMembers = provides.hasInstanceMembers;
            // if (owner.instantiationMode() == InstantiationMode.PROTOTYPE && provides.hasInstanceMembers) {
            // throw new InvalidDeclarationException("Cannot @Provides instance members form on services that are registered as
            // prototypes");
            // }

            // First check that we do not have existing services with any of the provided keys
            // for (Key<?> k : provides.members.keySet()) {
            // // if (builder().box.services().nodes.containsKey(k)) {
            // // throw new IllegalArgumentException("At service with key " + k + " has already been registered");
            // // }
            // }

            // AtProvidesGroup has already validated that the specified type does not have any members that provide services with
            // the same key, so we can just add them now without any verification
            for (AtProvides member : provides.members.values()) {
                builder.nodes2.add(owner.provide(member));// put them directly
            }
        }
    }
}
