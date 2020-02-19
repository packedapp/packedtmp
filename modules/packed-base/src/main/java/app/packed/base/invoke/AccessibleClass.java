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
package app.packed.base.invoke;

import java.lang.invoke.MethodHandles;

/**
 *
 */
// A wrapper for Class + Lookup object

// Not safe for multiple threads...

// An open class can be partial open???
// Or maybe it only returns members that are open for reading...
// So, for example, if abstract class is not open...
// It is not returned...

//Problemet er lidt at Open Class har en betydning allerede: https://en.wikipedia.org/wiki/Class_(computer_programming)#Open_Class
public interface AccessibleClass {

    // Single constructor (public)

    // ExecutableSource findCreator(); <- fail if not???? Ide'en er at finde en enkelt method handle...

    // UsePrivateLookup
    static AccessibleClass of(MethodHandles.Lookup lookup, Class<?> clazz) {
        // Validate lookup + clazz???
        return of(lookup, clazz, false);
    }

    static AccessibleClass of(MethodHandles.Lookup lookup, Class<?> clazz, boolean registerForNativeImage) {
        throw new UnsupportedOperationException();
    }

    boolean isPrivateLookup();

    // Skal vi have ofPrivate()....

}
// @SidecarMapping(Inject.class, StuffSidecar.class) <- Paa bundle...