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
package app.packed.artifact.hosttest;

import app.packed.container.Bundle;

/**
 *
 */

// Altsaa vi vil jo teknisk set ogsaa gerne kunne bruge dette distribuerede....

public interface GuestImage extends SomeGuest {

    // Altsaa den laver vel et image per use site.
    // Det er jo meningen den er statisk...
    static GuestImage composeOnFirstUse(Bundle b) {
        throw new UnsupportedOperationException();
    }
}
