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
package app.packed.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.container.extension.AnnotatedMethodHook;
import packed.internal.reflect.PackedIllegalAccessException;
import packed.internal.util.StringFormatter;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Foerst og fremmest typen... //maaske om den skal vaere async??
// Styring af exceptions???? Version 2
/// Men ja, mener helt at det er noget man goer i operatoren...
/// Alt hvad der ikke skal tilpasses en runtime...

public abstract class MethodOperator<T> {

    /**
     * Applies this operator the specified static method.
     * 
     * @param caller
     *            the caller
     * @param method
     *            the method to apply the operator on
     * @return the result of applying the operator
     * @throws IllegalArgumentException
     *             if the method is not static, or if the operator cannot be applied on the method. For example, if the
     *             operator requires two parameter, but the method only takes one
     */
    public final T applyStatic(Lookup caller, Method method) {
        MethodHandle mh;
        try {
            mh = caller.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new PackedIllegalAccessException(e);
        }
        return apply(mh);
    }

    public abstract MethodType type();

    public abstract T applyStaticHook(AnnotatedMethodHook<?> packedAnnotatedMethodHook);

    public abstract T apply(MethodHandle mh);

    public static <T> MethodOperator<T> cast(MethodOperator<T> operator) {
        requireNonNull(operator, "operator is null");
        if (!(operator instanceof MethodOperator)) {
            throw new IllegalArgumentException("Custom implementations of " + MethodOperator.class.getSimpleName() + " are not supported, type = "
                    + StringFormatter.format(operator.getClass()));
        }
        return operator;
    }

    public static <T> MethodOperator<Object> invokeOnce() {
        return new MethodOperator.InvokeOnce<>();
    }

    public static MethodOperator<Runnable> runnable() {
        return new MethodOperator.RunnableInternalMethodOperation();
    }

    private static class InvokeOnce<T> extends MethodOperator<T> {

        /** {@inheritDoc} */
        @Override
        public T applyStaticHook(AnnotatedMethodHook<?> hook) {
            return apply(hook.methodHandle());
        }

        /** {@inheritDoc} */
        @Override
        public T apply(MethodHandle mh) {
            try {
                return (T) mh.invoke();
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }

        /** {@inheritDoc} */
        @Override
        public MethodType type() {
            return MethodType.methodType(void.class);
        }
    }

    static class RunnableInternalMethodOperation extends MethodOperator<Runnable> {

        public Runnable apply(Lookup lookup, Method method, Object instance) {
            MethodHandle mh;
            try {
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new PackedIllegalAccessException(e);
            }
            mh = mh.bindTo(instance);
            return new StaticRunnable(mh);
        }

        /** {@inheritDoc} */
        @Override
        public Runnable applyStaticHook(AnnotatedMethodHook<?> hook) {
            return new StaticRunnable(hook.methodHandle());
        }

        /** {@inheritDoc} */
        @Override
        public Runnable apply(MethodHandle mh) {
            return new StaticRunnable(mh);
        }

        /** {@inheritDoc} */
        @Override
        public MethodType type() {
            return MethodType.methodType(void.class);
        }
    }

    private static class StaticRunnable implements Runnable {
        private final MethodHandle mh;

        /**
         * @param mh
         */
        public StaticRunnable(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            try {
                mh.invoke();
            } catch (Throwable e) {
                ThrowableUtil.rethrowErrorOrRuntimeException(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }

}
