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
package internal.app.packed.binding;

/**
 *
 */
public enum SuppliedBindingKind {

    /** Generated doing codegen (possible lazy) */
    CODEGEN,

    /** Once per bean instance? */
    // Cannot really use it on an operation
    LAZY,  //lazy shared at runtime

    //
    /** Every time the binding is requested */
    PER_INVOCATION;
}
// Binding
// --Manual
// Hook
//// AnnotatedField    (Annotation)
//// AnnotatedVariable (Annotation)
// Service
//// Operation
//// Bean
//// Context (Class)
//// Namespace (Name)


// Hvis jeg går ind og overskriver med en Operation saa virker et evt. service mirror jo ikke