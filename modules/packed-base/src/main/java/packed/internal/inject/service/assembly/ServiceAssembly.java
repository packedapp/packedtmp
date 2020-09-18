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
package packed.internal.inject.service.assembly;

import static java.util.Objects.requireNonNull;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.Provide;
import app.packed.service.Service;
import packed.internal.inject.ContainerInjectionManager;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.inject.dependency.Injectable;
import packed.internal.inject.service.runtime.PackedService;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.inject.service.runtime.ServiceInstantiationContext;

/**
 * Build service entries ...node is used at configuration time, to make sure that multiple services with the same key
 * are not registered. And for helping in initialization dependency graphs. Build nodes has extra fields that are not
 * needed at runtime.
 * 
 * <p>
 * Instances of this class are never exposed to end users. But instead wrapped.
 */
public abstract class ServiceAssembly<T> implements DependencyProvider {

    /** The configuration site of this object. */
    private final ConfigSite configSite;

    /** The service no this entry belongs to. Or null for wirelets */
    @Nullable // Is nullable for stages for now
    public final ContainerInjectionManager im;

    /**
     * The key of the node (optional). Can be null, for example, for a class that is not exposed as a service but has
     * instance methods annotated with {@link Provide}. In which the case the declaring class needs to be constructor
     * injected before the providing method can be invoked.
     */
    private Key<T> key;

    public ServiceAssembly(@Nullable ContainerInjectionManager im, ConfigSite configSite, Key<T> key) {
        this.im = requireNonNull(im);
        this.configSite = requireNonNull(configSite);
        this.key = requireNonNull(key);
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

    @Override
    @Nullable
    public Injectable injectable() {
        throw new UnsupportedOperationException();
    }

    public int regionIndex() {
        throw new UnsupportedOperationException();
    }

    public final Key<T> key() {
        return key;
    }

    /**
     * Creates a new runtime node from this node.
     *
     * @return the new runtime node
     */
    protected abstract RuntimeService<T> newRuntimeNode(ServiceInstantiationContext context);

    public final Service toDescriptor() {
        return new PackedService(key, configSite);
    }

    // cacher runtime noden...
    @SuppressWarnings("unchecked")
    public final RuntimeService<T> toRuntimeEntry(ServiceInstantiationContext context) {
        return (RuntimeService<T>) context.transformers.computeIfAbsent(this, k -> {
            return k.newRuntimeNode(context);
        });
    }
}
