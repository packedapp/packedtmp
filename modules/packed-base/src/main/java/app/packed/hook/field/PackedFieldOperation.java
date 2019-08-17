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
package app.packed.hook.field;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Supplier;

import app.packed.hook.FieldOperator;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public abstract class PackedFieldOperation<T> implements FieldOperator<T> {

    @Override
    public abstract T applyStatic(MethodHandles.Lookup lookup, Field field);

    public void checkAccessMode(AccessMode accessMode) {
        // SET does not make sense for GetOnce
    }

    public abstract T doItStatic(VarHandle handle, boolean isVolatile);

    public abstract T invokeGetter(MethodHandle mh);

    public static class GetOnceInternalFieldOperation<T> extends PackedFieldOperation<T> {
        Class<?> fieldType;

        /** {@inheritDoc} */
        @Override
        public T apply(Lookup lookup, Field field, Object instance) {
            MethodHandle mh;
            try {
                mh = lookup.unreflectGetter(field);
            } catch (IllegalAccessException e) {
                throw new PackedIllegalAccessException(e);
            }
            mh = mh.bindTo(instance);
            // Den her method handle burde jo kunne caches...

            return invokeGetter(mh);
        }

        /** {@inheritDoc} */
        @Override
        public T applyStatic(Lookup lookup, Field field) {
            MethodHandle mh;
            try {
                mh = lookup.unreflectGetter(field);
            } catch (IllegalAccessException e) {
                throw new PackedIllegalAccessException(e);
            }
            // Den her method handle burde jo kunne caches...

            return invokeGetter(mh);
        }

        /** {@inheritDoc} */
        @Override
        public T doItStatic(VarHandle varHandle, boolean isVolatile) {
            if (isVolatile) {
                return (T) varHandle.getVolatile();
            } else {
                return (T) varHandle.get();
            }
        }

        /** {@inheritDoc} */
        @Override
        public T invokeGetter(MethodHandle mh) {
            try {
                return (T) mh.invoke();
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    public static class StaticSup<T> implements Supplier<T> {
        private final MethodHandle mh;

        /**
         * @param mh
         */
        public StaticSup(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        /** {@inheritDoc} */
        @Override
        public T get() {
            try {
                return (T) mh.invoke();
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    public static class SupplierInternalFieldOperation<T> extends PackedFieldOperation<Supplier<T>> {
        Class<?> fieldType;

        /** {@inheritDoc} */
        @Override
        public Supplier<T> applyStatic(Lookup lookup, Field field) {
            MethodHandle mh;
            try {
                mh = lookup.unreflectGetter(field);
            } catch (IllegalAccessException e) {
                throw new PackedIllegalAccessException(e);
            }
            return new StaticSup<T>(mh);
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<T> doItStatic(VarHandle varHandle, boolean isVolatile) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<T> invokeGetter(MethodHandle mh) {
            return new StaticSup<T>(mh);
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<T> apply(Lookup lookup, Field field, Object instance) {
            MethodHandle mh;
            try {
                mh = lookup.unreflectGetter(field);
            } catch (IllegalAccessException e) {
                throw new PackedIllegalAccessException(e);
            }
            mh = mh.bindTo(instance);
            return new StaticSup<T>(mh);
        }
    }
}
