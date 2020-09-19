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

import app.packed.sidecar.MethodSidecar;
import packed.internal.inject.util.InstantiatorBuilder;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public abstract class SidecarModel<T> {

    /** A cache of models. */
    private static final ClassValue<SidecarModel<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected SidecarModel<?> computeValue(Class<?> implementation) {
            // Problemet er lidt referencer paa kryds og tvaers mellem modeller....
            // Vi kan i virkeligheden godt ende op med nogle cirkler
            // Tror maaske vi bare gennem klasse for nu...

            SidecarModel.Builder<?, ?> b;
            if (MethodSidecar.class.isAssignableFrom(implementation)) {
                b = new MethodSidecarModel.Builder(implementation);
            } else {
                throw new IllegalArgumentException("" + implementation);
            }

            b.configure();
            return b.build();
        }
    };

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder.
     */
    protected SidecarModel(Builder<T, ?> builder) {}

    public static SidecarModel<?> of(Class<?> implementation) {
        return CACHE.get(implementation);
    }

    static <T extends SidecarModel<T>> T of(Class<T> sidecarType, Class<?> implementation) {

        // Er der nogengang vi ved
        // hellere Maaske hellere <T extends SidecarModel> of (Class<T> sidecarType, Class<?> implementation)
        throw new UnsupportedOperationException();
    }

    public static MethodSidecarModel ofMethod(Class<?> implementation) {
        return (MethodSidecarModel) CACHE.get(implementation);
    }

    /** A builder for a sidecar model. */
    static abstract class Builder<T, C> {

        /** The configuration object that the sidecar implementation can use. */
        private final C configuration;

        final InstantiatorBuilder ib;

        /** A method handle that can call the sidecar's configure method. */
        private final MethodHandle mhConfigure;

        /** A var handle */
        private final VarHandle vhConfiguration;

        protected Object instance;

        // If we get a shared Sidecar we can have a single MethodHandle configure
        Builder(VarHandle vh, MethodHandle configure, Class<?> implementation, C context) {
            this.vhConfiguration = requireNonNull(vh);
            this.mhConfigure = requireNonNull(configure);
            this.configuration = requireNonNull(context);
            ib = InstantiatorBuilder.of(MethodHandles.lookup(), implementation);
            // validate extension
        }

        /**
         * Build the new sidecar model.
         * 
         * @return the new sidecar model
         */
        protected abstract SidecarModel<T> build();

        protected final C configuration() {
            return configuration;
        }

        private void configure() {
            // We perform a compare and exchange with configuration. Guarding against
            // concurrent usage of this bundle.
            // Don't think it makes sense to register

            MethodHandle constructor = ib.build();
            try {
                instance = constructor.invoke();
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }

            Object existing = vhConfiguration.compareAndExchange(instance, null, configuration);
            if (existing == null) {
                try {
                    // Invokes app.packed.component.Bundle#configure()
                    mhConfigure.invoke(instance);
                } catch (Throwable e) {
                    throw ThrowableUtil.orUndeclared(e);
                } finally {
                    // sets Bundle.configuration to a marker that indicates the bundle has been consumed
                    vhConfiguration.setVolatile(instance, null);
                }
            } else {
                // Can be this thread or another thread that is already using the bundle.
                throw new IllegalStateException("This bundle is currently being used elsewhere, type = " + instance.getClass());
            }
        }

        private void scan() {

        }
    }

    // InjectableFunctionSidecar??
    // Naah, why? tanker bare at FunctionSidecar()
}
