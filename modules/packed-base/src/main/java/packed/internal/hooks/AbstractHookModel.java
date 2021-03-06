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
package packed.internal.hooks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

import packed.internal.invoke.MemberScanner;
import packed.internal.invoke.MethodHandleBuilder;
import packed.internal.invoke.OpenClass;
import packed.internal.util.ThrowableUtil;

/** An abstract base class for bootstrap hook classes. */
public abstract class AbstractHookModel<T> {

    /** A method handle for the bootstrap's constructor. */
    private final MethodHandle mhConstructor; // ()Object

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder.
     */
    protected AbstractHookModel(Builder<T> builder) {
        MethodHandleBuilder mhb = MethodHandleBuilder.of(builder.oc.type());
        Constructor<?> constructor = MemberScanner.getConstructor(builder.oc.type(), false, e -> new IllegalArgumentException(e));
        this.mhConstructor = mhb.build(builder.oc, constructor);
    }

    /**
     * Returns the bootstrap class.
     * 
     * @return the bootstrap class
     */
    public final Class<?> bootstrapImplementation() {
        return mhConstructor.type().returnType();
    }

    /**
     * Returns a new bootstrap instance.
     * 
     * @return a new bootstrap instance
     * 
     */
    public final Object newInstance() {
        try {
            return mhConstructor.invoke();
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /** A builder for a bootstrap model. */
    static abstract class Builder<T> extends MemberScanner {

        final OpenClass oc;

        // If we get a shared Sidecar we can have a single MethodHandle configure
        /**
         * Creates a new builder.
         * 
         * @param bootstrapImplementation
         *            the bootstrap implementation
         */
        Builder(Class<?> bootstrapImplementation) {
            super(bootstrapImplementation);
            this.oc = OpenClass.of(MethodHandles.lookup(), bootstrapImplementation);
        }

        /**
         * Build the new model.
         * 
         * @return the new model
         */
        protected abstract AbstractHookModel<T> build();
    }

}
