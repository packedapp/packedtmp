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
package packed.internal.host.prototype;

import app.packed.artifact.ArtifactDriver;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;
import packed.internal.host.api.HostDriver;

/**
 *
 */
public interface PrototypeHost<A> {

    static <A> HostDriver<PrototypeHostConfiguration<A>, PrototypeHost<A>, A> driver(ArtifactDriver<A> driver, Bundle bundle, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
// Noget 