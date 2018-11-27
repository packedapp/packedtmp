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
package packed.internal.inject.providers;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.inject.InjectionException;
import app.packed.inject.Provider;

/**
 *
 */
public class MethodHandleStaticArgumentsProvider<T> implements Provider<T> {

    private final Object[] arguments;

    private final MethodHandle methodHandle;

    /**
     * @param methodHandle
     * @param arguments
     */
    public MethodHandleStaticArgumentsProvider(MethodHandle methodHandle, Object[] arguments) {
        this.methodHandle = requireNonNull(methodHandle);
        this.arguments = requireNonNull(arguments);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        try {
            return (T) methodHandle.invokeWithArguments(arguments);
        } catch (Throwable e) {
            throw new InjectionException("foo", e);
        }
    }
}
