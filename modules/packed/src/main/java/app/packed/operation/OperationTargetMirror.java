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

import app.packed.operation.OperationTargetMirror.OfConstructorInvoke;
import app.packed.operation.OperationTargetMirror.OfFieldAccess;
import app.packed.operation.OperationTargetMirror.OfFunctionCall;
import app.packed.operation.OperationTargetMirror.OfInstanceAccess;
import app.packed.operation.OperationTargetMirror.OfMethodInvoke;
import app.packed.operation.OperationTargetMirror.OfSyntheticInvoke;

/**
 * The target of an operation.
 * 
 * @see OperationMirror#target()
 */
// Proev at undgaa at smid for meget information fra OperationMirror her...

// Hvad med Factory1<>??? Her taenker vi paa at der bliver fanget annoteringer...
// Er det Saa Method invoke???Eller FunctionCall

// Maaske drop Invoke til di
// OperationLocationmirror?
// OperationKindMirror?
// InvocationSiteMirror
public sealed interface OperationTargetMirror permits OfConstructorInvoke, OfFieldAccess, OfFunctionCall, OfInstanceAccess, OfMethodInvoke, OfSyntheticInvoke {

    // AnnotataionReader annotations()???

    // Members
    /** Represents an operation that invokes a constructor. */
    public non-sealed interface OfConstructorInvoke extends OperationTargetMirror {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();
    }

    /** Represents an operation that gets, sets or updates a field. */
    public non-sealed interface OfFieldAccess extends OperationTargetMirror {

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
    public non-sealed interface OfFunctionCall extends OperationTargetMirror {

        default Method actualMethod() {
            throw new UnsupportedOperationException();
        }

        default Method functionalInterfaceMethod() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Represents an operation that simply return an instance
     */
    // BeanSpace? ConstantSpace?
    public non-sealed interface OfInstanceAccess extends OperationTargetMirror {
        // empty if the instance was provided
        // otherwise the operation that created it, and stored it somewhere.
        Optional<OperationTargetMirror> origin();

        // Accessing an instance that have previously been computed
        // Was BeanInstance but we create a synthetic operation for for example BeanVarInject.provideInstance
        // Giver ikke mening for provide...
        // Kan ikke komme paa andre brugbare ting end @Provide

        // Er det her en speciel operation istedet for et target???
        /// InstanceProvide? IDK

        // Har maaske ogsaa noget LifetimePoolMirror her????
    } // ofLifetimePool? Hmm

    /** Represents an operation that invokes a method. */
    public non-sealed interface OfMethodInvoke extends OperationTargetMirror  {

        /** {@return the invokable method.} */
        Method method();
    }

    // Maybe just MethodHandleInvoke
    public non-sealed interface OfSyntheticInvoke extends OperationTargetMirror {
        MethodType methodType();
    }
}
