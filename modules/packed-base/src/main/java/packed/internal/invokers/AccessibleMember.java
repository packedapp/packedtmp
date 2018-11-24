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
package packed.internal.invokers;

import static java.util.Objects.requireNonNull;

/**
 * An accessible field, constructor or method with a metadata object.
 */

// Maybe we are going to use it again?
public abstract class AccessibleMember<T> {

    /** An metadata object, can probably change to non-null */
    private final T metadata;

    AccessibleMember() {
        this.metadata = null;
    }

    /**
     * @param metadata
     */
    AccessibleMember(T metadata) {
        this.metadata = requireNonNull(metadata);
    }

    public final T metadata() {
        return metadata;
    }
}
