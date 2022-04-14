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
package app.packed.bean.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import app.packed.extension.Extension;
import app.packed.inject.FactoryType;

/**
 *
 */
public non-sealed interface BeanMethod extends BeanElement{

    /**
     * Returns the modifiers of the method.
     * 
     * @return the modifiers of the method
     * @see Method#getModifiers()
     * @apiNote the method is named getModifiers instead of modifiers to be consistent with {@link Method#getModifiers()}
     */
    int getModifiers();

    boolean hasInvokeAccess();

    /**
     * Returns the underlying method.
     * 
     * @return the underlying method
     */
    Method method();

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
    MethodHandle methodHandle();
    
    BeanOperation operation();
    
    FactoryType type();
    
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface Hook {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as {@link BeanMethod#methodHandle()} and... will fail with {@link UnsupportedOperationException} unless
         * the value of this attribute is {@code true}.
         * 
         * @return whether or not the implementation is allowed to invoke the target method
         * 
         * @see BeanMethod#methodHandle()
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        boolean allowInvoke() default false; // allowIntercept...

        /** The hook's {@link BeanField} class. */
        Class<? extends Extension<?>> extension();

        // Altsaa vi har jo ikke lukket for at vi senere kan goere nogle andre ting...
        // Class<Supplier<? extends BeanMethod>> bootstrap();
    }
}
