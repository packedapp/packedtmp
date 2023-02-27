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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.packed.extension.container.ContainerTemplate;
import app.packed.extension.container.ExtensionLink;
import app.packed.extension.context.ContextTemplate;
import app.packed.extension.operation.OperationTemplate;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.lifetime.PackedExtensionLink;

/**
 *
 */
public record PackedContainerTemplate(PackedContainerKind kind, Class<?> holderClass, List<PackedExtensionLink> links, @Nullable Class<?> expectsResult)
        implements ContainerTemplate {

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> holderKeys() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public List<OperationTemplate> lifetimeOperations() {
        return List.of();
    }

    // expects results. Maa ogsaa tage en Extension...
    public PackedContainerTemplate expectResult(Class<?> resultType) {
        return new PackedContainerTemplate(kind, holderClass, links, resultType);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerTemplate holder(Class<?> guest) {
        // I don't think we are going to do any checks here?
        // Well not interface, annotation, abstract class, ...
        return new PackedContainerTemplate(kind, guest, links, expectsResult);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerTemplate addLink(ExtensionLink channel) {
        ArrayList<PackedExtensionLink> l = new ArrayList<>(links);
        l.add((PackedExtensionLink) channel);
        return new PackedContainerTemplate(kind, holderClass, List.copyOf(l), expectsResult);
    }

    public PackedContainerTemplate withKind(PackedContainerKind kind) {
        return new PackedContainerTemplate(kind, holderClass, links, expectsResult);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerTemplate lifetimeOperationAddContext(int index, ContextTemplate template) {
        throw new UnsupportedOperationException();
    }
}
