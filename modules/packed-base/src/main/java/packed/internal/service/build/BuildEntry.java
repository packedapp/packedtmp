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

import app.packed.config.ConfigSite;
import app.packed.service.Dependency;
import app.packed.service.PrototypeRequest;
import app.packed.service.Provide;
import app.packed.service.ServiceConfiguration;
import app.packed.service.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.service.ServiceEntry;
import packed.internal.service.run.RuntimeEntry;
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
public abstract class BuildEntry<T> implements ServiceEntry<T> {

    /** An empty array of entries. */
    private static final ServiceEntry<?>[] EMPTY_ARRAY = new ServiceEntry<?>[0];

    /** The configuration site of this object. */
    private final ConfigSite configSite;

    /** The dependencies of this node. */
    public final List<Dependency> dependencies;

    public String description;

    /** A flag used to detect cycles in the dependency graph. */
    public boolean detectCycleVisited;

    /** Whether or this node contains a dependency on {@link PrototypeRequest}. */
    protected final boolean hasDependencyOnInjectionSite;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with {@link Provide}. In which the case the declaring class needs to be constructor
     * injected before the providing method can be invoked.
     */
    @Nullable
    protected Key<T> key;

    /** The resolved dependencies of this node. */
    public final ServiceEntry<?>[] resolvedDependencies;

    /** The runtime representation of this node. We cache it, to make sure it is only created once. */
    @Nullable
    private RuntimeEntry<T> runtimeNode;

    /** The injector builder this node belongs to. */
    @Nullable // Is nullable for stages for now
    public final ServiceExtensionNode serviceExtension;

    public BuildEntry(@Nullable ServiceExtensionNode serviceExtension, ConfigSite configSite, List<Dependency> dependencies) {
        this.serviceExtension = serviceExtension;
        this.configSite = requireNonNull(configSite);
        this.dependencies = requireNonNull(dependencies);
        this.resolvedDependencies = dependencies.isEmpty() ? EMPTY_ARRAY : new ServiceEntry<?>[dependencies.size()];
        boolean hasDependencyOnInjectionSite = false;
        if (!dependencies.isEmpty()) {
            for (Dependency e : dependencies) {
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
        for (int i = 0; i < resolvedDependencies.length; i++) {
            ServiceEntry<?> n = resolvedDependencies[i];
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
    @Override
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

    public final Key<T> getKey() {
        return key;
    }

    /**
     * Returns whether or not this node has any dependencies that needs to be resolved.
     *
     * @return whether or not this node has any dependencies that needs to be resolved
     */
    @Override
    public abstract boolean hasUnresolvedDependencies();

    /** {@inheritDoc} */
    @Override
    public final Key<T> key() {
        return key;
    }

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    protected abstract RuntimeEntry<T> newRuntimeNode();

    public final ServiceDescriptor toDescriptor() {
        return new PackedServiceDescriptor(key, configSite, description);
    }

    /** {@inheritDoc} */
    @Override
    public final RuntimeEntry<T> toRuntimeEntry() {
        RuntimeEntry<T> runtime = this.runtimeNode;
        return runtime == null ? this.runtimeNode = newRuntimeNode() : runtime;
    }
}
