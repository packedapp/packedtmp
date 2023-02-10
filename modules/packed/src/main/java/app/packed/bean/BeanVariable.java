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
package app.packed.bean;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import app.packed.bindings.BindingMirror;
import app.packed.context.Context;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Variable;
import internal.app.packed.service.KeyHelper;

/**
 * A bindable variable
 */
public non-sealed interface BeanVariable extends BeanElement {

    /**
     * By default binding to static fields is not allowed and will
     * <p>
     * Calling this method has no effect if the underlying variable is not a field.
     *
     * @return
     * @throws IllegalStateException
     *             if called after the variable has been bound
     */
    BeanVariable allowStaticFieldBinding();

    default Map<Class<? extends Context<?>>, List<Class<?>>> availableContexts() {
        return Map.of();
    }

    // Hmm, vi vil jo ogsaa gerne have contexts med...
    // Map<Context.class, List<>>
    List<Class<?>> availableInvocationArguments();

    /**
     * Binds the specified constant value to the underlying variable.
     * <p>
     * Tror vi smider et eller andet hvis vi er normal og man angiver null. Kan kun bruges for raw
     * <p>
     * See {@link #bindGeneratedConstant(Supplier)}
     *
     * @param value
     *            the value to bind
     * @throws IllegalArgumentException
     *             if {@code null} is specified and the variable is a primitive
     * @throws ClassCastException
     *             if the type of the value does not match the underlying parameter
     * @throws IllegalStateException
     *             if the variable has already been bound.
     */
    void bindConstant(@Nullable Object value);

    /**
     * Binds the underlying variable to a constant that is generated as part of the application's code generating phase.
     * <p>
     * If the application does not have a {@link BuildGoal#isCodeGenerating() code generating} phase. The specified supplier
     * is never invoked.
     * <p>
     * If the specified supplier returns a value that is not assignable to the underlying variable. The runtime will throw a
     * {@link CodegenException} in the application's code generating phase.
     * <p>
     * The specified supplier is never invoked more than once for a single binding.
     *
     * @param supplier
     *            the supplier of the constant
     * @see #bindConstant(Object)
     */
    void bindGeneratedConstant(Supplier<@Nullable ?> supplier);

    /**
     * @param argumentType
     */
    // Er den for farlig? Man kan jo kun binde hvis man ogsaa er operator
    void bindInvocationArgument(Class<?> argumentType);

    /**
     * @param argumentIndex
     *            the index of the argument
     *
     * @throws IndexOutOfBoundsException
     *             if the specified index does not represent a valid argument
     * @throws IllegalArgumentException
     *             if the invocation argument is not of kind {@link OperationTemplate.ArgumentKind#ARGUMENT}
     * @throws UnsupportedOperationException
     *             if the {@link #invokedBy()} is not identical to the binding extension
     * @throws ClassCastException
     *
     * @see OperationTemplate
     * @see InvocationArgument
     */
    void bindInvocationArgument(int argumentIndex);

    /**
     * @param argumentIndex
     * @param context
     *
     * @see InvocationContextArgument
     */
    default void bindInvocationArgumentForContext(Class<? extends Context<?>> context, int argumentIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * Binds an operation that will invoked every time the variable is needed
     *
     * @param op
     *            the operation to bind
     */
    void bindOp(Op<?> op);

    // bindLazy-> Per Binding? PerOperation? PerBean, ?PerBeanInstance ?PerContainer ? PerContainerInstance ?
    // PerApplicationInstance

    // Kan only do this if is invoking extension!!

    /**
     * @param classes
     *            the classes to check
     * @return the first class in the specified array that the variable is assignable to
     * @see Class#isAssignableFrom(Class)
     */
    // maybe replace it with notAssignableTo
    default Class<?> checkAssignableTo(Class<?>... classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("Cannot specify an empty array");
        }
        Class<?> rawType = variable().rawType();
        for (Class<?> c : classes) {
            if (c.isAssignableFrom(rawType)) {
                return c;
            }
        }
        throw new BeanInstallationException(variable() + ", Must be assignable to one of " + Arrays.toString(classes));
    }

    /** {@return the extension that is responsible for invoking the underlying operation.} */
    Class<? extends Extension<?>> invokedBy();

    /** {@return whether or not the underlying variable has been bound.} */
    boolean isBound();

    /** {@return the raw type of the variable.} */
    default Class<?> rawType() {
        return variable().rawType();
    }

    /**
     *
     * we are actually specializing the binding and not the variable. But don't really want to create a BindingHandle...
     * just for this method
     *
     * @param supplier
     * @return
     */
    BeanVariable specializeMirror(Supplier<? extends BindingMirror> supplier);

    /**
     * Parses the variable to a key.
     *
     * @return a key representing the variable
     *
     * @throws InvalidKeyException
     *             if the variable does not represent a valid key
     */
    @Override
    default Key<?> toKey() {
        return KeyHelper.convert(variable().type(), variable().annotations().toArray(), this);
    }

    default BeanWrappedVariable unwrap() {
        // peel ->

        // peel, unwrap
        // Ville vaere fedt hvis alle metoderne havde samme prefix
        throw new UnsupportedOperationException();
    }

    /** {@return the variable that can be bound.} */
    Variable variable();

    // The target of a binding
    // What about Variable in an OP??
    enum Target {
        FIELD, PARAMETER, TYPE_PARAMETER;
    }
}

interface Sanfbox {

    /**
     *
     * @throws UnsupportedOperationException
     *             if the underlying variable is not a {@link Record}
     */
    void bindCompositeRecord(); // bindComposite?
    // Har saa mange metoder i forvejen

    default boolean isAssignableTo(Class<?>... classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("Cannot specify an empty array of classes");
        }
        Class<?> rawType = Class.class; //variable().getRawType();
        for (Class<?> c : classes) {
            if (c.isAssignableFrom(rawType)) {
                return true;
            }
        }
        return false;
    }

}