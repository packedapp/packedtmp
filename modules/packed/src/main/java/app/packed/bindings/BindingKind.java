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
package app.packed.bindings;

import java.lang.reflect.Field;

import app.packed.bean.BeanExtensionPoint.BindingHook;

/**
 *
 */
// All dependencies have a resolution kind even those that are unresolved

// Algorithm is completely fixed. But lots of ways to customization

// Binding
//// OnAnnotation
//// OnClass
// Composite
// Key
//// Service
//// FactoryService
public enum BindingKind {

    // Maaske har vi kun en Binding enum value... Ligesom vi kun har en annotering, og en metode paa inspectoren...
    
    /**
     * AnnotationBindingHook
     * 
     * @see BindingHook
     **/
    BINDING_ANNOTATION,

    /**
     * 
     * @see Field#getType()
     * @see Parameter#getType();
     **/
    BINDING_CLASS,

    /**
     * 
     * @see Composite
     * 
     * @see CompositeBindingMirror
     **/
    COMPOSITE, // Always an operation 

    // Key? Service, OperationContext, ExtensionService, LocalService, InitializationBinding
    // Maaske er det FactoryBinding??? A binding that takes a service and can only be used when creating
    // a bean instance
    // method of the bean
    KEY,

    /**
     * Manual is always because of a position
     * 
     * @see BindingKind#ARGUMENT
     * @see OperationHandle#bindManually(int)
     * @see Op#bind(Object)
     * @see Op#bind(int, Object, Object...)
     */
    // Positional or an argument (which is positional as well??)
    MANUAL; //Always a position... But not always a constant...
}

// Check if already manually bound
// Check annotation for Prime annotations
// check raw class for Prime Class