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
package packed.internal.container.extension;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import app.packed.container.Extension;

/**
 *
 */
public class LittleCache {

    Map<Long, Order> mm = new HashMap<>();

    // Paa PackedContainerConfiguration
    // Ideen er at gemme en
    // long som bliver OR'ed hver gang en extension bliver brugt.

    // Hvis vi har en extension med en id>64 har vi en backup...

    // Eller ogsaa bruger vi et bit set???

    // ---------------------------

    /// Hver 100 for spoergelse
    // Check we om map.size>200)
    //// Hvilet den i 99,99% af tilfaelde aldrig vil vaere...

    // Hvis ja, finder vi medianen. Og smide 50 % ud eller lignende

    // OnUsage {

    // Long bitMap

    class Order {
        WeakReference<Class<? extends Extension>>[] array; // sorted

        AtomicLong hits = new AtomicLong();
        // Class<?> c array.
        // extension.get()

    }
}
