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
package packed.internal.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Optional;
import java.util.Set;

import app.packed.artifact.ArtifactContext;
import app.packed.base.Nullable;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.ComponentDescriptor;
import app.packed.component.ComponentPath;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerBundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDescriptor.Builder;
import app.packed.container.Extension;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import packed.internal.artifact.PackedAssembleContext;
import packed.internal.component.PackedComponentConfigurationContext;
import packed.internal.component.PackedComponentConfigurationContext.State;
import packed.internal.inject.factory.FactoryHandle;

/**
 *
 */
public class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    public final PackedContainerConfigurationContext pccc;

    public PackedContainerConfiguration(PackedContainerConfigurationContext pccc) {
        super(pccc);
        this.pccc = pccc;
    }

    public PackedContainerConfigurationContext actualContainer() {
        return pccc.actualContainer();
    }

    public final PackedAssembleContext artifact() {
        return pccc.artifact();
    }

    public PackedContainerConfigurationContext assemble() {
        return pccc.assemble();
    }

    @Override
    public <W extends Wirelet> Optional<W> assemblyWirelet(Class<W> type) {
        return pccc.assemblyWirelet(type);
    }

    public void buildDescriptor(Builder builder) {
        pccc.buildDescriptor(builder);
    }

    @Override
    public final void checkConfigurable() {
        pccc.checkConfigurable();
    }

    @Override
    public final ConfigSite configSite() {
        return pccc.configSite();
    }

    public final @Nullable PackedContainerConfigurationContext container() {
        return pccc.container();
    }

    public final int depth() {
        return pccc.depth();
    }

    public final ComponentDescriptor descritor() {
        return pccc.descritor();
    }

    @Override
    public boolean equals(Object obj) {
        return pccc.equals(obj);
    }

    @Override
    public final Optional<Class<? extends Extension>> extension() {
        return pccc.extension();
    }

    @Override
    public Set<Class<? extends Extension>> extensions() {
        return pccc.extensions();
    }

    public MethodHandle fromFactoryHandle(FactoryHandle<?> handle) {
        return pccc.fromFactoryHandle(handle);
    }

    @Override
    public final @Nullable String getDescription() {
        return pccc.getDescription();
    }

    public @Nullable PackedExtensionConfiguration getExtensionContext(Class<? extends Extension> extensionType) {
        return pccc.getExtensionContext(extensionType);
    }

    @Override
    public final String getName() {
        return pccc.getName();
    }

    @Override
    public int hashCode() {
        return pccc.hashCode();
    }

    public final String initializeName(State newState, String setName) {
        return pccc.initializeName(newState, setName);
    }

    @Override
    public String initializeNameDefaultName() {
        return pccc.initializeNameDefaultName();
    }

    @Override
    public <T> SingletonConfiguration<T> install(Class<T> implementation) {
        return pccc.install(implementation);
    }

    @Override
    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        return pccc.install(factory);
    }

    @Override
    public <T> SingletonConfiguration<T> installInstance(T instance) {
        return pccc.installInstance(instance);
    }

    @Override
    public StatelessConfiguration installStateless(Class<?> implementation) {
        return pccc.installStateless(implementation);
    }

    public ArtifactContext instantiateArtifact(WireletPack wc) {
        return pccc.instantiateArtifact(wc);
    }

    @Override
    public boolean isArtifactRoot() {
        return pccc.isArtifactRoot();
    }

    public boolean isInSameContainer(PackedComponentConfigurationContext other) {
        return pccc.isInSameContainer(other);
    }

    @Override
    public void link(ContainerBundle bundle, Wirelet... wirelets) {
        pccc.link(bundle, wirelets);
    }

    @Override
    public void lookup(@Nullable Lookup lookup) {
        pccc.lookup(lookup);
    }

    @Override
    public final ComponentDescriptor model() {
        return pccc.model();
    }

    public ContainerLayer newLayer(String name, ContainerLayer... dependencies) {
        return pccc.newLayer(name, dependencies);
    }
//
//    @Override
//    public void onNamed(Consumer<? super ComponentConfiguration> action) {
//        pccc.onNamed(action);
//    }

    @Override
    public final ComponentPath path() {
        return pccc.path();
    }

    @Override
    public PackedContainerConfiguration setDescription(String description) {
        pccc.setDescription(description);
        return this;
    }

    @Override
    public PackedContainerConfiguration setName(String name) {
        pccc.setName(name);
        return this;
    }

    @Override
    public Class<?> sourceType() {
        return pccc.sourceType();
    }

    @Override
    public String toString() {
        return pccc.toString();
    }

    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return pccc.use(extensionType);
    }

    public <W extends Wirelet> Optional<W> wireletAny(Class<W> type) {
        return pccc.wireletAny(type);
    }
}
