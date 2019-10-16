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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Semaphore;

import app.packed.service.InstantiationMode;
import app.packed.service.PrototypeRequest;
import packed.internal.inject.util.Provider;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.service.ComponentFactoryBuildEntry;
import packed.internal.util.ThrowableUtil;

/** A lazy runtime node if the service was not requested at configuration time. */
public final class LazyRuntimeEntry<T> extends RuntimeEntry<T> implements Provider<T> {

    /** The lazily instantiated instance. */
    private volatile Object instance;

    /**
     * Creates a new node
     * 
     * @param node
     *            the build node to create this node from
     */
    public LazyRuntimeEntry(ComponentFactoryBuildEntry<T> node) {
        super(node);
        this.instance = new Sync(new PrototypeRuntimeEntry<>(node));
    }

    public LazyRuntimeEntry(BuildEntry<T> node, T instance) {
        super(node);
        this.instance = requireNonNull(instance);
    }

    @Override
    public void initInstance(T instance) {
        // This is method is only ever called doing the container construction.
        // So there is no need to synchronize in order to make sure only one instance is created.
        // However, if we allow lazy creation, for example, via implementing Provider.
        // We need some way to make sure exactly one instance is created.
        // Maybe the container should always just call #getInstance() on this class.

        this.instance = requireNonNull(instance);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public T getInstance(PrototypeRequest site) {
        for (;;) {
            Object i = instance;
            if (!(i instanceof LazyRuntimeEntry.Sync)) {
                return (T) i;
            }
            ((LazyRuntimeEntry.Sync) i).tryCreate();
        }
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return InstantiationMode.LAZY;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }

    /** A helper class for lazy calculating the value */
    @SuppressWarnings("serial")
    final class Sync extends Semaphore {

        /** The factory used for creating a new instance. */
        private PrototypeRuntimeEntry<T> factory;

        /** Any failure encountered while creating a new value. */
        private Throwable failure;

        /**
         * Creates a new Sync object
         * 
         * @param factory
         *            the factory node that will create the value
         */
        Sync(PrototypeRuntimeEntry<T> factory) {
            super(1);
            this.factory = requireNonNull(factory);
        }

        /**
         * Try and create a new value.
         */
        void tryCreate() {
            acquireUninterruptibly();
            try {
                if (instance == this) {
                    if (failure != null) {
                        // We should not Rethrow it, We need to wrap it in some ProvisionException
                        ThrowableUtil.rethrowErrorOrRuntimeException(failure);
                    }
                    try {
                        T newInstance = factory.get();
                        if (newInstance == null) {
                            // We need to check null and type.... Maybe common method on RuntimeEntry
                            // TODO throw Provision Exception instead
                            requireNonNull(newInstance, "factory produced a null instance");
                        }
                        instance = newInstance;
                    } catch (Throwable e) {
                        failure = e;
                        throw e;
                    } finally {
                        factory = null;
                    }
                }
            } finally {
                release();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public T get() {
        return getInstance(null);
    }
}
