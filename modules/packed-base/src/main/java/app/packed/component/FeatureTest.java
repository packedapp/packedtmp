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
package app.packed.component;

import app.packed.app.App;

/**
 *
 */
public class FeatureTest {

    // Skal vi have 3 typer.
    // En for descriptor
    // En for live unmodifiable
    // En for live modifiable

    public void foo(App app) {

        // Total number of services in the component tree
        app.components().mapToInt(c -> c.use(Inj.SERVICES).size()).sum();

        app.components().forEachFeature(Inj.SERVICES, (c, s) -> System.out.println(c.path() + " exposes " + s.size() + " services"));
    }
}
