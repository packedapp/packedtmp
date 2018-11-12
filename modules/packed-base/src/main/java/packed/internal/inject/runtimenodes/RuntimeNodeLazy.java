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
package packed.internal.inject.runtimenodes;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Semaphore;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.util.Nullable;
import packed.internal.inject.buildnodes.BuildNode;
import packed.internal.inject.factory.InternalFactory;

/** A runtime node for an instance that have not yet been requested for the first time. */
public final class RuntimeNodeLazy<T> extends RuntimeNode<T> {

    /** The lazily instantiated instance. */
    @Nullable
    private volatile T instance;

    /** Lazy calculates the value, and throws away the factory afterwards. */
    @Nullable
    private volatile Sync lazy;

    /**
     * Creates a new node
     * 
     * @param node
     *            the build node to create this node from
     * @param factory
     *            the factory that will create the instance
     */
    public RuntimeNodeLazy(BuildNode<T> node, InternalFactory<T> factory) {
        super(node);
        this.lazy = new Sync(new RuntimeNodeFactory<>(node, factory));
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return BindingMode.LAZY_SINGLETON;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        for (;;) {
            T i = instance;
            if (i != null) {
                return i;
            }

            // Lazy calculate the value
            Sync l = lazy;
            if (l != null) {
                l.update(); // updates this.instance, and null itself out
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }

    /** A helper class for lazy calculating the value */
    @SuppressWarnings("serial")
    final class Sync extends Semaphore {

        /** The factory used for creating a new instance. */
        private final RuntimeNodeFactory<T> factory;

        /**
         * Creates a new Sync object
         * 
         * @param factory
         *            the actual factory node that will create the value
         */
        Sync(RuntimeNodeFactory<T> factory) {
            super(1);
            this.factory = requireNonNull(factory);
        }

        void update() {
            acquireUninterruptibly();
            try {
                if (lazy != null) {
                    // TODO I don't think we want to call the factory multiple times, maybe store the
                    // exception
                    T newInstance = factory.get();
                    if (newInstance == null) {
                        requireNonNull(instance, "factory produced a null instance");
                    }
                    instance = newInstance;
                    lazy = null;
                }
            } finally {
                release();
            }
        }
    }
}
