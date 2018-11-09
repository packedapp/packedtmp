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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.inject.InjectionSite;
import app.packed.inject.Key;
import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.InternalInjectionSites;
import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.inject.Node;
import packed.internal.inject.runtimenodes.RuntimeNode;
import packed.internal.util.AbstractConfiguration;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A build node is used at configuration time, to make sure that multiple services with the same key are not registered.
 * And for helping in initialization dependency graphs. Build nodes has extra fields that are not needed at runtime.
 */
public abstract class BuildNode<T> extends AbstractConfiguration<BuildNode<T>> implements Node<T>, ServiceConfiguration<T> {

    /** An empty array of nodes */
    private static final Node<?>[] EMPTY_ARRAY = new Node<?>[0];

    /** The configuration site of this node. **/
    final InternalConfigurationSite configurationSite;

    /** The dependencies of this node. */
    final List<InternalDependency> dependencies;

    /** A flag used to detect cycles in the build graph. */
    boolean detectCycleVisited;

    /** The injector configuration this node is registered with. */
    final InternalInjectorConfiguration injectorConfiguration;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has a
     * methods annotated with {@link Provides}. In which the case the declaring class might need to be constructor injected
     * before the method can be executed.
     */
    @Nullable
    private Key<T> key;

    /** The resolved dependencies of this node. */
    public final Node<?>[] resolvedDependencies;

    /** We cache the runtime node, to make sure it is only created once. */
    @Nullable
    private RuntimeNode<T> runtime;

    /** Whether or this node contains a dependency on {@link InjectionSite}. */
    final boolean hasDependencyOnInjectionSite;

    BuildNode(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, List<InternalDependency> dependencies) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.dependencies = requireNonNull(dependencies);
        this.resolvedDependencies = dependencies.isEmpty() ? EMPTY_ARRAY : new Node<?>[dependencies.size()];

        boolean hasDependencyOnInjectionSite = false;
        if (!dependencies.isEmpty()) {
            for (InternalDependency e : dependencies) {
                if (e.getKey().equals(InternalInjectionSites.INJECTION_SITE_KEY)) {
                    hasDependencyOnInjectionSite = true;
                    break;
                }
            }
        }
        this.hasDependencyOnInjectionSite = hasDependencyOnInjectionSite;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final ServiceConfiguration<T> as(Key<? super T> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        // validateKey(key);
        // Det er sgu ikke lige til at validere det med generics signature....
        this.key = (Key<T>) key;
        return this;
    }

    public final void checkResolved() {
        for (int i = 0; i < resolvedDependencies.length; i++) {
            Node<?> n = resolvedDependencies[i];
            if (n == null && !dependencies.get(i).isOptional()) {
                throw new AssertionError("Dependency " + dependencies.get(i) + " was not resolved");
            }
        }
    }

    /**
     * If this node is located on another build node return the node, otherwise null. For example a method annotated with
     * {@link Provides} on a class that is itself registered as a component.
     * 
     * @return
     */
    @Nullable
    BuildNode<?> declaringNode() {
        return null;
    }

    @Override
    public final void freeze() {
        super.freeze();
    }

    /** {@inheritDoc} */
    @Override
    public final InternalConfigurationSite getConfigurationSite() {
        return configurationSite;
    }

    /**
     * Returns a list of all dependencies of this node.
     * 
     * @return a list of all dependencies of this node
     */
    public final List<InternalDependency> getDependencies() {
        return dependencies;
    }

    /** {@inheritDoc} */
    @Override
    public final Key<T> getKey() {
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
    abstract RuntimeNode<T> newRuntimeNode();

    /** {@inheritDoc} */
    @Override
    public final RuntimeNode<T> toRuntimeNode() {
        RuntimeNode<T> runtime = this.runtime;
        return runtime == null ? this.runtime = newRuntimeNode() : runtime;
    }
}
