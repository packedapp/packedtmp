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
package packed.internal.sidecar;

import app.packed.base.Nullable;
import app.packed.sidecar.MethodSidecar;

/**
 *
 */
// Do we support custom sidecar types???
// Ved ikke hvad vi skal bruge den til...
public enum SidecarType {

//    FUNCTION, 
//
//    /** A {@link FieldSidecar field sidecar} represented by {@link FieldSidecarModel}. */
//    FIELD,
//
//    /** A {@link ConstructorSidecar constructor sidecar} represented by {@link ConstructorSidecarModel}. */
//    CONSTRUCTOR,

    /** A {@link MethodSidecar method sidecar} represented by {@link MethodSidecarModel}. */
    METHOD;

    @Nullable
    public static SidecarType of(Class<?> implementation) {
        throw new UnsupportedOperationException();
    }
}

//Method -> Static one per method | Instance -> One per Object instance [1 if method static, N if method instance]

//Constructor??? <- 

//ConstructionSidecar <- Attached to a component as long as it is in the construction phase?

//Sidecars har altid et hieraki.. en raekkefoelge...

//Alle methods/fields -> Functions... (En function er meta data resolved)
//En function behoever ikke komme fra fra en metode. Men man kan ogsaa attache den til en component...

//Component = Function*, GetField-> Function with return value, SetField->void function with a single parameter. Get/Set -> Return value + Single parameter

//Packlet... <- Component Packlet, Instance Packlet
//Ved ikke om vi skal have en type... Hvad hvis vi f.eks. vil have en
//NodeSidecar (Distributed) 
