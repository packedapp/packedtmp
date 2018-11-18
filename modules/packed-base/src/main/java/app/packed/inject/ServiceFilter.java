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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.util.Nullable;

/**
 *
 */
public abstract class ServiceFilter {
    @Nullable
    final MethodHandles.Lookup lookup;

    /** Creates a new filter */
    ServiceFilter() {
        this.lookup = null;
    }

    /**
     * Creates a new filter
     * 
     * @param lookup
     *            a lookup object that will be used for invoking methods annotated with {@link Provides}.
     */
    ServiceFilter(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup, "lookup is null");
    }

}
