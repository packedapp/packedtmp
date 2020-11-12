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
package app.packed.sidecar;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.Optional;

import app.packed.base.Nullable;
import app.packed.inject.Factory;
import app.packed.inject.FactoryType;

/**
 *
 */
public abstract class AbstractSemiFinalMethodSidecar extends MethodSidecar {

    final boolean needsInstance() {
        return !Modifier.isStatic(method().getModifiers());
    }

    /**
     * <p>
     * If the parameter type is a primitive, the argument object must be a wrapper, and will be unboxed to produce the
     * primitive value.
     * 
     * @param index
     *            the index of the parameter
     * @param constant
     *            the constant to bind
     * @throws IndexOutOfBoundsException
     *             if the specified index is not valid
     * @throws ClassCastException
     *             if an argument does not match the corresponding parameter type.
     * @see MethodHandles#insertArguments(MethodHandle, int, Object...)
     */
    public final void parameterBind(int index, @Nullable Object constant) {}

    // insert instead??? bind = constants, insert = factory
    public final void parameterReplace(int index, Factory<?> factory) {
        // ideen er lidt at vi indsaetter parameter fra factory i factory type
    }

    // this type of factory we will try and resolve...
    // bind parameters must be done before hand
    public final FactoryType targetType() {
        // this will change when binding parameter
        throw new UnsupportedOperationException();
    }

    // *******Customize receiver ******* /
    // Enten via en ny runtime klasse
    // Eller vi custom factories...

    public final Optional<Class<?>> runtime() {
        // Class<?> --> MetaClass
        // disable -> empty
        // no interesting methods -> empty
        // default = sidecar
        // replaceRuntime(Class...);
        // For eksempel hvis man returnere noget...
        throw new UnsupportedOperationException();
    }

    // addFunction(Factory + @Schedule(123)
    public void replaceRuntime(Object runtime) {
        // runtime must be fixed before we start binding...
    }

    // 1 per static method or 1 per instance method
    public void replaceRuntimeProtoype(Class<?> runtime) {

    }
    // We need meta class
    /// Only functions??
    /// Only interesting functions???
    // ...
}
