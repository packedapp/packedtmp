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
 * The target of an operation, typically a method.
 * 
 * @see OperationMirror#site()
 */
// Maybe this an operation site (instead of + Mirror) that is also available at runtime...
public sealed interface OperationSiteMirror {

    /** Represents an operation that invokes a {@link Constructor constructor}. */
    public sealed interface OfConstructorInvoke extends OperationSiteMirror permits ConstructorOperationSetup {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();
    }

    /** Represents an operation that gets, sets or updates a {@link Field field}. */
    public sealed interface OfFieldAccess extends OperationSiteMirror permits FieldOperationSetup {

        AccessMode accessMode();

        /** {@return the underlying field.} */
        Field field();
    }

    /**
     * Represents an operation that calls the abstract method on a functional interface.
     * <p>
     * method on a functional interface.
     */
    public sealed interface OfFunctionCall extends OperationSiteMirror permits FunctionOperationSetup {

        /** {@return the functional interface.} */
        Class<?> functionalInterface();

        /** {@return implementing method of the functional interface.} */
        Method implementationMethod();

        /** {@return the single abstract method on the functional interface.} */
        Method interfaceMethod();
    }

    /** Represents an operation that invokes a {@link MethodHandle method handle}. */
    public sealed interface OfMethodHandleInvoke extends OperationSiteMirror permits MethodHandleOperationSetup {

        /** {@return the method type of the method handle.} */
        MethodType methodType();
    }

    /** Represents an operation that invokes a {@link Method method}. */
    public sealed interface OfMethodInvoke extends OperationSiteMirror permits MethodOperationSetup {

        /** {@return the invokable method.} */
        Method method();
    }
}
// Er det ikke bare OperationType vi skal koere den paa????
// AnnotataionReader annotations()???

///**
// * Represents an operation simply returns a constant.
// * <p>
// * An {@link OperationMirror operation} with {@code constant} target never has any {@link OperationMirror#bindings()
// * bindings}.
// */
//public non-sealed interface OfConstant extends OperationSiteMirror {
//
//    /** {@return the type of constant.} */
//    Class<?> constantType();
//}
