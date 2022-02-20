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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.hooks.BeanClass.ClassHook;

/**
 *
 */
public abstract class BeanConstructor {

    /** Disables any further processing of the Constructor. */
    public final void disable() {}

    /**
     * Returns true if an annotation for the specified type is <em>present</em> on the hooked class, else false.
     * 
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return true if an annotation for the specified annotation type is present on the hooked class, else false
     * 
     * @see Field#isAnnotationPresent(Class)
     */
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a direct method handle to the matching method (without any intervening argument bindings or transformations
     * that may have been configured elsewhere).
     * 
     * @return a direct method handle to the matching method
     * @see Lookup#unreflect(Method)
     * @see BeanMethodHook#allowInvoke()
     * @see ClassHook#allowAllAccess()
     * 
     * @throws UnsupportedOperationException
     *             if invocation access has not been granted via {@link BeanMethodHook#allowInvoke()}
     */
    public final MethodHandle methodHandle() {
        throw new UnsupportedOperationException();
    }
}
