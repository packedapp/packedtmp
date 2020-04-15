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
package packed.internal.util;

import java.lang.reflect.Method;

/**
 *
 */
// Because a Pitcher throws stuff
public abstract class Pitcher {
    AssertionErrorPitcher ASSERTION_ERROR_PITCHER = new AssertionErrorPitcher();
    final Class<? extends RuntimeException> throwableType;

    Pitcher(Class<? extends RuntimeException> throwableType) {
        if (!(RuntimeException.class.isAssignableFrom(throwableType))) {
            throw new IllegalArgumentException("Not a runtime exception, type " + throwableType);
        }
        this.throwableType = throwableType;
    }

    public MethodBuilder forMethod(Method m) {
        throw new UnsupportedOperationException();
    }

    protected abstract RuntimeException newThrowable(String message, Throwable cause);

    protected abstract RuntimeException newThrowable0(String message);

    public static class AssertionErrorPitcher extends Pitcher {

        AssertionErrorPitcher() {
            super(ConvertableAssertionErrorException.class);
        }

        /** {@inheritDoc} */
        @Override
        protected RuntimeException newThrowable(String message, Throwable cause) {
            return new ConvertableAssertionErrorException(message, cause);
        }

        /** {@inheritDoc} */
        @Override
        protected RuntimeException newThrowable0(String message) {
            return new ConvertableAssertionErrorException(message);
        }

        public static void autoUnwrap(Runnable r) {
            try {
                r.run();
            } catch (ConvertableAssertionErrorException e) {
                AssertionError ae = new AssertionError(e.getMessage());
                ae.setStackTrace(e.getStackTrace());
                throw ae;
            }
        }

        static class ConvertableAssertionErrorException extends RuntimeException {

            /** */
            private static final long serialVersionUID = 1L;

            /**
             * @param message
             */
            public ConvertableAssertionErrorException(String message) {
                super(message);
            }

            /**
             * @param message
             * @param cause
             */
            public ConvertableAssertionErrorException(String message, Throwable cause) {
                super(message, cause);
            }

        }
    }

    public class MethodBuilder {
        void fail() {}

        String toMessage() {
            return "fff";
        }
    }
}
