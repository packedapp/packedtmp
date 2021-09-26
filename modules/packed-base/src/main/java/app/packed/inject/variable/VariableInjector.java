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
package app.packed.inject.variable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import app.packed.inject.Factory;
import app.packed.inject.Variable;

/**
 *
 *
 * <p>
 * 
 */

// Does not support lifecycle annotations...
// only @ScopedProvide

// VariableBinder, VariableProvider
//// ContextualizedProvisional

// 3 typer
// * Constant
// * Function
// * @Contextualizable
public interface VariableInjector {

    /**
     * Provides the same nullable constant to the variable at runtime.
     * 
     * @param constant
     *            the constant to provide to the variable
     * @throws ClassCastException
     *             if the type of the constant does not match the type of the variable
     */
    default void injectConstant(@Nullable Object constant) {
        injectVia(MethodHandles.constant(variable().getType(), constant));
    }

    /**
     * @param factory
     * 
     * @throws IllegalStateException
     *             if a bind method has already been called on this binder instance (I think it is fine to allow it to be
     *             overriden by itself)
     */
    void injectVia(Factory<?> factory);

    // Det return type of the method handle must match (be assignable to) variable.getType();
    void injectVia(MethodHandle methodHandle);

    void nextStep(VariableHook instance);

    void nextStepSpawn(Class<? extends VariableHook> implementation);

    // ------- Must have a single method annotated with @Provide, whose return type must match variable.getType()
    void nextStepSpawn(Factory<? extends VariableHook> factory); // Den laver et objekt som kan bruge... IDK hvor spaendende det er

    /** {@return the variable that should be bound.} */
    Variable variable(); // IDK know about this
}

// Alle hooks kan bruge den
interface ZookReplacer {

    void nextStep(Object instance);

    void nextStepSpawn(Class<?> implementation);

    // ------- Must have a single method annotated with @Provide, whose return type must match variable.getType()
    void nextStepSpawn(Factory<?> factory); // Den laver et objekt som kan bruge... IDK hvor spaendende det er

}