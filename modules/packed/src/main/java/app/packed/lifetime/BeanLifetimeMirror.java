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
package app.packed.lifetime;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.bean.BeanMirror;
import internal.app.packed.ValueBased;
import internal.app.packed.lifecycle.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifecycle.lifetime.LifetimeSetup;

/**
 * Represents a bean whose lifetime is independent of its container's lifetime.
 * <p>
 * The parent lifetime of a bean lifetime is always the lifetime of the container in which is installed.
 * <p>
 * Beans without a lifecycle, such as static beans, or those with an externally managed lifecycle, will never have their
 * own bean lifetime. Instead, they are always part of their container's lifetime, signifying that the bean can only be
 * used as long as the container exists.
 */
@ValueBased
public final class BeanLifetimeMirror extends LifetimeMirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(LifetimeSetup)}.
     */
    private final BeanLifetimeSetup lifetime;

    /** Create a new mirror. */
    BeanLifetimeMirror(BeanLifetimeSetup lifetime) {
        this.lifetime = requireNonNull(lifetime);
    }

    /** {@return all the beans that are in the lifetime.} */
    public BeanMirror bean() {
        return lifetime.bean.mirror();
    }

    /** {@inheritDoc} */
    @Override
    LifetimeSetup lifetime() {
        return lifetime;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ContainerLifetimeMirror> parent() {
        return Optional.of(lifetime.parent().mirror());
    }
}
