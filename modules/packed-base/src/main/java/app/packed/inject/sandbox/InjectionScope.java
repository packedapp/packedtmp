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
package app.packed.inject.sandbox;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 
 */
public enum InjectionScope {

    /** Typically an annotation. {@link Field}, {@link Method} or {@link Constructor}. */
    FUNCTION, CLASS, CONTAINER;

    // , ARTIFACT,

    /** The service is available */
    // SYSTEM;
}

// Fixed position er altid en annotation, eller en function type...

// Ohhh, f.eks. hvis man bruger caching... Saa kan man lave en function der goer det???
// Som folk saa bruger istedet for den oprindelige... men de er der begge 2. Maaske
// Er den oprindelige endda gemt..

// Foo.caching(Interface<C> d) {} Og saa lave vi noget metode til function mapning...
// Altsaa der er fucking mange muligheder...

// Function instead of Member. And then Variable???
// F.eks. fixed position injection...

// InjectionContext er altid invisible...
/// DVS. Foo(IC, String s) == Foo(String s, IC) i forbindelse med fixed positions

// Er det her mere bredt...
// EntityScope...

//Altsaa f.eks. har en Context et scope...

// InjectionContext -> Function (what can be injected here....)

// Problemet med de scopes.. er f.eks. 
// WireletPipeline... Nah den har scope container...
// Extension -> Container...

// S

// @Inject[Field]   
