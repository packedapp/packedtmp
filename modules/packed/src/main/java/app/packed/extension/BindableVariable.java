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
package app.packed.extension;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.bean.BeanInstallationException;
import app.packed.context.Context;
import app.packed.operation.BindingMirror;
import app.packed.operation.Op;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.Variable;
import internal.app.packed.bean.PackedBindableVariable;
import sandbox.extension.operation.OperationTemplate;

/**
 * Represents a variable that can be bound at build time by an extension.
 * <p>
 *
 */

// Something about being embedded
// For example, deep down, we cannot resolve something. And we need to
// throw an exception. But we need to include the original method that could not
// be resolved.
// Or VariableBinder
public sealed interface BindableVariable extends BeanElement permits PackedBindableVariable, UnwrappedBindableVariable {

    /**
     * By default binding to static fields are not permitted. Any call to one of the bind methods of this interface will
     * result in an exception being thrown unless this method has been called first.
     * <p>
     * Calling this method has no effect if the underlying variable is not a field.
     *
     * @return this bindable variable
     * @throws IllegalStateException
     *             if the variable has already has been bound
     */
    BindableVariable allowStaticFieldBinding();

    /**
     * <p>
     * This method exist only for informational purposes.
     *
     * @return
     */
    default Set<Class<? extends Context<?>>> availableContexts() {
        return Set.of();
    }

    // Hmm, vi vil jo ogsaa gerne have contexts med...
    // Map<Context.class, List<>>

    /**
     * NOTE: Invocation arguments are only available variables returned from Ope
     *
     * @
     */
    List<Class<?>> availableInvocationArguments();

    /**
     * Binds the underlying variable to a constant that is computed exactly once. Typically, doing the application's code
     * generating phase.
     * <p>
     * If the application's {@link BuildGoal#isCodeGenerating() code generating} phase is never executed, for example, if
     * building an {@link app.packed.application.ApplicationMirror}. The specified supplier will never be called.
     * <p>
     * If the specified supplier returns a value that is not assignable to the underlying variable. The runtime will throw a
     * {@link CodegenException} when called.
     * <p>
     * The specified supplier is never invoked more than once.
     *
     * @param supplier
     *            the supplier of the constant
     * @throws IllegalStateException
     *             if the variable has already been bound.
     * @see #bindConstant(Object)
     */
    BindableVariable bindComputedConstant(Supplier<@Nullable ?> supplier);

    /**
     * Binds the specified constant value to the underlying variable.
     *
     * @param value
     *            the value to bind
     * @throws IllegalArgumentException
     *             if {@code null} is specified and null is not a valid value for the variable
     * @throws ClassCastException
     *             if the type of the value is not assignable to the underlying parameter
     * @throws IllegalStateException
     *             if the variable has already been bound.
     * @see #bindGeneratedConstant(Supplier)
     */
    BindableVariable bindConstant(@Nullable Object value);

    /**
     * Binds the specified context to the underlying variable.
     * <p>
     * If you need to perform any kind of transformations on a particular context you can use {@link #bindOp(Op)} instead.
     * Taking the context as the sole argument and returning the result of the transformation.
     *
     * @param fromContext
     *
     * @throws ClassCastException
     *             if the type of the context is not assignable to the underlying variable
     * @throws app.packed.context.ContextNotAvailableException
     *             if the context is not available
     */
    BindableVariable bindContext(Class<? extends Context<?>> context);

    /**
     * @param index
     *            the index of the invocation argument in
     * @return
     *
     * @throws UnsupportedOperationException
     *             if called from a bindable variable that was not created from
     *             {@link sandbox.extension.operation.OperationHandle#manuallyBindable(int)}
     */
    // We need this, for example, for @OnEvent. Where the first argument is the event
    // Men hmm, hvad fx med ExtensionContext
    BindableVariable bindInvocationArgument(int index);

