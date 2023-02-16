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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import app.packed.extension.Extension;
import app.packed.extension.container.ContainerTemplate;
import app.packed.extension.container.ExtensionLink;
import app.packed.extension.operation.OperationTemplate;
import app.packed.util.Key;

/**
 *
 */
public record PackedContainerTemplate(ContainerKind kind, Class<?> holderClass) implements ContainerTemplate {

    /** The root lifetime of an application. */
    public static final ContainerTemplate APPLICATION_ROOT = new PackedContainerTemplate(ContainerKind.ROOT, void.class);

    /** {@inheritDoc} */
    @Override
    public Class<?> holderClass() {
        return void.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> holderKeys() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public List<OperationTemplate> operations() {
        return List.of();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerTemplate holder(Class<?> guest) {
        // I don't think we are going to do any checks here?
        // Well not interface, annotation, abstract class, ...
        return new PackedContainerTemplate(kind, guest);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<? extends Extension<?>>> installedBy() {
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerTemplate installedBy(Class<? extends Extension<?>> installedBy) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerTemplate linkWith(ExtensionLink channel) {
        return null;
    }
}
