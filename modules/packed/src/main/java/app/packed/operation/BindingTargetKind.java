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

/**
 *
 */
public enum BindingTargetKind {
    
    // LAZY (I think we need this...) IDK Lifetime_POOL beanen kan jo vaere lazy
    
    /** A constant. */
    CONSTANT,

    /** An argument that is provided when invoking the operation. */
    ARGUMENT,
    
    // The value is looked up in the lifetime pool...
    LIFETIME_POOL,
        
    /** The binding is a result of another operation. */
    OPERATION;
}
//
//
///**
//*
//*/
//// Det var tidligere den her...
//// Maaske er service instance... ikke en Operation Type... Men en bindingKind
//interface HowDepIsProvidedKind {
//   
//   // Build-time Constant
//   
//   // Result of an operation?
//   
//   // Background input param to operation
//   
//   // Lookup in some datastructed (service) Er det ikke bare result af en operation?
//}
