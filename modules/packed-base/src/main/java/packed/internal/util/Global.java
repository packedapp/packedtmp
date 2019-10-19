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
package packed.internal.util;

import java.lang.invoke.MethodHandles;

/**
 *
 */
// Folk kommer til at have global state...
// Saa vi kan ligesaa godt forberede os paa det
// Ville maaske ogsaa vaere rar med en loesning. Hvor man kan opt in.
// Uhh skal ogsaa noget class loaders here
final class Global {

    // WeakReference<Global> <- caller must hang on to it?
    // Or call here every time???
    // Maybe have both

    // Support for cleaner
    public static Global create(MethodHandles.Lookup caller) {
        throw new UnsupportedOperationException();
    }
}
