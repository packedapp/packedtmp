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

import app.packed.inject.Provides;

/**
 *
 */
public abstract class UpstreamWiringOperation extends WiringOperation {

    /** Creates a new stage */
    protected UpstreamWiringOperation() {}

    /**
     * Creates a new stage with a lookup object. This constructor is only needed if the extending class makes use of the
     * {@link Provides} annotation.
     * 
     * @param lookup
     *            a lookup object that will be used for invoking methods annotated with {@link Provides}.
     */
    protected UpstreamWiringOperation(MethodHandles.Lookup lookup) {
        super(lookup);
    }
}
