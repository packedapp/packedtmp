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
package packed.internal.container.extension.hook;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.function.Supplier;

import app.packed.container.extension.FieldOperator;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public abstract class PackedFieldOperator<T> implements FieldOperator<T> {

    public static <T> PackedFieldOperator<T> cast(FieldOperator<T> operator) {
        requireNonNull(operator, "operator is null");
        if (!(operator instanceof PackedFieldOperator)) {
            throw new IllegalArgumentException("Custom implementations of " + FieldOperator.class.getSimpleName() + " are not supported, type = "
                    + StringFormatter.format(operator.getClass()));
        }
        return (PackedFieldOperator<T>) operator;
    }

    /** {@inheritDoc} */
    @Override
    public FieldOperator<T> requireFinal() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final T applyStatic(Lookup lookup, Field field) {
        MethodHandle mh;
        try {
            mh = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new PackedIllegalAccessException(e);
        }
        // Den her method handle burde jo kunne caches...

        return invoke(mh);
    }

    /** {@inheritDoc} */
    @Override
    public final T apply(Lookup lookup, Field field, Object instance) {
        MethodHandle mh;
        try {
            mh = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new PackedIllegalAccessException(e);
        }
        mh = mh.bindTo(instance);
        // Den her method handle burde jo kunne caches...
        return invoke(mh);
    }

    public abstract T invoke(MethodHandle mh);

    public abstract T applyStaticHook(PackedAnnotatedFieldHook<?> hook);

    // If its a getter we cache the method handle
    public abstract boolean isSimpleGetter();

    public static class GetOnceInternalFieldOperation<T> extends PackedFieldOperator<T> {
        Class<?> fieldType;

        /** {@inheritDoc} */
        @Override
        public T invoke(MethodHandle mh) {
            try {
                return (T) mh.invoke();
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isSimpleGetter() {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public T applyStaticHook(PackedAnnotatedFieldHook<?> hook) {
            return invoke(hook.getter());
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

    public static class SupplierInternalFieldOperation<T> extends PackedFieldOperator<Supplier<T>> {
        Class<?> fieldType;

        /** {@inheritDoc} */
        @Override
        public Supplier<T> invoke(MethodHandle mh) {
            return new StaticSup<T>(mh);
        }

        /** {@inheritDoc} */
        @Override
        public boolean isSimpleGetter() {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public Supplier<T> applyStaticHook(PackedAnnotatedFieldHook<?> hook) {
            return new StaticSup<T>(hook.getter());
        }
    }
}
