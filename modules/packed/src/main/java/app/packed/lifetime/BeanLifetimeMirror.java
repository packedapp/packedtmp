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

import java.util.Optional;

import app.packed.bean.BeanMirror;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.framework.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.lifetime.BeanLifetimeSetup;
import internal.app.packed.lifetime.LifetimeSetup;

/**
 * A bean lifetime represents a bean whose lifetime is independent of its container's lifetime.
 * <p>
 * A bean lifetime always has a container lifetime as a parent
 * 
 * <p>
 * Static and functional beans never have their own lifetime but will always their containers lifetime.
 * As they are valid as long as the container exists.
 */
public final class BeanLifetimeMirror extends LifetimeMirror {

    /**
     * The internal configuration of the operation we are mirrored. Is initially null but populated via
     * {@link #initialize(LifetimeSetup)}.
     */
    @Nullable
    private BeanLifetimeSetup lifetime;

    /**
     * Create a new operation mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public BeanLifetimeMirror() {}

    /** {@return the bean.} */
    public BeanMirror bean() {
        return lifetime().bean.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof LifetimeMirror m && lifetime() == m.lifetime();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return lifetime().hashCode();
    }

    /**
     * Invoked by {@link Extension#mirrorInitialize(ExtensionMirror)} to set the internal configuration of the extension.
     * 
     * @param owner
     *            the internal configuration of the extension to mirror
     */
    final void initialize(BeanLifetimeSetup operation) {
        if (this.lifetime != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.lifetime = operation;
    }
    /**
     * {@return the internal configuration of operation.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(ApplicationSetup)} has not been called.
     */
    BeanLifetimeSetup lifetime() {
        BeanLifetimeSetup a = lifetime;
        if (a == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return a;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<ContainerLifetimeMirror> parent() {
        return Optional.of(lifetime().parent.mirror());
    }
}
