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
package packed.internal.service.buildtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.Provide;
import app.packed.service.ExportedServiceConfiguration;
import app.packed.service.Service;
import packed.internal.component.NodeStore;
import packed.internal.service.buildtime.service.ServiceProvidingManager;
import packed.internal.service.runtime.RuntimeEntry;

/**
 * Build service entries ...node is used at configuration time, to make sure that multiple services with the same key
 * are not registered. And for helping in initialization dependency graphs. Build nodes has extra fields that are not
 * needed at runtime.
 * 
 * <p>
 * BSEs are never exposed to end-users, but instead wrapped in implementations of {@link ExportedServiceConfiguration}.
 */
// BuildEntry does not implements ServiceDescriptor because it is mutable, so we
public abstract class BuildEntry<T> {

    /** The configuration site of this object. */
    private final ConfigSite configSite;

    /** A flag used to detect cycles in the dependency graph. */
    public boolean detectCycleVisited;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with {@link Provide}. In which the case the declaring class needs to be constructor
     * injected before the providing method can be invoked.
     */
    @Nullable
    protected Key<T> key;

    /** The service no this entry belongs to. Or null for wirelets */
    @Nullable // Is nullable for stages for now
    public final ServiceExtensionNode node;

    public final SourceHolder source;

    public BuildEntry(@Nullable ServiceExtensionNode serviceExtension, ConfigSite configSite) {
        this.node = serviceExtension;
        this.configSite = requireNonNull(configSite);
        this.source = new SourceHolder(List.of(), null);
    }

    public BuildEntry(@Nullable ServiceExtensionNode serviceExtension, ConfigSite configSite, SourceHolder sh) {
        this.node = serviceExtension;
        this.configSite = requireNonNull(configSite);
        this.source = sh;
    }

    @SuppressWarnings("unchecked")
    public void as(Key<? super T> key) {
        requireNonNull(key, "key is null");
        // requireConfigurable();
        // validateKey(key);
        // Det er sgu ikke lige til at validere det med generics signature....

        this.key = (Key<T>) key;
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

    /**
     * Returns whether or not this node has any dependencies that needs to be resolved.
     *
     * @return whether or not this node has any dependencies that needs to be resolved
     */
    public abstract boolean hasUnresolvedDependencies();

    public abstract ServiceMode instantiationMode();

    public final Key<T> key() {
        return key;
    }

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    protected abstract RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context);

    public abstract boolean requiresPrototypeRequest();

    public final Service toDescriptor() {
        return new PackedService(key, configSite);
    }

    protected abstract MethodHandle newMH(ServiceProvidingManager spm);

    // cacher runtime noden...
    @SuppressWarnings("unchecked")
    public final RuntimeEntry<T> toRuntimeEntry(ServiceExtensionInstantiationContext context) {
        return (RuntimeEntry<T>) context.transformers.computeIfAbsent(this, k -> {
            // System.out.println("MSIZE " + context.transformers.size() + " " + k);
            return k.newRuntimeNode(context);
        });
    }

    // cacher runtime noden...
    public final MethodHandle toMH(ServiceProvidingManager spm) {
        return spm.handlers.computeIfAbsent(this, k -> {
            MethodHandle mh = k.newMH(spm);
            if (!mh.type().parameterList().equals(List.of(NodeStore.class))) {
                throw new IllegalStateException("Must create node type of " + mh + " for " + getClass());
            }
            return mh;
        });
    }
}
