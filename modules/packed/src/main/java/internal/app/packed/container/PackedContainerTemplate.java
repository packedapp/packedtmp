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
import java.util.Set;

import app.packed.container.Wirelet;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.context.publish.ContextTemplate;
import sandbox.extension.container.ContainerTemplatePack;
import sandbox.extension.container.ContainerTemplate;
import sandbox.extension.operation.OperationTemplate;

/** Implementation of {@link ContainerTemplate}. */
public record PackedContainerTemplate(PackedContainerKind kind, Class<?> holderClass, PackedContainerTemplatePackList links, Class<?> resultType)
        implements ContainerTemplate {

    public PackedContainerTemplate(PackedContainerKind kind) {
        this(kind, void.class);
    }

    public PackedContainerTemplate(PackedContainerKind kind, Class<?> holderType) {
        this(kind, holderType, new PackedContainerTemplatePackList(List.of()), void.class);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> carrierKeys() {
        return links.keys();
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
    public PackedContainerTemplate carrierType(Class<?> guest) {
        // I don't think we are going to do any checks here?
        // Well not interface, annotation, abstract class, ...
        // All lifetime operation will change...
        return new PackedContainerTemplate(kind, guest, links, resultType);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerTemplate withPack(ContainerTemplatePack channel) {
        PackedContainerTemplatePackList tunnels = links.add((PackedContainerTemplatePack) channel);
        return new PackedContainerTemplate(kind, holderClass, tunnels, resultType);
    }

    public PackedContainerTemplate withKind(PackedContainerKind kind) {
        return new PackedContainerTemplate(kind, holderClass, links, resultType);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerTemplate lifetimeOperationAddContext(int index, ContextTemplate template) {
        throw new UnsupportedOperationException();
    }

    public PackedContainerTemplate withWirelets(Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Wirelet wirelet() {
        return null;
    }
}
