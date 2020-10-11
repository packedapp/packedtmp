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

import packed.internal.classscan.InstantiatorBuilder;
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

    private final MethodHandle constructor;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder.
     */
    protected SidecarModel(Builder<T> builder) {
        this.constructor = requireNonNull(builder.constructor);
    }

    public final Object newSidecar() {
        try {
            return constructor.invoke();
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /** A builder for a sidecar model. */
    static abstract class Builder<T> {

        final InstantiatorBuilder ib;

        private MethodHandle constructor;

        // If we get a shared Sidecar we can have a single MethodHandle configure
        Builder(Class<?> implementation) {
            ib = InstantiatorBuilder.of(MethodHandles.lookup(), implementation);
            constructor = ib.build();
            // validate extension
        }

        /**
         * Build the new sidecar model.
         * 
         * @return the new sidecar model
         */
        protected abstract SidecarModel<T> build();
    }
}
