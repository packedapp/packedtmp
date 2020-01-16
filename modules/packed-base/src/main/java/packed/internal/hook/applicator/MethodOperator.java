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
package packed.internal.hook.applicator;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;

import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Foerst og fremmest typen... //maaske om den skal vaere async??
// Styring af exceptions???? Version 2
/// Men ja, mener helt at det er noget man goer i operatoren...
/// Alt hvad der ikke skal tilpasses en runtime...

// Den fungerede bare aldrig rigtig. Erstattet af InvocationTemplate

public abstract class MethodOperator<T> {

    protected MethodOperator() {}

    /**
     * Applies this operator to the specified method handle.
     * 
     * @param mh
     *            the method handle to apply the operator on
     * @return the result of applying this operator
     */
    public abstract T apply(MethodHandle mh);

    public T apply(MethodHandle mh, Object instance) {
        return apply(mh.bindTo(instance));
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
        public T apply(MethodHandle mh) {
            try {
                return (T) mh.invoke();
            } catch (Throwable e) {
                ThrowableUtil.throwIfUnchecked(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }

    static class RunnableInternalMethodOperation extends MethodOperator<Runnable> {

        /** {@inheritDoc} */
        @Override
        public Runnable apply(MethodHandle mh) {
            return new StaticRunnable(mh);
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
                ThrowableUtil.throwIfUnchecked(e);
                throw new UndeclaredThrowableException(e);
            }
        }
    }

}
//
/// **
// * Applies this operator the specified static method.
// *
// * @param caller
// * the caller
// * @param method
// * the method to apply the operator on
// * @return the result of applying the operator
// * @throws IllegalArgumentException
// * if the method is not static, or if the operator cannot be applied on the method. For example, if the
// * operator requires two parameter, but the method only takes one
// */
// public final T applyMethod(Lookup caller, Method method) {
// MethodHandle mh;
// try {
// mh = caller.unreflect(method);
// } catch (IllegalAccessException e) {
// throw new PackedIllegalAccessException(e);
// }
// return apply(mh);
// }
