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
package app.packed.container;

import app.packed.extension.Extension;
import internal.app.packed.container.DomainSetup;

/**
 * A mirror of a domain.
 */
// Kan maaske have en EventRouter? DeliveredEvent = <Domain, Event>
public class DomainMirror<E extends Extension<E>> {

    /** The domain configuration. */
    private final DomainSetup domain = DomainSetup.MI.initialize();

    // IDK
    Class<? extends Extension<?>> domainExtension() {
        return domain.root.extensionType;
    }

    /**
     * {@return the local name of this domain for the specified container.}
     * <p>
     * Domain instance may be available with different names in different containers.
     *
     * @param container
     *            the container to return a local name for
     * @throws IllegalArgumentException
     *             if this domain instance is not available in the specified container
     */
    public final String domainLocalName(ContainerMirror container) {
        throw new UnsupportedOperationException();
    }

    /** {@return the name of this domain.} */
    public final String domainName() {
        return domain.name;
    }

    /** {@return the owner of this domain instance.} */
    public final Realm domainOwner() {
        return domain.owner.realm();
    }

    /** {@return the root container of the domain.} */
    public ContainerMirror domainRoot() {
        return domain.root.container.mirror();
    }

    /** {@return a tree containing every container where this domain instance is present.} */
    public ContainerTreeMirror domainTree() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return other instanceof DomainMirror<?> m && getClass() == m.getClass() && domain == m.domain;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return domain.hashCode();
    }
}
