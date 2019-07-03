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
package app.packed.hook;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;

import app.packed.util.FieldDescriptor;
import app.packed.util.IllegalAccessRuntimeException;

/** A hook representing a field annotated with a specific type. */
public interface AnnotatedFieldHook<T extends Annotation> {

    /**
     * Returns the annotation value.
     *
     * @return the annotation value
     */
    T annotation();

    /**
     * Returns the annotated field.
     * 
     * @return the annotated field
     */
    FieldDescriptor field();

    Lookup lookup(); // TODO remove this method

    /**
     * Creates a new {@link VarHandle} for the underlying field.
     * 
     * @return a new VarHandle for the underlying field
     * @throws IllegalAccessRuntimeException
     *             if a var handle could not be created
     * @see Lookup#unreflectVarHandle(java.lang.reflect.Field)
     */
    VarHandle newVarHandle();

    /**
     * Creates a method handle giving read access to the underlying field.
     * 
     * @return a new method handle with read access
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created
     * @see Lookup#unreflectGetter(java.lang.reflect.Field)
     */
    MethodHandle newMethodHandleGetter();

    /**
     * Creates a method handle giving read access to the underlying field.
     * 
     * @return a new method handle with read access
     * @throws IllegalAccessRuntimeException
     *             if a method handle could not be created, for example, if the underlying field is final
     * @see Lookup#unreflectSetter(java.lang.reflect.Field)
     */
    MethodHandle newMethodHandleSetter();
}
