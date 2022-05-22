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
package app.packed.container.scopedresource.usage;

import app.packed.container.scopedresource.ExtentMirror;

/**
 *
 */

// Svaert da den kan vaere partielt konfigureret.
//// Og fx fyldes ud paa runtime (eller defaults overskrives)

public interface DataSourceMirror extends ExtentMirror<DataSourceExtension> {

    // Renders<T> to "$Fooo$"
    interface RuntimeConfigurable<T> {

        interface Full {}

        interface Partial {}

        interface None {}
    }
}
