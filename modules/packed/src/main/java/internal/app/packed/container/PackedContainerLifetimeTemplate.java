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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import app.packed.lifetime.ContainerLifetimeTemplate;

/**
 *
 */
public final class PackedContainerLifetimeTemplate implements ContainerLifetimeTemplate {

    public final ContainerKind kind;

    // Can only be used internally
    public static final ContainerLifetimeTemplate ROOT = new PackedContainerLifetimeTemplate(ContainerKind.ROOT);

    public PackedContainerLifetimeTemplate(ContainerKind kind) {
        this.kind = requireNonNull(kind);
    }

    /** {@inheritDoc} */
    @Override
    public Mode mode() {
        return null;
    }
}