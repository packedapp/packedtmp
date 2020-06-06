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

import app.packed.artifact.ArtifactDriver;

/**
 *
 */

// En host er fuldstaendig udafhandig af ejeren...

// A host is not a component type it is a property of a component

// Configuring a component as a host. Will make available one or more services available to be injected into the host component.
// But also what is available to be injected into the guest...
public class HostDriver2 {

    public static HostDriver2 singleLine(ArtifactDriver<?> driver) {
        throw new UnsupportedOperationException();
    }

    public HostDriver2 withLine() {
        // man bygger ovenpaa...
        throw new UnsupportedOperationException();
    }
}

// Her sporgsmaalet om alle