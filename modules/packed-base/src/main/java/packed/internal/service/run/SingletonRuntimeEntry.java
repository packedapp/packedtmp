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
package packed.internal.service.run;

import app.packed.config.ConfigSite;
import app.packed.service.InstantiationMode;
import app.packed.service.PrototypeRequest;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.util.Provider;
import packed.internal.service.build.BuildEntry;

/**
 * An runtime service node holding {@link InstantiationMode#SINGLETON} instances.
 */
public final class SingletonRuntimeEntry<T> extends RuntimeEntry<T> implements Provider<T> {

    /** The singleton instance. */
    @Nullable
    private final T instance;

    /**
     * Creates a new node.
     *
     * @param buildNode
     *            the node to create this node from
     * @param instance
     *            the singleton instance
     */
    public SingletonRuntimeEntry(BuildEntry<T> buildNode, @Nullable T instance) {
        super(buildNode);
        this.instance = instance;
    }

    /**
     * @param configSite
     * @param key
     * @param description
     */
    public SingletonRuntimeEntry(ConfigSite configSite, Key<T> key, @Nullable String description, @Nullable T instance) {
        super(configSite, key, description);
        this.instance = instance;
    }

    /** {@inheritDoc} */
    @Override
    public T get() {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(PrototypeRequest ignore) {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return InstantiationMode.SINGLETON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
