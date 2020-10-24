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
package packed.internal.errorhandling;

import java.lang.reflect.Method;
import java.util.Arrays;

import app.packed.cube.InternalExtensionException;
import packed.internal.util.ThrowableUtil;

/**
 *
 */

// TODO we can bruge dem som en builder af en slags...
// Make rename to something like UncheckedThrowableFactory....
// Og saa laver vi et check

// HVorfor var det lige jeg lavede den om...... Hmmm
// I think I got tired of having throws T on every method that could fail.

public abstract class UncheckedThrowableFactory<T extends RuntimeException> {

    public static final UncheckedThrowableFactory<InternalExtensionException> INTERNAL_EXTENSION_EXCEPTION_FACTORY = new UncheckedThrowableFactory<InternalExtensionException>() {

        /** {@inheritDoc} */
        @Override
        protected InternalExtensionException newThrowable0(String message) {
            return new InternalExtensionException(message);
        }

        /** {@inheritDoc} */
        @Override
        public InternalExtensionException newThrowable(String message, Throwable cause) {
            return new InternalExtensionException(message, cause);
        }
    };

    public final T newThrowable(CharSequence msg) {
        return newThrowable(msg.toString());
    }

    protected abstract T newThrowable0(String message);

    public final void fail(String message) {
        // Kan vel bare bruge onUndeclared()...
        ThrowableUtil.throwAny(newThrowable(message, 1));
    }

    public final T newThrowable(String message) {
        T t = newThrowable0(message);
        StackTraceElement[] stackTrace = t.getStackTrace();
        StackTraceElement[] ste = Arrays.copyOfRange(stackTrace, 3, stackTrace.length);
        t.setStackTrace(ste);
        return t;
    }

    public final T newThrowable(String message, int depth) {
        T t = newThrowable0(message);
        StackTraceElement[] stackTrace = t.getStackTrace();
        StackTraceElement[] ste = Arrays.copyOfRange(stackTrace, 3 + depth, stackTrace.length);
        t.setStackTrace(ste);
        return t;
    }

    public final T newThrowableForMethod(String message, Method method) {
        // We need to keep track of the depth...
        return newThrowable(message + " for method  " + method, 1);
    }

    public abstract T newThrowable(String message, Throwable cause);

    public static class AssertionErrorRuntimeException extends RuntimeException {

        /**
         * @param message
         * @param cause
         */
        public AssertionErrorRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * @param message
         */
        public AssertionErrorRuntimeException(String message) {
            super(message);
        }

        public AssertionError convert() {
            AssertionError ar = new AssertionError(getMessage());
            ar.setStackTrace(getStackTrace());
            return ar;
        }

        /** */
        private static final long serialVersionUID = 1L;

        public static final UncheckedThrowableFactory<AssertionErrorRuntimeException> FACTORY = new UncheckedThrowableFactory<>() {

            /** {@inheritDoc} */
            @Override
            protected AssertionErrorRuntimeException newThrowable0(String message) {
                return new AssertionErrorRuntimeException(message);
            }

            /** {@inheritDoc} */
            @Override
            public AssertionErrorRuntimeException newThrowable(String message, Throwable cause) {
                return new AssertionErrorRuntimeException(message, cause);
            }
        };

    }
}
