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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import packed.internal.bean.hooks.PackedBeanField.BuildTimeFieldTargetMirror;
import packed.internal.bean.hooks.PackedBeanMethod.BuildTimeMethodTargetMirror;

/**
 * The target of an operation.
 * 
 * @see OperationMirror#target()
 */
// Proev at undgaa at smid for meget information fra OperationMirror her...

// Hvad med Factory1<>??? Her taenker vi paa at der bliver fanget annoteringer...
// Er det Saa Method invoke???Eller FunctionCall

// Maaske drop Invoke til di
// OperationTargetMirror tror jeg bedre jeg kunne lide
public interface OperationTargetMirror { //extends AnnotatedMember?

    // Accessing an instance that have previously been computed
    // Was BeanInstance but we create a synthetic operation for for example BeanVarInject.provideInstance
    // Giver ikke mening for provide...

    // Kan ikke komme paa andre brugbare ting end @Provide
    public interface OfInstanceAccess extends OperationTargetMirror {
        // empty if the instance was provided
        // otherwise the operation that created it, and stored it somewhere.
        Optional<OperationTargetMirror> origin();

        // Har maaske ogsaa noget LifetimePoolMirror her????
    } // ofLifetimePool? Hmm

    // invoke exact?
    public interface OfMethodHandleInvoke extends OperationTargetMirror {} // ofSynthetic?

////////////////////////////////

    public interface OfFunctionCall extends OperationTargetMirror {}

    // Members
    /** Represents an operation that invokes a constructor. */
    public interface OfConstructorInvoke extends OperationTargetMirror {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();
    }

    /** Represents an operation that gets, sets or updates a field. */
    public sealed interface OfFieldAccess extends OperationTargetMirror permits BuildTimeFieldTargetMirror {

        boolean allowGet();

        boolean allowSet();

        /** {@return the underlying field.} */
        Field field();
    }

    /** Represents an operation that invokes a method. */
    public sealed interface OfMethodInvoke extends OperationTargetMirror permits BuildTimeMethodTargetMirror {

        /** {@return the underlying method.} */
        Method method();
    }
}
// OfBeanInstance - Something that just returns the bean instance
// OfConstructor -
// OfField -
// OfFunction - invoke a function
// OfMethod -
// OfMethodHandle - Synthetic

// of(Instace).map, of(method).map
// of(Method).bind
