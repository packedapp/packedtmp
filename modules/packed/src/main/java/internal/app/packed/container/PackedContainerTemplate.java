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
import java.util.function.Function;

import app.packed.component.guest.OldContainerTemplateLink;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerInstaller;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.context.ContextTemplate;
import app.packed.extension.Extension;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.component.ComponentTagHolder;

/** Implementation of {@link ContainerTemplate}. */
public record PackedContainerTemplate<H extends ContainerHandle<?>>(PackedContainerKind kind, Class<?> holderClass, PackedContainerTemplatePackList links,
        Class<?> resultType, Set<String> componentTags, boolean isManaged) implements ContainerTemplate<H> {

    public PackedContainerTemplate(PackedContainerKind kind) {
        this(kind, void.class);
    }

    public PackedContainerTemplate(PackedContainerKind kind, Class<?> holderType) {
        this(kind, holderType, new PackedContainerTemplatePackList(List.of()), void.class);
    }

    public PackedContainerTemplate(PackedContainerKind kind, Class<?> holderClass, PackedContainerTemplatePackList links, Class<?> resultType) {
        this(kind, holderClass, links, resultType, Set.of(), false);
    }

    @SuppressWarnings("unchecked")
    public Function<? super ContainerInstaller<?>, H> handleFactory() {
        return i -> (H) new ContainerHandle<>(i);
    }

    public boolean isManaged() {
        if (kind == PackedContainerKind.UNMANAGED) {
            return false;
        }
        return true;
    }

    public PackedContainerInstaller<?> newInstaller(Class<? extends Extension<?>> installedBy, ApplicationSetup application, @Nullable ContainerSetup parent) {
        PackedContainerInstaller<?> installer = new PackedContainerInstaller<>(this, null, parent, installedBy);

        for (PackedContainerLink b : installer.template.links().packs) {
            if (b.onUse() != null) {
                b.onUse().accept(installer);
            }
        }
        return installer;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerTemplate<H> reconfigure(Consumer<? super Configurator> configure) {
        return configure(this, configure);
    }

    public static <H extends ContainerHandle<?>> PackedContainerTemplate<H> configure(PackedContainerTemplate<H> template,
            Consumer<? super Configurator> configure) {
        PackedContainerTemplateConfigurator<H> c = new PackedContainerTemplateConfigurator<>(template);
        configure.accept(c);
        return c.pbt;
    }

    public final static class PackedContainerTemplateConfigurator<H extends ContainerHandle<?>> implements ContainerTemplate.Configurator {

        public PackedContainerTemplate<H> pbt;

        public PackedContainerTemplateConfigurator(PackedContainerTemplate<H> pbt) {
            this.pbt = pbt;
        }

//        /** {@inheritDoc} */
//        @Override
//        public PackedContainerTemplateConfigurator carrierType(Class<?> guest) {
//            // I don't think we are going to do any checks here?
//            // Well not interface, annotation, abstract class, ...
//            // All lifetime operation will change...
//            this.pbt = new PackedContainerTemplate(pbt.kind, guest, pbt.links, pbt.resultType, pbt.componentTags, pbt.lifecycleKind);
//            return this;
//        }

        // expects results. Maa ogsaa tage en Extension...
        public PackedContainerTemplateConfigurator<H> expectResult(Class<?> resultType) {
            this.pbt = new PackedContainerTemplate<>(pbt.kind, pbt.holderClass, pbt.links, resultType, pbt.componentTags, pbt.isManaged);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PackedContainerTemplateConfigurator<H> lifetimeOperationAddContext(int index, ContextTemplate template) {
            throw new UnsupportedOperationException();
        }

        public Wirelet wirelet() {
            return null;
        }

        public PackedContainerTemplateConfigurator<H> withKind(PackedContainerKind kind) {
            this.pbt = new PackedContainerTemplate<>(kind, pbt.holderClass, pbt.links, pbt.resultType, pbt.componentTags, pbt.isManaged);
            return this;
        }

        /** {@inheritDoc} */
        @Override
        public PackedContainerTemplateConfigurator<H> withPack(OldContainerTemplateLink channel) {
            PackedContainerTemplatePackList tunnels = pbt.links.add((PackedContainerLink) channel);
            this.pbt = new PackedContainerTemplate<>(pbt.kind, pbt.holderClass, tunnels, pbt.resultType, pbt.componentTags, pbt.isManaged);
            return this;
        }

        public PackedContainerTemplateConfigurator<H> withWirelets(Wirelet... wirelets) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Configurator componentTag(String... tags) {
            this.pbt = new PackedContainerTemplate<>(pbt.kind, pbt.holderClass, pbt.links, pbt.resultType,
                    ComponentTagHolder.copyAndAdd(pbt.componentTags, tags), pbt.isManaged);
            return this;

        }
    }

//
//        /**
//         * A set of keys that are available for injection into a lifetime bean using {@link FromLifetimeChannel}.
//         * <p>
//         * This method is mainly used for informational purposes.
//         *
//         * @return the set of keys available for injection
//         */
//        Set<Key<?>> carrierKeys();
//
//        /** {@return a list of the lifetime operation of this container template.} */
//        List<OperationTemplate> lifetimeOperations();

}
