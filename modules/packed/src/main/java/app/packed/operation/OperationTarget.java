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
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import internal.app.packed.operation.OperationSetup.FunctionOperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.ConstructorOperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.FieldOperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.MethodOperationSetup;
import internal.app.packed.operation.OperationSetup.MethodHandleOperationSetup;

/**
 * The target of an operation.
 * 
 * @see OperationMirror#target()
 */
public sealed interface OperationTarget {

//    /** Represents an operation that accesses a bean instance. */
//    public non-sealed interface OfBeanAccess extends OperationTarget {
//
//        /** {@return the bean that is being accessed.} */
//        BeanMirror bean();
//    }

    /** Represents an operation that invokes a {@link Constructor constructor}. */
    public sealed interface OfConstructorInvoke extends OperationTarget permits ConstructorOperationSetup {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();
    }

    /** Represents an operation that gets, sets or updates a {@link Field field}. */
    public sealed interface OfFieldAccess extends OperationTarget permits FieldOperationSetup {

        /** {@return the mode used when accessing the field.} */
        AccessMode accessMode(); // Could we have a list instead???

        /** {@return the underlying field.} */
        Field field();
    }

    /** Represents the invocation of a function on a functional interface. */
    public sealed interface OfFunctionCall extends OperationTarget permits FunctionOperationSetup {

        /** {@return the functional interface.} */
        Class<?> functionalInterface();

        /** {@return the method that implements the function.} */
        Method implementingMethod();

        /** {@return the single abstract method on the functional interface.} */
        Method interfaceMethod();
    }

    /** Represents an operation that invokes a {@link MethodHandle method handle}. */
    public sealed interface OfMethodHandleInvoke extends OperationTarget permits MethodHandleOperationSetup {

        /** {@return the method type of the method handle.} */
        MethodType methodType();
    }

    /** Represents an operation that invokes a {@link Method method}. */
    public sealed interface OfMethodInvoke extends OperationTarget permits MethodOperationSetup {

        /** {@return the invokable method.} */
        Method method();
    }
}
