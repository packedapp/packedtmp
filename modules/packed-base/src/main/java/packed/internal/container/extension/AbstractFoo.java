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
package packed.internal.container.extension;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.UndeclaredThrowableException;

import packed.internal.util.ThrowableUtil;

/**
 *
 */
public abstract class AbstractFoo<T> {

    /** The method handle used to create a new instances. */
    final MethodHandle constructor;

    protected AbstractFoo(MethodHandle constructor) {
        this.constructor = requireNonNull(constructor);
    }

    /**
     * Creates a new instance.
     * 
     * @return a new instance
     */
    public final T newInstance() {
        try {
            return (T) constructor.invoke();
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }
}
