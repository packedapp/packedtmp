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
package packed.internal.service.runtime;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Semaphore;

import app.packed.inject.Provider;
import app.packed.service.ProvideContext;
import app.packed.service.ServiceMode;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.service.ComponentFactoryBuildEntry;
import packed.internal.util.ThrowableUtil;

/** A lazy runtime node if the service was not requested at configuration time. */
// we don't support lazy services anymore.
// But we keep this around if we want to support lazy instantiated components at some point
public final class LazyInjectorEntry<T> extends InjectorEntry<T> implements Provider<T> {

    /** The lazily instantiated instance. */
    private volatile Object instance;

    /**
     * Creates a new node
     * 
     * @param node
     *            the build node to create this node from
     */
    public LazyInjectorEntry(ComponentFactoryBuildEntry<T> node, ServiceExtensionInstantiationContext context) {
        super(node);
        this.instance = new Sync(new PrototypeInjectorEntry<>(node, context));
    }

    public LazyInjectorEntry(BuildEntry<T> node, T instance) {
        super(node);
        this.instance = requireNonNull(instance);
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public T getInstance(ProvideContext site) {
        for (;;) {
            Object i = instance;
            if (!(i instanceof LazyInjectorEntry.Sync)) {
                return (T) i;
            }
            ((LazyInjectorEntry.Sync) i).tryCreate();
        }
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
        private PrototypeInjectorEntry<T> factory;

        /** Any failure encountered while creating a new value. */
        private Throwable failure;

        /**
         * Creates a new Sync object
         * 
         * @param factory
         *            the factory node that will create the value
         */
        Sync(PrototypeInjectorEntry<T> factory) {
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
                        ThrowableUtil.throwIfUnchecked(failure);
                    }
                    try {
                        T newInstance = factory.newInstance();
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
    public T provide() {
        return getInstance(null);
    }
}