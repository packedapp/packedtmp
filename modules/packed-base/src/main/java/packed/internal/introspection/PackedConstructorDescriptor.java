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
package packed.internal.introspection;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Constructor;

public final class PackedConstructorDescriptor<T> extends PackedExecutableDescriptor {

    /** The constructor that is being mirrored. */
    private final Constructor<?> constructor;

    /**
     * Creates a new InternalConstructorDescriptor from the specified constructor.
     *
     * @param constructor
     *            the constructor to create a descriptor from
     */
    public PackedConstructorDescriptor(Constructor<?> constructor) {
        super(requireNonNull(constructor, "constructor is null"));
        this.constructor = constructor;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return format(constructor);
    }
}
