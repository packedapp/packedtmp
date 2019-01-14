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
package xxx;

import app.packed.inject.ServiceConfiguration;

/**
 *
 */

// Api lifecycle management
public class ApiBundle {

    protected final <T> T asDeprecated(T t, String reason) {
        return t;
    }

    protected final <T> T asPreview(T t, String reason) {
        return t;
    }

    final void ifPropertySet(String value, Runnable r) {
        // SetThreadLocal
        ifPropertySet("foo", () -> {
            // bind("Foo");
        });
        // ClearThreadLocal
    }

    // Ideen er at man man extend et Bundle, med et nyt bundle der har test information
    // Lav nogle testvaerktoejer der aabner dem istedet syntes jeg
    // Eller saetter et pre-filter ind...
    protected final void overwrite(ServiceConfiguration<?> sc) {

    }

    // requireAll();
    // require(Predicate<? super Dependenc> p); //require(e->!e.isOptional);
}
