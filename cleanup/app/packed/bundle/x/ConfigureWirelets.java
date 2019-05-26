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
package app.packed.bundle.x;

import java.lang.invoke.MethodHandles;

import app.packed.container.Bundle;
import app.packed.inject.Provide;

/**
 * Configure wiring operations are invoked immediately after a bundle has been {@link Bundle#configure() configured}.
 * There are two typical use cases:
 * 
 * The first one involves some light modifications to the runtime the bundle creates. They typically fall into
 * description of runtime, name for container (maybe also injector), and tags. If a wiring operation requires over
 * requiresPatch
 * 
 * 
 * 
 * 
 * The other use case is for patching the bundle, this is typically done via the exchange of the For example, here we
 * switchd
 *
 * 
 */
// ConfigureBundleWiringOperation

// Two types of operations
// Operations that requires that a bundle is patchable
// Operations that does not require it
public class ConfigureWirelets {

    /** Creates a new operation. */
    protected ConfigureWirelets() {}

    /**
     * Creates a new operation with a lookup object. This constructor is only needed if the extending class makes use of the
     * {@link Provide} annotation.
     * 
     * @param lookup
     *            a lookup object that can be used for accessing member on subclasses, such as methods annotated with
     *            {@link Provide}.
     */
    protected ConfigureWirelets(MethodHandles.Lookup lookup) {}

    /**
     * Returns a wiring operation that opens a bundle.
     * 
     * @return a wiring operation that opens a bundle
     */
    // Maybe still open??? Hmm
    // makePatchable();
    public static ConfigureWirelets patchBundle() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wiring operation that opens a bundle.
     * 
     * @param lookup
     *            object that must have read access to the module that defines the bundle
     * @return a wiring operation that opens a bundle
     */
    public static ConfigureWirelets patchBundle(MethodHandles.Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    public static ConfigureWirelets redescribe(String newDescription) {
        throw new UnsupportedOperationException();
    }

    public static ConfigureWirelets rename(String newName) {
        throw new UnsupportedOperationException();
    }

    public static ConfigureWirelets retag(String... newTags) {
        throw new UnsupportedOperationException();
        // Can extend all 3 versions with retag(Function<Set<String>, Set<String>>);
        // rename(Function<String, String>);
    }

    // protected boolean requiresPatchable();
}
