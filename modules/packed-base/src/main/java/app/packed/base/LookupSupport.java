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
package app.packed.base;

import java.lang.invoke.MethodHandles;

/**
 *
 */
// Ideen er at vi dropper support for generisk lookup thingling...
// Istedet for kalder man ind på den her metode...
// I en statisk initializer i klassen...
// Den anden annotering er sgu for forvirrende...

// Cross module extension hirakies
public class LookupSupport {

    public static void openForSubclasses(MethodHandles.Lookup lookup, Class<?> superClass) {
        // allows subclasses...
    }

    public static void openForSubclasses(MethodHandles.Lookup lookup, Class<?> superClass, String... modules) {
        // allows subclasses...
    }
}
