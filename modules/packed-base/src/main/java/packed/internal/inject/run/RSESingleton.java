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
package packed.internal.inject.run;

import static java.util.Objects.requireNonNull;

import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceRequest;
import packed.internal.inject.build.BuildEntry;

/**
 * An runtime service node holding {@link InstantiationMode#SINGLETON} instances. This node also holds
 * {@link InstantiationMode#LAZY} instances that was created at configuration time.
 */
public final class RSESingleton<T> extends RSE<T> implements Provider<T> {

    /**
     * The binding mode, we save it to distinguish between lazy and non-lazy services. Even if the lazy service was
     * initialized while building the injector.
     */
    private final InstantiationMode instantionMode;

    /** The singleton instance. */
    private final T instance;

    /**
     * Creates a new node.
     *
     * @param buildNode
     *            the node to create this node from
     * @param instance
     *            the singleton instance
     */
    public RSESingleton(BuildEntry<T> buildNode, T instance) {
        super(buildNode);
        this.instance = requireNonNull(instance);
        this.instantionMode = buildNode.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public T get() {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return instantionMode;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ServiceRequest ignore) {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }
}
