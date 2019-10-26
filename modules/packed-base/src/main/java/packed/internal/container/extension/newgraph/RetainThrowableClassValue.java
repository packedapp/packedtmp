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
package packed.internal.container.extension.newgraph;

import packed.internal.util.ThrowableUtil;

/**
 *
 */
public abstract class RetainThrowableClassValue<T> {

    private final ClassValue<T> actual = new ClassValue<>() {

        @Override
        protected T computeValue(Class<?> type) {
            Result<T> r = cv.get(type);
            if (r.cause != null) {
                ThrowableUtil.throwAny(r.cause);
            }
            // We null out the result, but keep the exception...
            T result = r.result;
            r.result = null;
            return result;
        }
    };

    private final ClassValue<Result<T>> cv = new ClassValue<>() {

        @Override
        protected Result<T> computeValue(Class<?> type) {
            T val = null;
            Throwable cause = null;
            try {
                val = RetainThrowableClassValue.this.computeValue(type);
            } catch (RuntimeException | Error e) {
                cause = e;
            }
            return new Result<T>(val, cause);
        }
    };

    protected abstract T computeValue(Class<?> type);

    public T get(Class<?> type) {
        return actual.get(type);
    }

    static class Result<T> {
        Throwable cause;
        // We should probably recalc the stack trace...
        T result;

        Result(T result, Throwable cause) {
            this.cause = cause;
            this.result = result;
        }

    }
}
