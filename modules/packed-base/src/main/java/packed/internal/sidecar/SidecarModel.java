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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import packed.internal.classscan.InstantiatorBuilder;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public abstract class SidecarModel<T> {

    private final MethodHandle constructor;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder.
     */
    protected SidecarModel(Builder<T> builder) {
        this.constructor = builder.ib.build();
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

        // If we get a shared Sidecar we can have a single MethodHandle configure
        Builder(Class<?> implementation) {
            ib = InstantiatorBuilder.of(MethodHandles.lookup(), implementation);
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
