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
package app.packed.context;

import java.util.function.Supplier;

/**
 *
 */
// Modelled like Optional
// Except that it throws ContextCouldNotLocatedException (Not a build time exception)
public interface RuntimeContextLookup<T extends Context<?>> extends Supplier<T> {

    static <T extends Context<?>> RuntimeContextLookup<T> lookup(Class<T> context) {
        throw new UnsupportedOperationException();
    }

    static <T extends Context<?>> RuntimeContextLookup<T> ofMissing() {
        throw new UnsupportedOperationException();
    }
}
