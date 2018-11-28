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
package packed.internal.inject.runtime;

import static java.util.Objects.requireNonNull;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.inject.Provider;
import packed.internal.inject.builder.AbstractBuildNode;

/**
 * An runtime service node holding {@link BindingMode#SINGLETON} instances. This node also holds
 * {@link BindingMode#LAZY} instances that are created at configuration.
 */
public final class RuntimeServiceNodeSingleton<T> extends RuntimeServiceNode<T> implements Provider<T> {

    /**
     * The binding mode, we save it to distinguish between lazy and non-lazy services. Even if the lazy service was
     * initialized while building the injector.
     */
    private final BindingMode bindingMode;

    /** The singleton instance. */
    private final T instance;

    /**
     * Creates a new instance node from the specified build node.
     *
     * @param buildNode
     *            the node to create this node from
     */
    public RuntimeServiceNodeSingleton(AbstractBuildNode<T> buildNode, T instance, BindingMode bindingMode) {
        super(buildNode);
        if (instance == null) {
            System.out.println(buildNode.getKey());
        }
        this.instance = requireNonNull(instance);
        this.bindingMode = requireNonNull(bindingMode);
    }

    /** {@inheritDoc} */
    @Override
    public T get() {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return bindingMode;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }
}
