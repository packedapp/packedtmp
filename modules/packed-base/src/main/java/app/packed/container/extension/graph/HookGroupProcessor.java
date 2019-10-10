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
package app.packed.container.extension.graph;

import app.packed.component.ComponentConfiguration;

/**
 *
 */
@FunctionalInterface

// Problem den virker ikke paa runtime!!! dough dumme
// Eller paa bundles...

// Ved ikke lige hvad jeg taenkte....
public interface HookGroupProcessor<P, G> {

    void process(P processor, ComponentConfiguration<?> cc, G group);
}
