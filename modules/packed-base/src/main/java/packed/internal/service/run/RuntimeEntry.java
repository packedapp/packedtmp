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
package packed.internal.service.run;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.config.ConfigSite;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceDescriptor;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.service.ServiceEntry;
import packed.internal.service.build.BuildEntry;

/** An entry that represents a service at runtime. */
public abstract class RuntimeEntry<T> implements ServiceEntry<T>, ServiceDescriptor {

    /** The point where this entry was registered. */
    private final ConfigSite configSite;

    /** An (optionally) description of the service. */
    @Nullable
    private final String description;

    /** The key under which the service is available. */
    private final Key<T> key;

    /**
     * Creates a new runtime node from a build entry.
     *
     * @param buildEntry
     *            the build node to create the runtime node from
     */
    RuntimeEntry(BuildEntry<T> buildEntry) {
        this.configSite = requireNonNull(buildEntry.configSite());
        this.description = buildEntry.getDescription();
        this.key = requireNonNull(buildEntry.key());
    }

    RuntimeEntry(ConfigSite configSite, Key<T> key, @Nullable String description) {
        this.configSite = requireNonNull(configSite);
        this.description = description;
        this.key = requireNonNull(key);
    }

    /** {@inheritDoc} */
    @Override
    public final ConfigSite configSite() {
        return configSite;
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(description);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean hasUnresolvedDependencies() {
        return false;
    }

    @Override
    public abstract InstantiationMode instantiationMode();

    /** {@inheritDoc} */
    @Override
    public final Key<T> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public final RuntimeEntry<T> toRuntimeEntry() {
        return this; // this entry is already a runtime entry
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key());
        sb.append("[").append(instantiationMode()).append(']');
        if (description != null) {
            sb.append(":").append(description);

        }
        return sb.toString();
    }
}
//
//// Ideen er at vi kan komme med forslag til andre noegler end den forespurgte
//// F.eks. man eftersporger Foo.class, men maaske er der en FooImpl et sted
// public boolean isAssignableTo(Class<?> type) {
// throw new UnsupportedOperationException();
// }