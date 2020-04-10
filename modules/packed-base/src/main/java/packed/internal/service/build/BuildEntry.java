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
package packed.internal.service.build;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.service.Provide;
import app.packed.service.ProvideContext;
import app.packed.service.ServiceConfiguration;
import app.packed.service.ServiceDescriptor;
import app.packed.service.ServiceMode;
import packed.internal.inject.ServiceDependency;
import packed.internal.service.build.service.AbstractComponentBuildEntry;
import packed.internal.service.runtime.InjectorEntry;
import packed.internal.util.KeyBuilder;

/**
 * Build service entries ...node is used at configuration time, to make sure that multiple services with the same key
 * are not registered. And for helping in initialization dependency graphs. Build nodes has extra fields that are not
 * needed at runtime.
 * 
 * <p>
 * BSEs are never exposed to end-users, but instead wrapped in implementations of {@link ServiceConfiguration}.
 */
// BuildEntry does not implements ServiceDescriptor because it is mutable, so we
public abstract class BuildEntry<T> {

    /** An empty array of entries. */
    private static final BuildEntry<?>[] EMPTY_ARRAY = new BuildEntry<?>[0];

    /** The configuration site of this object. */
    private final ConfigSite configSite;

    /** The dependencies of this node. */
    public final List<ServiceDependency> dependencies;

    public String description;

    /** A flag used to detect cycles in the dependency graph. */
    public boolean detectCycleVisited;

    /** Whether or this node contains a dependency on {@link ProvideContext}. */
    protected final boolean hasDependencyOnInjectionSite;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with {@link Provide}. In which the case the declaring class needs to be constructor
     * injected before the providing method can be invoked.
     */
    @Nullable
    protected Key<T> key;

    /** The resolved dependencies of this node. */
    public final BuildEntry<?>[] resolvedDependencies;

    /** The service no this entry belongs to. Or null for wirelets */
    @Nullable // Is nullable for stages for now
    public final ServiceExtensionNode node;

    public final int offset;

    public BuildEntry(@Nullable ServiceExtensionNode serviceExtension, ConfigSite configSite, List<ServiceDependency> dependencies) {
        this(serviceExtension, null, configSite, dependencies);
    }

    public BuildEntry(@Nullable ServiceExtensionNode serviceExtension, AbstractComponentBuildEntry<?> declaringEntry, ConfigSite configSite,
            List<ServiceDependency> dependencies) {
        this.node = serviceExtension;
        this.offset = declaringEntry == null ? 0 : 1;
        this.configSite = requireNonNull(configSite);
        this.dependencies = requireNonNull(dependencies);
        int depSize = dependencies.size() + offset;
        this.resolvedDependencies = depSize == 0 ? EMPTY_ARRAY : new BuildEntry<?>[depSize];
        boolean hasDependencyOnInjectionSite = false;
        if (declaringEntry != null) {
            resolvedDependencies[0] = declaringEntry;
        }
        if (!dependencies.isEmpty()) {
            for (ServiceDependency e : dependencies) {
                if (e.key().equals(KeyBuilder.INJECTION_SITE_KEY)) {
                    hasDependencyOnInjectionSite = true;
                    break;
                }
            }
        }
        this.hasDependencyOnInjectionSite = hasDependencyOnInjectionSite;
    }

    @SuppressWarnings("unchecked")
    public void as(Key<? super T> key) {
        requireNonNull(key, "key is null");
        // requireConfigurable();
        // validateKey(key);
        // Det er sgu ikke lige til at validere det med generics signature....
        this.key = (Key<T>) key;
    }

    public final void checkResolved() {
        for (int i = offset; i < resolvedDependencies.length; i++) {
            BuildEntry<?> n = resolvedDependencies[i];
            if (n == null && !dependencies.get(i).isOptional()) {
                throw new AssertionError("Dependency " + dependencies.get(i) + " was not resolved");
            }
        }
    }

    /**
     * Returns the configuration site of this configuration.
     * 
     * @return the configuration site of this configuration
     */
    public final ConfigSite configSite() {
        return configSite;
    }

    /**
     * If this node is located on another build node return the node, otherwise null. For example a method annotated with
     * {@link Provide} on a class that is itself registered as a component.
     * 
     * @return stuff
     */
    @Nullable
    public BuildEntry<?> declaringEntry() {
        return null;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns whether or not this node has any dependencies that needs to be resolved.
     *
     * @return whether or not this node has any dependencies that needs to be resolved
     */
    public abstract boolean hasUnresolvedDependencies();

    public abstract ServiceMode instantiationMode();

    public boolean isPrivate() {
        return key().equals(KeyBuilder.INJECTOR_KEY);// || key().equals(KeyBuilder.CONTAINER_KEY);
    }

    public final Key<T> key() {
        return key;
    }

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    protected abstract InjectorEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context);

    public abstract boolean requiresPrototypeRequest();

    public final ServiceDescriptor toDescriptor() {
        return new PackedServiceDescriptor(key, configSite, description);
    }

    @SuppressWarnings("unchecked")
    public final InjectorEntry<T> toRuntimeEntry(ServiceExtensionInstantiationContext context) {
        return (InjectorEntry<T>) context.transformers.computeIfAbsent(this, k -> k.newRuntimeNode(context));
    }
}
