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
import java.util.function.Consumer;

import app.packed.binding.Key;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.context.ContextTemplate;
import app.packed.extension.Extension;
import app.packed.lifetime.LifecycleKind;
import app.packed.operation.OperationTemplate;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.component.ComponentTagManager;
import sandbox.extension.container.ContainerTemplateLink;

/** Implementation of {@link ContainerTemplate}. */
public record PackedContainerTemplate(PackedContainerKind kind, Class<?> holderClass, PackedContainerTemplatePackList links, Class<?> resultType,
        Set<String> componentTags, LifecycleKind lifecycleKind) implements ContainerTemplate {

    public PackedContainerTemplate(PackedContainerKind kind) {
        this(kind, void.class);
    }

    public PackedContainerTemplate(PackedContainerKind kind, Class<?> holderType) {
        this(kind, holderType, new PackedContainerTemplatePackList(List.of()), void.class);
    }

    public PackedContainerTemplate(PackedContainerKind kind, Class<?> holderClass, PackedContainerTemplatePackList links, Class<?> resultType) {
        this(kind, holderClass, links, resultType, Set.of(), LifecycleKind.UNMANAGED);
    }

    public LifecycleKind lifecycleKind() {
        if (kind == PackedContainerKind.ROOT_UNMANAGED || kind == PackedContainerKind.UNMANAGED) {
            return LifecycleKind.UNMANAGED;
        }
        return LifecycleKind.MANAGED;
    }

    public PackedContainerInstaller newInstaller(Class<? extends Extension<?>> installedBy, ApplicationSetup application, @Nullable ContainerSetup parent) {
        PackedContainerInstaller installer = new PackedContainerInstaller(this, null, parent, installedBy);

        for (PackedContainerTemplatePack b : installer.template.links().packs) {
            if (b.onUse() != null) {
                b.onUse().accept(installer);
            }
        }
        return installer;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerTemplate reconfigure(Consumer<? super Configurator> configure) {
        return configure(this, configure);
    }

    public static PackedContainerTemplate configure(PackedContainerTemplate template, Consumer<? super Configurator> configure) {
        PackedContainerTemplateConfigurator c = new PackedContainerTemplateConfigurator(template);
        configure.accept(c);
        return c.pbt;
    }

    public final static class PackedContainerTemplateConfigurator implements ContainerTemplate.Configurator {

        public PackedContainerTemplate pbt;

        public PackedContainerTemplateConfigurator(PackedContainerTemplate pbt) {
            this.pbt = pbt;
        }

        /** {@inheritDoc} */
        @Override
        public PackedContainerTemplateConfigurator carrierType(Class<?> guest) {
            // I don't think we are going to do any checks here?
            // Well not interface, annotation, abstract class, ...
            // All lifetime operation will change...
            this.pbt = new PackedContainerTemplate(pbt.kind, guest, pbt.links, pbt.resultType, pbt.componentTags, pbt.lifecycleKind);
            return this;
        }

        // expects results. Maa ogsaa tage en Extension...
        public PackedContainerTemplateConfigurator expectResult(Class<?> resultType) {
            this.pbt = new PackedContainerTemplate(pbt.kind, pbt.holderClass, pbt.links, resultType, pbt.componentTags, pbt.lifecycleKind);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PackedContainerTemplateConfigurator lifetimeOperationAddContext(int index, ContextTemplate template) {
            throw new UnsupportedOperationException();
        }

        public Wirelet wirelet() {
            return null;
        }

        public PackedContainerTemplateConfigurator withKind(PackedContainerKind kind) {
            this.pbt = new PackedContainerTemplate(kind, pbt.holderClass, pbt.links, pbt.resultType, pbt.componentTags, pbt.lifecycleKind);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PackedContainerTemplateConfigurator withPack(ContainerTemplateLink channel) {
            PackedContainerTemplatePackList tunnels = pbt.links.add((PackedContainerTemplatePack) channel);
            this.pbt = new PackedContainerTemplate(pbt.kind, pbt.holderClass, tunnels, pbt.resultType, pbt.componentTags, pbt.lifecycleKind);
            return this;
        }

        public PackedContainerTemplateConfigurator withWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Configurator componentTag(String... tags) {
            this.pbt = new PackedContainerTemplate(pbt.kind, pbt.holderClass, pbt.links, pbt.resultType, ComponentTagManager.copyAndAdd(pbt.componentTags, tags),
                    pbt.lifecycleKind);
            return this;

        }
    }

    public record PackedDescriptor(PackedContainerTemplate template) implements ContainerTemplate.Descriptor {

        /** {@inheritDoc} */
        @Override
        public List<OperationTemplate> lifetimeOperations() {
            return List.of();
        }

        /** {@inheritDoc} */
        @Override
        public Set<Key<?>> carrierKeys() {
            return template.links.keys();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Descriptor descriptor() {
        return new PackedDescriptor(this);
    }
}
