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
package app.packed.hook;

/**
 *
 */

// Tjah... er vel ikke saa anderledes en Contract.Hooks()...

// Eneste problem.. er man ikke kan styre Lifecycle. Man kan kalde hooks efter f.eks. shutdown....
// Men det er jo ikke anderledes end at kalde en service efter shutdown...
// Maaske er det kun noget man skal vaere nervoes for
interface HookService {

    // Streams, vs collections...
    // vs det vill vaere rart at kunne kalde
    // vs, man ligesom kan lukke hooks ned. Hvis de altid bliver eksvekveret i en forEach();
}
