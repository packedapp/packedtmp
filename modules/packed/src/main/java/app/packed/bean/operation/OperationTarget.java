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
package app.packed.bean.operation;

/**
 *
 */
// Tror bare vi har et enkelt interface
public interface OperationTarget {

    public interface OfBeanInstanceAccess extends OperationTarget {} // ofLifetimePool? Hmm

    public interface OfMethodHandleInvoke extends OperationTarget {} // ofSynthetic?

    public interface OfFunctionCall extends OperationTarget {}
    
    public interface OfInjectVariable extends OperationTarget {}
    
    public interface OfConstructorInvoke extends OperationTarget {}

    public interface OfFieldAccess extends OperationTarget {
        boolean allowRead();
        boolean allowWrite();
    }

    public interface OfMethodInvoke extends OperationTarget {}
}
// OfBeanInstance - Something that just returns the bean instance
// OfConstructor -
// OfField -
// OfFunction - invoke a function
// OfMethod -
// OfMethodHandle - Synthetic

// of(Instace).map, of(method).map
// of(Method).bind
