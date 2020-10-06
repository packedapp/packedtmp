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
package packed.internal.sidecar;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import packed.internal.classscan.util.InstantiatorBuilder;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
/// Taget fra noget gammel kode
// 1. Look for class annotations, that changes the packlet system model

// 2. Look for packlet class annotations... Can change the "model"

// 3. Constructor needs to take 1+2 into consideration

// 3. Look for field and member annotations taking 1+2 into consideration

//
public abstract class SidecarModel<T> {

    private final Object instance;

    public Object instance() {
        return instance;
    }

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder.
     */
    protected SidecarModel(Builder<T> builder) {
        this.instance = requireNonNull(builder.instance);
    }

    static <T extends SidecarModel<T>> T ofd(Class<T> sidecarType, Class<?> implementation) {

        @SuppressWarnings("unused")
        Runnable eclipseBug = () -> {};
        // Er der nogengang vi ved
        // hellere Maaske hellere <T extends SidecarModel> of (Class<T> sidecarType, Class<?> implementation)
        throw new UnsupportedOperationException();
    }

    /** A builder for a sidecar model. */
    static abstract class Builder<T> {

        final InstantiatorBuilder ib;

        /** A method handle that can call the sidecar's configure method. */
        private final MethodHandle mhConfigure;

        /** A var handle for setting the configuration's object */
        private final VarHandle vhConfiguration;

        /** The sidecar instance. */
        protected Object instance;

        // If we get a shared Sidecar we can have a single MethodHandle configure
        Builder(VarHandle vh, MethodHandle configure, Class<?> implementation) {
            this.vhConfiguration = requireNonNull(vh);
            this.mhConfigure = requireNonNull(configure);
            ib = InstantiatorBuilder.of(MethodHandles.lookup(), implementation);
            // validate extension
        }

        /**
         * Build the new sidecar model.
         * 
         * @return the new sidecar model
         */
        protected abstract SidecarModel<T> build();

        protected final void configure() {
            // We perform a compare and exchange with configuration. Guarding against
            // concurrent usage of this bundle.
            // Don't think it makes sense to register

            MethodHandle constructor = ib.build();
            try {
                instance = constructor.invoke();
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }

            vhConfiguration.set(instance, this);
            try {
                mhConfigure.invoke(instance); // Invokes sidecar#configure()
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                vhConfiguration.set(instance, null); // clears the configuration
            }
        }
    }
}
