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
package packed.internal.component.wirelet;

import app.packed.component.WireletReceive;

/**
 * You should only change the s
 */
public enum WireletScope {

    /**
     * Wirelets and pipelines are only available for injection at runtime. Trying to inject a wirelet or pipeline using
     * {@link WireletReceive} at assembly time will fail with an {@link IllegalStateException}.
     */
    RUNTIME, // The default...

    /**
     * Wirelets and their pipelines are only available at assembly time. Trying to specify any wirelet after assembly time
     * will fail with an {@link IllegalArgumentException}. They can still always be injected.
     */
    // Wirelets can be injected at any time. But users must specify them so they are available at assembly time...
    ASSEMBLY,

    /**
     * Wirelets can be specified at any time. However, Any wirelets specified at assembly time are consumed at assembly
     * time.
     */
    // Should verify be called on pipeline on AssemblyTime??? I'm not sure...
    CONSUME;
}
// Problemet er lidt at vi gerne vil have at almindelige bruger ikke maerker noget til det....
// Saa vi kan ikke rigtig have consume som default... Med mindre de skal fungere forskelligt
// Alt efter om det en user eller extension wirelet.

// ServiceExtension... Vi vil bare processere AssemblyTime wirelets med det samme... (Mixed/Consumed)