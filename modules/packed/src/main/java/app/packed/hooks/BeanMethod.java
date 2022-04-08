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
import java.lang.reflect.Method;

import app.packed.bean.hooks.BeanClassHook;
import app.packed.bean.hooks.BeanMethodHook;
import app.packed.inject.FactoryType;

/**
 *
 */
public abstract class BeanMethod {

    /** Create a new bean method instance. */
    protected BeanMethod() {}

    /**
     * Returns the modifiers of the method.
     * 
     * @return the modifiers of the method
     * @see Method#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
     */
    public abstract int getModifiers();

    public abstract boolean hasInvokeAccess();

    /**
     * Returns the underlying method.
     * 
     * @return the underlying method
     */
    public abstract Method method();

    /**
     * Returns a direct method handle to the {@link #method()} (without any intervening argument bindings or transformations
     * that may have been configured elsewhere).
     * 
     * @return a direct method handle to the underlying method
     * @see Lookup#unreflect(Method)
     * @see BeanMethodHook#allowInvoke()
     * @see BeanClassHook#allowAllAccess()
     * 
     * @throws UnsupportedOperationException
     *             if invocation access has not been granted via {@link BeanMethodHook#allowInvoke()} or BeanClassHook#allowAllAccess()
     */
    public abstract MethodHandle methodHandle();
    
    public BeanOperation operation() {
        throw new UnsupportedOperationException();
    }
    
    public abstract FactoryType type();
}
