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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import app.packed.base.Key;
import app.packed.inject.InjectionContext;

/** The default implementation of {@link InjectionContext}. */
public class PackedInjectionContext implements InjectionContext {
    private final Class<?> target;
    private final Set<Key<?>> keys;

    public PackedInjectionContext(Class<?> target, Set<Key<?>> keys) {
        this.target = requireNonNull(target);
        this.keys = requireNonNull(keys);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> keys() {
        return keys;
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> targetClass() {
        return target;
    }

    @Override
    public String toString() {
        return "InjectionContext[" + target.getCanonicalName() + "]: " + keys;
    }
}
