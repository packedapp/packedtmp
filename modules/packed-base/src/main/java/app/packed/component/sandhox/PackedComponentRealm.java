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
package app.packed.component.sandhox;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Nullable;

/**
 *
 */
// Do we maintain the lookup object???
// I should think not, any method handles we need we generate before... 

// Resursive classes cannot be inlined..
// Maaske er det bare en klasse array...
// Eller maaske har den componenten inde i maven...
public final class PackedComponentRealm implements ComponentRealm {

    /** Any parent this realm might have. */
    private final PackedComponentRealm parent;

    /** The realm class. */
    private final Class<?> realmClass;

    public PackedComponentRealm(Class<?> realmClass) {
        this(null, realmClass);
    }

    private PackedComponentRealm(@Nullable PackedComponentRealm parent, Class<?> realmClass) {
        this.parent = parent;
        this.realmClass = requireNonNull(realmClass);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentRealm spawn(Class<?> clazz) {
        requireNonNull(clazz, "clazz is null");
        return new PackedComponentRealm(this, clazz);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ComponentRealm> parent() {
        return Optional.ofNullable(parent);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> realmClass() {
        return realmClass;
    }
}
