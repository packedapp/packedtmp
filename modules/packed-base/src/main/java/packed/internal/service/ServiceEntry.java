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
package packed.internal.service;

import app.packed.config.ConfigSite;
import app.packed.service.InstantiationMode;
import app.packed.service.PrototypeRequest;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.run.RuntimeEntry;
import packed.internal.util.KeyBuilder;

/**
 * A service node represent the provider of a service either at {@link BuildEntry build-time } or at {@link RuntimeEntry
 * runtime-time}.
 *
 * The reason for for separating them into two interfaces to avoid retaining any information that is not strictly needed
 * at runtime.
 */
public interface ServiceEntry<T> {

    /**
     * Returns the configuration site of this entry.
     * 
     * @return the configuration site of this entry
     */
    ConfigSite configSite();

    /**
     * Returns an instance.
     * 
     * @param request
     *            a request if needed by {@link #requiresPrototypeRequest()}
     * @return the instance
     */
    T getInstance(@Nullable PrototypeRequest request);

    // Rename to isResolved
    boolean hasUnresolvedDependencies();

    InstantiationMode instantiationMode();

    default boolean isPrivate() {
        return key().equals(KeyBuilder.INJECTOR_KEY);// || key().equals(KeyBuilder.CONTAINER_KEY);
    }

    /**
     * Returns any key that the entry is registered under. This is null for entries that are available for lookup
     *
     * @return any key that the entry is registered under.
     */
    @Nullable
    Key<?> key();

    /**
     * Returns whether or not this node needs a {@link PrototypeRequest} instance to be able to deliver a service.
     * <p>
     * It is unfortunately that we need this method, however, otherwise we would need to create an instance of
     * {@link PrototypeRequest} every time we requested a service. Including for something as innocent as get(service).
     * Something people would assume was garbage free.
     *
     * @return whether or not this node needs a {@link PrototypeRequest} instance to be able to deliver a service
     */
    // Technically we don't need this any more, after we have settled on a Component, Dependency, Key<?> format
    // However we keep it for now, because it might make sense to have a backtrack to the defining entity at some point.
    boolean requiresPrototypeRequest();

    /**
     * Converts this node to a run-time node if this node is a build-node, otherwise returns this. Build-nodes must always
     * return the same runtime node instance
     *
     * @return if build node converts to runtime node, if runtime node returns self
     */
    RuntimeEntry<T> toRuntimeEntry();
}

// /**
// * Returns the optional description of this service.
// *
// * @return the optional description of this service
// * @see ServiceComponentConfiguration#setDescription(String)
// */
// Optional<String> description();

//
// default Provider<T> createProvider(Injector injector, Container container, Component component, Key<T> key) {
// InjectionSite site = InternalInjectionSite.of(injector, container, component, key);
// return () -> getInstance(site);
// }
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