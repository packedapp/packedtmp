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

import app.packed.base.Nullable;
import app.packed.inject.Factory;
import app.packed.inject.FactoryType;

/**
 *
 */
public abstract class AbstractSemiFinalMethodSidecar extends AbstractMethodSidecar {

    /**
     * 
     * 
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
    public final void bindParameterConstant(int index, @Nullable Object constant) {

    }

    // insert instead??? bind = constants, insert = factory
    public final void bindParameter(int index, Factory<?> factory) {
        // ideen er lidt at vi indsaetter parameter fra factory i factory type
    }

    // this type of factory we will try and resolve...
    // bind parameters must be done before hand
    public final FactoryType factoryType() {
        // this will change when binding parameter
        throw new UnsupportedOperationException();
    }
}
