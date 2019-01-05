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
package app.packed.bundle;

import java.lang.invoke.MethodHandles;

import app.packed.inject.Provides;

/**
 * A bundling operation that
 */
public abstract class BundlingExportOperation extends BundlingOperation {

    /** Creates a new operation. */
    protected BundlingExportOperation() {}

    /**
     * Creates a new operation with a lookup object. This constructor is only needed if the extending class makes use of the
     * {@link Provides} annotation.
     * 
     * @param lookup
     *            a lookup object that can be used for accessing member on subclasses, such as methods annotated with
     *            {@link Provides}.
     */
    protected BundlingExportOperation(MethodHandles.Lookup lookup) {
        super(lookup);
    }
}
