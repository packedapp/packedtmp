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
import java.util.Optional;

import app.packed.config.ConfigSite;
import app.packed.service.Provide;
import app.packed.service.ServiceConfiguration;
import app.packed.service.ServiceDependency;
import app.packed.service.ServiceDescriptor;
import app.packed.service.ServiceRequest;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.service.ServiceEntry;
import packed.internal.service.run.RSE;
import packed.internal.util.KeyBuilder;

/**
 * Build service entries ...node is used at configuration time, to make sure that multiple services with the same key
 * are not registered. And for helping in initialization dependency graphs. Build nodes has extra fields that are not
 * needed at runtime.
 * 
 * <p>
 * BSEs are never exposed to end-users, but instead wrapped in implementations of {@link ServiceConfiguration}.
 */
public abstract class BuildEntry<T> implements ServiceEntry<T> {

    /** An empty array of nodes */
    private static final ServiceEntry<?>[] EMPTY_ARRAY = new ServiceEntry<?>[0];

    /** The configuration site of this object. */
    private final ConfigSite configSite;

    /** The dependencies of this node. */
    public final List<ServiceDependency> dependencies;

    public String description;

    /** A flag used to detect cycles in the dependency graph. */
    public boolean detectCycleVisited;

    /** Whether or this node contains a dependency on {@link ServiceRequest}. */
    protected final boolean hasDependencyOnInjectionSite;

    /** The injector builder this node belongs to. */
    @Nullable // Is nullable for stages for now
    public final InjectionExtensionNode injectorBuilder;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with {@link Provide}. In which the case the declaring class needs to be constructor
     * injected before the providing method can be invoked.
     */
    public Key<T> key;

    /** The resolved dependencies of this node. */
    public final ServiceEntry<?>[] resolvedDependencies;

    /** The runtime representation of this node. We cache it, to make sure it is only created once. */
    @Nullable
    private RSE<T> runtimeNode;

    public BuildEntry(InjectionExtensionNode injectorBuilder, ConfigSite configSite) {
        this(injectorBuilder, configSite, List.of());
    }

    public BuildEntry(InjectionExtensionNode injectorBuilder, ConfigSite configSite, List<ServiceDependency> dependencies) {
        this.injectorBuilder = injectorBuilder;
        this.configSite = requireNonNull(configSite);
        this.dependencies = requireNonNull(dependencies);
        this.resolvedDependencies = dependencies.isEmpty() ? EMPTY_ARRAY : new ServiceEntry<?>[dependencies.size()];
        boolean hasDependencyOnInjectionSite = false;
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
    public BuildEntry<?> declaringNode() {
        return null;
    }

    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public String getDescription() {
        return description;
    }

    public final Key<T> getKey() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public final Key<T> key() {
        return key;
    }

    /**
     * Returns whether or not this node has any dependencies that needs to be resolved.
     *
     * @return whether or not this node has any dependencies that needs to be resolved
     */
    @Override
    public abstract boolean needsResolving();

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    protected abstract RSE<T> newRuntimeNode();
    //
    // protected void onFreeze() {
    // if (key != null) {
    // if (this instanceof BuildtimeServiceNodeExported) {
    // injectorBuilder.box.services().exports.put(this);
    // } else {
    // if (!injectorBuilder.box.services().nodes.putIfAbsent(this)) {
    // System.err.println("OOPS " + key);
    // }
    // }
    // }
    // }

    public final ServiceDescriptor toDescriptor() {
        return new ExposedServiceDescriptor(key, configSite, description /* immutableCopyOfTags() */);
    }

    /** {@inheritDoc} */
    @Override
    public final RSE<T> toRuntimeEntry() {
        RSE<T> runtime = this.runtimeNode;
        return runtime == null ? this.runtimeNode = newRuntimeNode() : runtime;
    }

    /** The default implementation of {@link ServiceDescriptor}. */
    // We might ditch the interface is future versions, and just have a class.
    // However, for now I think we might like the flexibility of not having.
    // ServiceDescriptor.of
    static final class ExposedServiceDescriptor implements ServiceDescriptor {

        /** The configuration site of the service. */
        private final ConfigSite configSite;

        /** An optional description of the service. */
        @Nullable
        private final String description;

        /** The key of the service. */
        private final Key<?> key;

        /**
         * Creates a new descriptor.
         * 
         * @param key
         *            the key of the service
         * @param configSite
         *            the config site of the service
         * @param description
         *            the (optional) description of the service
         */
        ExposedServiceDescriptor(Key<?> key, ConfigSite configSite, String description) {
            this.key = requireNonNull(key);
            this.configSite = requireNonNull(configSite);
            this.description = description;
        }

        /** {@inheritDoc} */
        @Override
        public ConfigSite configSite() {
            return configSite;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<String> description() {
            return Optional.ofNullable(description);
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return key;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "ServiceDescriptor[key=" + key + ", configSite=" + configSite + ", description=" + description + "]";
        }
    }
}
