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
import packed.internal.inject.ServiceNode;
import packed.internal.inject.buildtime.AtProvidesGroup;
import packed.internal.inject.buildtime.BuildServiceNode;
import packed.internal.inject.buildtime.BuildServiceNodeExported;
import packed.internal.inject.buildtime.DependencyGraph;
import packed.internal.inject.buildtime.InjectorBuilder;

/**
 * This extension provides functionality for injection and service management.
 */
// manualRequirementManagement(); Do we need or can we just say that we should extend this contract exactly?
// Registered registererUnqualifiedAnnotation <---
// Tror kun det ville skabe en masse problemer, en bundle der registrere den, men en anden hvor man glemmer det.
// Man faar ikke nogle fejl fordi runtimen i det "glemte" bundle ikke er klar over den har nogen betydning.
public final class InjectionExtension extends Extension {

    @SuppressWarnings("exports")
    public final InjectorBuilder builder;

    /** The container configuration. */
    private final PackedContainerConfiguration configuration;

    /** Creates a new injector extension. */
    InjectionExtension(PackedContainerConfiguration configuration) {
        this.configuration = requireNonNull(configuration);
        builder = new InjectorBuilder(configuration);
    }

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

    /** {@inheritDoc} */
    @Override
    public void buildBundle(Builder descriptor) {
        for (ServiceNode<?> n : builder.nodes) {
            if (n instanceof BuildServiceNode) {
                descriptor.addServiceDescriptor(((BuildServiceNode<?>) n).toDescriptor());
            }
        }

        for (BuildServiceNode<?> n : builder.exportedNodes) {
            if (n instanceof BuildServiceNodeExported) {
                descriptor.contract().services().addProvides(n.getKey());
            }
        }

        builder.buildContract(descriptor.contract().services());
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
        return builder.exportKey(key, cs);
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
        requireNonNull(injector, "injector is null");
        checkConfigurable();
        builder.importAll(injector, WireletList.of(wirelets));
    }

    /**
     * Requires that all requirements are explicitly added via either {@link #addOptional(Key)}, {@link #addRequired(Key)}
     * or via implementing a contract.
     */
    // Kan vi lave denne generisk paa tvaers af extensions...
    // disableAutomaticRequirements()
    public void manualRequirementsManagement() {
        builder.autoRequires = false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void onConfigured() {
        for (BuildServiceNode<?> e : builder.nodes2) {
            if (!builder.nodes.putIfAbsent(e)) {
                System.err.println("OOPS " + e.getKey());
            }
        }
        for (BuildServiceNodeExported<?> e : builder.exportedNodes) {
            ServiceNode<?> sn = builder.nodes.getRecursive(e.getKey());
            if (sn == null) {
                throw new IllegalStateException("Could not find node to export " + e.getKey());
            }
            e.exportOf = (ServiceNode) sn;
            builder.exports.put(e);
        }
        DependencyGraph dg = new DependencyGraph(configuration, builder);

        if (buildContext().isInstantiating()) {
            dg.instantiate();
        } else {
            dg.analyze();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
        context.put(configuration, builder.publicInjector); // Taken by PackedContainer
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

    @OnHook(AtProvidesGroup.Builder.class)
    void set(ComponentConfiguration cc, AtProvidesGroup group) {
        builder.set(cc, group);
    }
}
