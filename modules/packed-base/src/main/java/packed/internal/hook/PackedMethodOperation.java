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
package packed.internal.hook;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import app.packed.hook.MethodOperator;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public abstract class PackedMethodOperation<T> implements MethodOperator<T> {

    abstract T applyStaticHook(PackedAnnotatedMethodHook<?> packedAnnotatedMethodHook);

    public static class InvokeOnce<T> extends PackedMethodOperation<T> {

        /** {@inheritDoc} */
        @Override
        public T applyStatic(Lookup lookup, Method method) {

            MethodHandle mh;
            try {
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new PackedIllegalAccessException(e);
            }
            // Den her method handle burde jo kunne caches...
            return invoke(mh);
        }

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
        T applyStaticHook(PackedAnnotatedMethodHook<?> hook) {
            return invoke(hook.methodHandle());
        }
    }

    public static class RunnableInternalMethodOperation extends PackedMethodOperation<Runnable> {

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
        public Runnable applyStatic(Lookup lookup, Method method) {
            MethodHandle mh;
            try {
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException e) {
                throw new PackedIllegalAccessException(e);
            }
            return new StaticRunnable(mh);
        }

        /** {@inheritDoc} */
        @Override
        public Runnable invoke(MethodHandle mh) {
            return new StaticRunnable(mh);
        }

        /** {@inheritDoc} */
        @Override
        Runnable applyStaticHook(PackedAnnotatedMethodHook<?> hook) {
            return new StaticRunnable(hook.methodHandle());
        }
    }

    public static class StaticRunnable implements Runnable {
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