    /**
     * Binds an operation whose return value will be used as the variable argument. The specified operation will be invoked
     * every time the the variable is requested.
     * <p>
     * There are no direct support for lazy computation or caching of computed values.
     *
     * @param op
     *            the operation to bind
     * @throws ClassCastException
     *             if the return type of the op is not assignable to the variable
     */
    BindableVariable bindOp(Op<?> op);

    /**
     * Checks that the underlying variable is {@link Class#isAssignableFrom(Class) assignable} to one of the specified
     * classes.
     *
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
        List<String> cls = List.of(classes).stream().map(c -> c.getSimpleName()).toList();
        throw new BeanInstallationException(variable() + ", Must be assignable to one of " + cls);
    }

    /**
     * {@return the extension that is responsible for invoking the underlying non-nested operation}
     */
    Class<? extends Extension<?>> invokedBy();

    /** {@return whether or not the underlying variable has been bound.} */
    boolean isBound();

    /** {@return a descriptor of the underlying operation. */
    // What if embedded?
    OperationTemplate.Descriptor operation();

    /** {@return the raw type of the variable.} */
    default Class<?> rawType() {
        return variable().rawType();
    }

    /**
     * Specializes the binding mirror.
     * <p>
     * Notice: We are actually specializing the binding and not the variable.
     *
     * @param supplier
     *            the supplier used to create the binding mirror
     * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
     *          must be returned
     * @return this bindable variable
     */
    BindableVariable specializeMirror(Supplier<? extends BindingMirror> supplier);

    /**
     * Parses the variable as a key.
     *
     * @return a key representing the variable
     *
     * @throws InvalidKeyException
     *             if the variable does not represent a valid key
     */
    @Override
    Key<?> toKey();

    default UnwrappedBindableVariable unwrap() {
        // peel ->

        // peel, unwrap
        // Ville vaere fedt hvis alle metoderne havde samme prefix
        throw new UnsupportedOperationException();
    }

    /** {@return the variable that can be bound.} */
    Variable variable();

    // The target of a binding, Put on variable instead???
    // Syntes faktisk ikke det giver mening at have den her, men ikke paa variable

    // What about Variable in an OP?? And what if the Op wraps a field???
    // Target (Object?) + TargetKind (Enum)
    /**
     * The target of a bindable variable.
     */
    enum Target {
        /** The variable represents a field. */
        FIELD, PARAMETER, TYPE_PARAMETER; // What if MethodHandle???
    }
}

interface Sanfbox {

    // bindLazy-> Per Binding? PerOperation? PerBean, ?PerBeanInstance ?PerContainer ? PerContainerInstance ?
    // PerApplicationInstance. Can only do this if is invoking extension!!
    void bindCachableOp(Object cacheType, Op<?> op);

    // Ideen er den bliver instantiteret hver gang (Er det en bean saa???)

    // Det er lidt som en OP

    // Tror vi skriver meget klart, at man kun scanner efter inject...
    // Alle andre annoteringer bliver ignoreret
    // Altsaa man kan jo bare finde constructeren selv, og saa kalde
    // Constructor c= Foo.class.getConstroctur(int.class, String.class);
    // Op.ofConstructor(MethodHandles.lookup(), c);
    void bindComposite(Class<?> compositeClass); // bindComposite?

    /**
     *
     * @throws UnsupportedOperationException
     *             if the underlying variable is not a {@link Record}
     */
    // En composite
    void bindCompositeRecord(); // bindComposite?
    // Har saa mange metoder i forvejen

    BindableVariable bindInvocationArgument(int index, MethodHandle transformer);

    default boolean isAssignableTo(Class<?>... classes) {
        if (classes.length == 0) {
            throw new IllegalArgumentException("Cannot specify an empty array of classes");
        }
        Class<?> rawType = Class.class; // variable().getRawType();
        for (Class<?> c : classes) {
            if (c.isAssignableFrom(rawType)) {
                return true;
            }
        }
        return false;
    }

}