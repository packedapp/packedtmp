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

import java.util.Arrays;

import app.packed.container.InternalExtensionException;

/**
 *
 */
public abstract class ThrowableFactory<T extends Throwable> {

    public static final ThrowableFactory<InternalExtensionException> INTERNAL_EXTENSION_EXCEPTION_FACTORY = new ThrowableFactory<InternalExtensionException>() {

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

    public final T newThrowable(String message) {
        T t = newThrowable0(message);
        StackTraceElement[] stackTrace = t.getStackTrace();
        StackTraceElement[] ste = Arrays.copyOfRange(stackTrace, 3, stackTrace.length);
        t.setStackTrace(ste);
        return t;
    }

    public abstract T newThrowable(String message, Throwable cause);
}
