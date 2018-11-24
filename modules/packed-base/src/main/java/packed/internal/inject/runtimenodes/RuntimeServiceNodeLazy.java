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
import app.packed.inject.Provider;
import app.packed.util.Nullable;
import packed.internal.inject.buildnodes.BuildNode;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.util.ThrowableUtil;

/** A runtime node for an instance that have not yet been requested for the first time. */
public final class RuntimeServiceNodeLazy<T> extends RuntimeServiceNode<T> {

    /** The lazily instantiated instance. */
    @Nullable
    private volatile T instance;

    /** Lazy calculates the value, and is then nulled out. */
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
    public RuntimeServiceNodeLazy(BuildNode<T> node, InternalFactory<T> factory) {
        super(node);
        this.lazy = new Sync(new RuntimeServiceNodePrototype<>(node, factory));
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return BindingMode.LAZY;
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
                i = l.tryCreate();
                if (i != null) {
                    instance = i;
                    lazy = null;
                    return i;
                }
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
        private Provider<T> factory;

        /** Any failure encountered while creating a new value. */
        private Throwable failure;

        /**
         * Creates a new Sync object
         * 
         * @param factory
         *            the factory node that will create the value
         */
        Sync(Provider<T> factory) {
            super(1);
            this.factory = requireNonNull(factory);
        }

        /**
         * Try and create a new value.
         * 
         * @return the newly created value, or null if the value has already been created
         */
        T tryCreate() {
            acquireUninterruptibly();
            try {
                if (failure != null) {
                    ThrowableUtil.rethrowErrorOrRuntimeException(failure);
                }
                if (factory != null) {
                    try {
                        T newInstance = factory.get();
                        if (newInstance == null) {
                            // TODO throw Provision Exception instead
                            requireNonNull(instance, "factory produced a null instance");
                        }
                        return newInstance;
                    } catch (RuntimeException | Error e) {
                        failure = e;
                        throw e;
                    } finally {
                        factory = null;
                    }
                }
            } finally {
                release();
            }
            return null;
        }
    }
}
