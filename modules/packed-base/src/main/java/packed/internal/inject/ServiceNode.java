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
package packed.internal.inject;

import app.packed.container.Component;
import app.packed.inject.Dependency;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvidesHelper;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.runtime.RuntimeServiceNode;
import packed.internal.util.KeyBuilder;

/**
 * A node is the base interface for internally registered services.
 *
 * The reason for for separating them into two interfaces to avoid retaining any information that is not strictly needed
 * at runtime.
 */
// BuildNode also implements Service, because it must available at some point???Tjah...
public interface ServiceNode<T> extends ServiceDescriptor {
    //
    // default Provider<T> createProvider(Injector injector, Container container, Component component, Key<T> key) {
    // InjectionSite site = InternalInjectionSite.of(injector, container, component, key);
    // return () -> getInstance(site);
    // }

    default T getInstance(Injector injector, Dependency dependency, @Nullable Component component) {
        return getInstance(ProvidesHelper.of(injector, dependency));
    }

    InstantiationMode instantiationMode();

    default T getInstance(Injector injector, Key<T> key, @Nullable Component component) {
        return getInstance(ProvidesHelper.of(injector, key));
    }

    T getInstance(ProvidesHelper site);

    /**
     * Returns whether or not this node needs a {@link ProvidesHelper} instance to be able to deliver a service.
     *
     * @return whether or not this node needs a {@link ProvidesHelper} instance to be able to deliver a service
     */
    boolean needsInjectionSite();

    boolean needsResolving();

    /**
     * Converts this node to a run-time node if a build-node, otherwise returns this. Build-nodes must return the same
     * instance
     *
     * @return if build node converts to runtime node, if runtime node returns self
     */
    RuntimeServiceNode<T> toRuntimeNode();

    default boolean isPrivate() {
        return key().equals(KeyBuilder.INJECTOR_KEY) || key().equals(KeyBuilder.CONTAINER_KEY);
    }
}

// default void validateKey(Key<?> key) {}

/// **
// * Returns the description of this node.
// * <p>
// * For a runtime node this is static, for a build node this can change during the lifetime of the node.
// *
// * @return the description of this node
// */
// @Override
// String getDescription();
/**
 * Returns the key of this node.
 * <p>
 * For a runtime node this is static, for a build node this can change during the lifetime of the node.
 *
 * @return the key of this node
 */
// @Override
// Key<?> getKey();
//
// default T getInstance(Injector injector, Container container, Component component, Class<T> key) {
// return getInstance(InternalInjectionSite.of(injector, Key.of(key), container, component));
// }

//
// default Provider<T> createProvider(Injector injector, Container container, Component component, Class<T> key) {
// if (needsInjectionSite()) {
// return () -> getInstance(injector, container, component, key);
// } else {
// return createProvider(injector, container, component, Key.of(key));
// }
// }