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
package packed.internal.container;

import app.packed.container.AnyBundle;

/**
 *
 */

// Ideen er vi wrapper container sourcen.
// Saa vi kan returner. Den klasse vi skal bruge som standard for lookups.
// Default name

// ??? Do we want to include wirelets???

// T what we want to return????

// SourceDescriptor?

public class InternalContainerSource {

    public InternalContainerSource appOf(AnyBundle bundle) {
        throw new UnsupportedOperationException();
    }

    public InternalContainerSource link(AnyBundle bundle) {
        throw new UnsupportedOperationException();
    }
}
