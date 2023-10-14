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

import java.util.Collection;
import java.util.Optional;

import app.packed.bean.BeanMirror;
import app.packed.util.Nullable;
import internal.app.packed.lifetime.BeanLifetimeSetup;

/**
 * Represents a bean whose lifetime is independent of its container's lifetime.
 * <p>
 * The parent lifetime of a bean lifetime is always the lifetime of the container in which is installed.
 * <p>
 * Beans without a lifecycle, such as static beans, or those with an externally managed lifecycle, will never have their
 * own bean lifetime. Instead, they are always part of their container's lifetime, signifying that the bean can only be
 * used as long as the container exists.
 */
public final class BeanLifetimeMirror extends LifetimeMirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(LifetimeSetup)}.
     */
    @Nullable
    private BeanLifetimeSetup lifetime;

    /** Create a new mirror. */
    public BeanLifetimeMirror() {}

    /** {@return all the beans that are in the lifetime.} */
    public Collection<BeanMirror> beans() {
        return lifetime().beans.stream().map(b -> b.mirror()).toList();
    }

    /** {@return the lifetime of the container this bean is contained within.} */
    // Does this method confuse more than it helps?
    ContainerLifetimeMirror containerLifetime() {
        return lifetime().parent().mirror();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the mirro.
     *
     * @param lifetime
     *            the internal configuration of lifetime
     */
    void initialize(BeanLifetimeSetup lifetime) {
        if (this.lifetime != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.lifetime = lifetime;
    }

    /**
     * {@return the internal configuration of the lifetime.}
     *
     * @throws IllegalStateException
     *             if {@link #initialize(BeanLifetimeSetup)} has not been called.
     */
    @Override
    BeanLifetimeSetup lifetime() {
        BeanLifetimeSetup l = lifetime;
        if (l == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return l;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ContainerLifetimeMirror> parent() {
        return Optional.of(containerLifetime());
    }
}
