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
package zamples;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import packed.internal.util.ThrowableUtil;

/**
 * A provider of object instances.
 */
//Is this in app.packed.service??? No its in the dependency model.
@FunctionalInterface
public interface Provider<T> {

    /**
     * Provides an instance of type {@code T}.
     *
     * @return the provided value
     * @throws RuntimeException
     *             if an exception is encountered while providing an instance
     */
    T provide();

    static <T> Provider<T> of(T t) {
        return new ConstantProvider<>(t);
    }
}

class InvokeExactProvider<T> implements Provider<T> {

    private final MethodHandle mh;

    InvokeExactProvider(MethodHandle mh) {
        this.mh = requireNonNull(mh);
    }

    /** {@inheritDoc} */
    @Override
    public T provide() {
        try {
            return (T) mh.invokeExact();
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }
}

class ConstantProvider<T> implements Provider<T> {

    private final T t;

    public ConstantProvider(T t) {
        this.t = requireNonNull(t);
    }

    /** {@inheritDoc} */
    @Override
    public T provide() {
        return t;
    }
}