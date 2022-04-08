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
package app.packed.hooks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.base.Variable;
import app.packed.bean.hooks.BeanFieldHook;

/**
 *
 */
public abstract class BeanField {

    /** Create a new bean method instance. */
    protected BeanField() {}

    /**
     * Returns the underlying field.
     * 
     * @return the underlying field
     */
    public abstract Field field();

    /**
     * Returns the modifiers of the field.
     * 
     * @return the modifiers of the field
     * @see Field#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
     */
    public abstract int getModifiers();

    public abstract BeanOperation operation(VarHandle.AccessMode accessMode);

    public abstract BeanOperation operationGetter();

    public abstract BeanOperation operationSetter();

    /**
     * @return a method handle getter
     */
    public abstract MethodHandle methodHandleGetter();
    
    public abstract MethodHandle methodHandleSetter();

    /**
     * Must have both get and set
     * 
     * @return the variable
     * @see Lookup#unreflectVarHandle(Field)
     * @see BeanFieldHook#allowGet()
     * @see BeanFieldHook#allowSet()
     * @throws UnsupportedOperationException
     *             if the extension field has not both get and set access
     */
    public abstract VarHandle varHandle();

    /**
     * {@return the underlying represented as a {@code Variable}.}
     * 
     * @see Variable#ofField(Field)
     */
    public abstract Variable variable();
}
