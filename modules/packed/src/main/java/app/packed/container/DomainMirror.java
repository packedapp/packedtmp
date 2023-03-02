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
public class DomainMirror<E extends Extension<E>> {

    /** The domain configuration. */
    private final DomainSetup domain = DomainSetup.MI.initialize();

    // IDK
    Class<? extends Extension<?>> domainExtension() {
        return domain.root.extensionType;
    }

    /** {@return the name of the domain.} */
    public String domainName() {
        return domain.name;
    }

    public Realm domainOwner() {
        return domain.owner.realm();
    }

    /** {@return the root container of the domain.} */
    public ContainerMirror domainRoot() {
        return domain.root.container.mirror();
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
