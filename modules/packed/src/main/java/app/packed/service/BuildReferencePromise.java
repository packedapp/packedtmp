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
package app.packed.service;

import app.packed.binding.Key;

/**
 *
 */
// Bruger den i situationer hvor vi vil injecte noget der kan foerst kan laves paa runtime
// Ideen er at vi laver virtuelle instancer af den paa build time (Invoker)service naar vi bygger.
// Den kan saa bliver injected on runtime

// Men vi extracter alt hvad vi kan fra T

// Fx Invokers
public interface BuildReferencePromise<T> {
    Key<?> key();
}
