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
package app.packed.operation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.bindings.BindableVariable;
import app.packed.bindings.BindingClassifierKind;
import app.packed.context.Context;
import internal.app.packed.operation.PackedOperationHandle;

/**
 *
 */
public sealed interface OperationHandle permits PackedOperationHandle {

    /** {@return any contexts this operation operates within.} */
    Set<Class<? extends Context<?>>> contexts();

    /**
     * Generates a method handle that can be used to invoke the underlying operation.
     * <p>
     * This method can only be called in the code generating phase of the application's build process.
     * <p>
     * The type of the returned method handle is {@code invocationType()}.
     * 
     * @return the generated method handle
     * 
     * @throws IllegalStateException
     *             if called outside of the code generating phase of the application. Or if called more than once
     */
    MethodHandle generateMethodHandle();

    // Tror ikke det noget med extensionen at goere, snare lifetimen...
    // IDK
    MethodHandle generateMethodHandle(Object lifetimeHandle);

    /**
     * {@return the invocation type of this operation.}
     * <p>
     * Method handles generated via {@link #generateMethodHandle()} will always return their {@link MethodHandle#type()
     * type} as the returned value
     * 
     * @see OperationTemplate
     */
    MethodType invocationType();

    // Ogsaa en template ting taenker jeg? IDK
    void named(String name);

    /**
     * Allows f a binding for the parameter with the specified index.
     * <p>
     * This operation is no longer configurable when this method returns.
     * <p>
     * The will report a {@link BindingClassifierKind#MANUAL} as binding classifier
     * 
     * @param index
     *            the index of the parameter to bind
     * @return a bindable variable
     * @throws IndexOutOfBoundsException
     *             if the parameter index is out of bounds
     * @throws UnsupportedOperationException
     *             if a function
     */
    // Tror vi force laver (reserves) en binding her.
    // Det er jo kun meningen at man skal binden den hvis man kalder denne metode.
    // parameter virker kun som navn hvis man ikke "reservere" binding.
    // Men binder med det samme
    BindableVariable parameter(int index);

    /**
     * Specializes the mirror that is returned for the operation.
     * <p>
     * The specified supplier may be called multiple times for the same operation.
     * <p>
     * The specified supplier should never return {@code null}.
     * 
     * @param supplier
     *            a mirror supplier that is called if a mirror is required
     * @throws IllegalStateException
     *             if the operation is no longer configurable
     */
    void specializeMirror(Supplier<? extends OperationMirror> supplier);

    /** {@return the target of this operation.} */
    OperationTarget target();

    /** {@return the type of this operation.} */
    OperationType type();
}