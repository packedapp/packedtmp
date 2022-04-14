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
package app.packed.bean.operation.mirror;

/**
 * The target of an operation.
 */
public interface OperationTargetMirror {

    // Accessing an instance that have previously been computed
    // Was BeanInstance but we create a synthetic operation for for example BeanVarInject.provideInstance
    public interface OfInstanceAccess extends OperationTargetMirror {} // ofLifetimePool? Hmm

    public interface OfMethodHandleInvoke extends OperationTargetMirror {} // ofSynthetic?

    public interface OfFunctionCall extends OperationTargetMirror {}

    // Hmm???
    public interface OfInjectVariable extends OperationTargetMirror {}
    
    // Members
    
    public interface OfConstructorInvoke extends OperationTargetMirror {}

    public interface OfFieldAccess extends OperationTargetMirror {
        boolean allowGet();
        boolean allowSet();
    }

    public interface OfMethodInvoke extends OperationTargetMirror {}
}
// OfBeanInstance - Something that just returns the bean instance
// OfConstructor -
// OfField -
// OfFunction - invoke a function
// OfMethod -
// OfMethodHandle - Synthetic

// of(Instace).map, of(method).map
// of(Method).bind
