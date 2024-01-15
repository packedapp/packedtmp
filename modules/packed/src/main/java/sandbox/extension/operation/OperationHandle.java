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
package sandbox.extension.operation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.function.Supplier;

import app.packed.component.ComponentPath;
import app.packed.extension.BindableVariable;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionPoint;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationType;
import internal.app.packed.context.publish.ContextualizedElement;
import internal.app.packed.operation.PackedOperationBuilder;
import internal.app.packed.operation.PackedOperationHandle;

/**
 * A handle for managing an operation at build-time.
 */
// AllInit
/// FooBean.init()
/// TooBean.init1()
/// TooBean.init12()

// Embedded (yes/no)
// Top (yes/no)
// Top does not have a parent
// Top Yes and

// Top
// Non-Top
// Embedded
public sealed interface OperationHandle extends ContextualizedElement permits PackedOperationHandle {

    ComponentPath componentPath();

    /**
     * Checks that the container is still configurable, or throws an exception.
     *
     * @throws IllegalStateException
     *             if the container is no longer configurable
     */
    default void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("This container is no longer configurable");
        }
    }

    /**
     * Returns whether or not the container is still configurable.
     * <p>
     * If an assembly was used to create the container. The handle is never configurable.
     *
     * @return {@code true} if the bean is still configurable
     */
    boolean isConfigurable();

    // Hmm there is a difference between operating within contexts./
    // And invocation argument contexts
    // Set<Class<? extends Context<?>>> contexts();

    /**
     * Generates a method handle that can be used to invoke the underlying operation.
     * <p>
     * This method cannot be called earlier than the code generating phase of the application.
     * <p>
     * The {@link MethodType method type} of the returned method handle is {@code invocationType()}.
     *
     * @return the generated method handle
     *
     * @throws IllegalStateException
     *             if called before the code generating phase of the application.
     */
    MethodHandle generateMethodHandle();

    /**
     * {@return the invocation type of this operation.}
     * <p>
     * Method handles generated via {@link #generateMethodHandle()} will always the returned value as their
     * {@link MethodHandle#type() method handle type}.
     *
     * @see OperationTemplate.Descriptor#invocationType()
     */
    MethodType invocationType();

    /**
     * This will create a {@link BindingKind#MANUAL manual} binding for the parameter with the specified index.
     * <p>
     * The {@link BindableVariable} must be bound at some point before the assembly closes. Otherwise a BuildException is
     * thrown.
     * <p>
     * This operation is no longer configurable when this method returns.
     * <p>
     * The will report a {@link BindingKind#MANUAL} as binding classifier
     *
     * @param parameterIndex
     *            the index of the parameter to bind
     * @return a bindable variable
     * @throws IndexOutOfBoundsException
     *             if the parameter index is out of bounds
     * @throws UnsupportedOperationException
     *             if type of operation does not support manually binding. For example, a lifecycle operation. Eller er det
     *             i virkeligheden mere at ejeren af operationen er anderledes end den der kalder. Problemet er fx.
     *             BEan.lifetimeOperations som returnere OperationHandle. Men de er der kun fordi man skal kalde generateX.
     *             Maaske har vi en slags immutable version af operation handle
     * @throws IllegalStateException
     *             this method must be called before the runtime starts resolving parameters. It is best to call this
     *             immediately after having created the operation. The actual binding can be done at a laver point\
     * @see BindingKind#MANUAL
     */
    // Tror vi force laver (reserves) en binding her.
    // Det er jo kun meningen at man skal binden den hvis man kalder denne metode.
    // parameter virker kun som navn hvis man ikke "reservere" binding.
    // Men binder med det samme
    // reserve
    // manuallyBindable(1).bindInvocationArgument(1);
    BindableVariable manuallyBindable(int parameterIndex);

    // Ogsaa en template ting taenker jeg? IDK
    void named(String name);

    /** {@return the operator of the operation.} */
    Class<? extends Extension<?>> operator();

    default <C extends OperationConfiguration> C configure(Supplier<? super C> configure) {
        throw new UnsupportedOperationException();
    }

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

    /**
    *
    */

    // Source
    //// Method/Constructor
    //// Field -> Get/Set/Invoke
    //// Function / Op / MethodHandle

    // "Targets"

    // FullOp
    // ChildOp
    // DelegateTo
    // (Bounded) EmbeddedOp (Er aldrig visible...

    public sealed interface Builder permits PackedOperationBuilder {

        OperationHandle build(OperationTemplate template);

        Builder delegateTo(ExtensionPoint.UseSite extension);

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
        Builder specializeMirror(Supplier<? extends OperationMirror> supplier);
    }
}

interface Zandbox {

//  // Maaske har vi en and then...
//  // First@InitializeHandle.andThen(Builder )
//  // Kan kun lave en MH for den foerste
//  OperationHandle buildChild(OperationHandle parent);

    // raekkefoelgen kender man jo ikke
    // Foer vi har sorteret
    void addChild(OperationHandle h);

    // Det ville jo vaere rart at sige. Hey
    // Lav denne Operation. Det er en naestet operation, saa
    // tages contexts fra parent operation

    default VarHandle generateVarHandle() {
        throw new UnsupportedOperationException();
    }
}