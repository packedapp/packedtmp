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

import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import app.packed.operation.OperationSiteMirror.OfConstant;
import app.packed.operation.OperationSiteMirror.OfConstructorInvoke;
import app.packed.operation.OperationSiteMirror.OfFieldAccess;
import app.packed.operation.OperationSiteMirror.OfFunctionCall;
import app.packed.operation.OperationSiteMirror.OfLifetimePoolAccess;
import app.packed.operation.OperationSiteMirror.OfMethodHandleInvoke;
import app.packed.operation.OperationSiteMirror.OfMethodInvoke;
import internal.app.packed.operation.OperationSite;

/**
 * The target of an operation. This is typically a method
 * 
 * @see OperationMirror#site()
 */
public sealed interface OperationSiteMirror permits OperationSite, OfConstructorInvoke, OfFieldAccess, OfFunctionCall, OfLifetimePoolAccess, OfMethodInvoke, OfMethodHandleInvoke, OfConstant {

    OperationType type();
    
    // AnnotataionReader annotations()???

    /**
     * Represents an operation that simply returns a constant.
     * <p>
     * An {@link OperationMirror operation} with {@code constant} target never has any {@link OperationMirror#bindings()
     * bindings}.
     */
    public non-sealed interface OfConstant extends OperationSiteMirror {

        /** {@return the type of constant.} */
        Class<?> constantType();
    }

    // Members
    /** Represents an operation that invokes a constructor. */
    public non-sealed interface OfConstructorInvoke extends OperationSiteMirror {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();
    }

    /** Represents an operation that gets, sets or updates a field. */
    public non-sealed interface OfFieldAccess extends OperationSiteMirror {

        AccessMode accessMode();

        boolean allowGet();

        boolean allowSet();

        /** {@return the underlying field.} */
        Field field();
    }

    /**
     * Represents an operation that calls the abstract method on a functional interface.
     * <p>
     * method on a functional interface.
     */
    public non-sealed interface OfFunctionCall extends OperationSiteMirror {

        /** {@return the functional interface.} */
        Class<?> functionalInterface();

        default Method implementationMethod() {
            throw new UnsupportedOperationException();
        }

        default Method interfaceMethod() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Represents an operation that it performed sometime in the lifetime of X
     * And then returned for subsequent invocations.
     * <p>
     * This is typically for access container beans that are created as
     * part of their container lifetime creation.
     * .
     * <p>
     * This is typically used for beans whose instance is provided or exported as a service.
     */
    //
    public non-sealed interface OfLifetimePoolAccess extends OperationSiteMirror {

        // empty if the instance was provided
        // otherwise the operation that created it, and stored it somewhere.
        Optional<OperationMirror> origin();

        // Accessing an instance that have previously been computed
        // Was BeanInstance but we create a synthetic operation for for example BeanVarInject.provideInstance
        // Giver ikke mening for provide...
        // Kan ikke komme paa andre brugbare ting end @Provide

        // Er det her en speciel operation istedet for et target???
        /// InstanceProvide? IDK

        // Har maaske ogsaa noget LifetimePoolMirror her????
    } // ofLifetimePool? Hmm

    public non-sealed interface OfMethodHandleInvoke extends OperationSiteMirror {
        MethodType methodType();
    }

    /** Represents an operation that invokes a method. */
    public non-sealed interface OfMethodInvoke extends OperationSiteMirror {

        /** {@return the invokable method.} */
        Method method();
    }
}
