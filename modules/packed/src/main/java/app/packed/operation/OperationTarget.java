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

import internal.app.packed.operation.OperationMemberTarget.OperationConstructorTarget;
import internal.app.packed.operation.OperationMemberTarget.OperationFieldTarget;
import internal.app.packed.operation.OperationMemberTarget.OperationMethodTarget;
import internal.app.packed.operation.OperationSetup.FunctionOperationSetup;
import internal.app.packed.operation.OperationSetup.MethodHandleOperationSetup;

/**
 * The target of an operation.
 *
 * @see OperationMirror#target()
 */
public sealed interface OperationTarget {

    /** Represents an operation that invokes a {@link Constructor constructor}. */
    sealed interface OfConstructor extends OperationTarget permits OperationConstructorTarget {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();
    }

    /** Represents an operation that gets, sets or updates a {@link Field field}. */
    sealed interface OfField extends OperationTarget permits OperationFieldTarget {

        /** {@return the mode used when accessing the field.} */
        AccessMode accessMode(); // If we support VarHandle: Optional? or fail . Hmm skal vel matche operation type

        /** {@return the underlying field.} */
        Field field();
    }

    /** Represents a operation that invokes the single abstract method on a functional interface. */
    sealed interface OfFunction extends OperationTarget permits FunctionOperationSetup {

        /** {@return the functional interface.} */
        Class<?> functionalInterface();

        /** {@return the method that implements the function.} */
        Method implementingMethod();

        /** {@return the single abstract method on the functional interface.} */
        Method interfaceMethod();
    }

    /** Represents an operation that invokes a {@link Method method}. */
    sealed interface OfMethod extends OperationTarget permits OperationMethodTarget {

        /** {@return the invokable method.} */
        Method method();
    }

    /** Represents an operation that invokes a {@link MethodHandle method handle}. */
    sealed interface OfMethodHandle extends OperationTarget permits MethodHandleOperationSetup {

        /** {@return the method type of the method handle.} */
        MethodType methodType();
    }
}

//Hvad har at lave en ny container/bean som target???
// OfSpecial()

// Vi har invokers nu. Det er ikke operation
///** Represents an operation that invokes other (child) operations. */
//non-sealed interface OfNested extends OperationTarget {}

///** Represents an operation that accesses a bean instance. */
//public non-sealed interface OfThisAccess extends OperationTarget {
//
//  /** {@return the bean that is being accessed.} */
//  BeanMirror bean();
//}
