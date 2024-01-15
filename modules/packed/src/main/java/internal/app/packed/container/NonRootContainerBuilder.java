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

import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.assembly.Assembly;
import app.packed.build.BuildGoal;
import app.packed.container.ContainerLocal;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.Key;
import app.packed.util.Nullable;
import sandbox.extension.container.ContainerHandle.Builder;
import sandbox.extension.container.ContainerTemplate;

// Would love
/** Implementation of {@link ContainerBuilder} for a non-root container. */
public final class NonRootContainerBuilder extends NonBootstrapContainerBuilder implements Builder {

    /** The extension that is installing the container. */
    final Class<? extends Extension<?>> installedBy;

    //boolean isUsed;

    /** Whether we are creating a new application */
  //  boolean newApplication;

    // Cannot take ExtensionSetup, as BaseExtension is not instantiated for a root container
    private NonRootContainerBuilder(PackedContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        super(template);
        this.parent = parent;
        this.installedBy = requireNonNull(installedBy);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup build(Assembly assembly, Wirelet... wirelets) {
        checkNotUsed();
        checkIsConfigurable();

        processBuildWirelets(wirelets);

        ContainerSetup container = buildNow(assembly);
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup build(Wirelet... wirelets) {
        checkNotUsed();
        checkIsConfigurable();

        // Be careful if moving into newContainer, some tests
        // can fail easily
        processBuildWirelets(wirelets);

        ContainerSetup container = newContainer(parent.application, parent.assembly);
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerSetup buildAndUseThisExtension(Wirelet... wirelets) {
        ContainerSetup handle = build(wirelets);
        handle.useExtension(installedBy, null);
        return handle;
    }

    /** {@inheritDoc} */
    @Override
    public <T> Builder carrierProvideConstant(Key<T> key, T constant) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @throws IllegalStateException
     *             if the container is no longer configurable
     */
    public void checkIsConfigurable() {
        if (!parent.assembly.isConfigurable()) {
            throw new IllegalStateException("This assembly is no longer configurable");
        }
    }

    /**
     * Checks that the builder has not been used.
     * <p>
     * The main problem with using
     */
    private void checkNotUsed() {

    }

    /** {@inheritDoc} */
    @Override
    public BuildGoal goal() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public LifetimeKind lifetimeKind() {
        return LifetimeKind.MANAGED;
    }

    @SuppressWarnings("unchecked")
    public <T> Builder localConsume(ContainerLocal<T> local, Consumer<T> action) {
        PackedContainerLocal<?> pcl = (PackedContainerLocal<?>) local;
        action.accept((T) pcl.get(this));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> Builder localSet(ContainerLocal<T> local, T value) {
        locals.put((PackedContainerLocal<?>) local, value);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Builder named(String name) {
        this.name = name;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Builder specializeMirror(Supplier<? extends ContainerMirror> supplier) {
        this.containerMirrorSupplier = supplier;
        return this;
    }

    public static NonRootContainerBuilder of(ContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        NonRootContainerBuilder pcb = new NonRootContainerBuilder((PackedContainerTemplate) template, installedBy, application, parent);

        for (PackedContainerTemplatePack b : pcb.template.links().packs) {
            b.build(pcb);
        }
        return pcb;
    }
}

// Problemet er at vi kan foerst finde ud af sent om vi er en application nu.
// Dvs vi skal loebe alle wirelets igennem foerst for at checke denne.

// Cannot change anything regarding the template
// That is fixed
//public static final class NewApplicationWirelet extends InternalBuildWirelet {
//
//    /** {@inheritDoc} */
//    @Override
//    public void onInstall(PackedContainerBuilder installer) {
//        if (installer.parent == null) {
//            throw new Error("This wirelet cannot be used when creating a new application");
//        }
//        // installer.newApplication = true;
//    }
//}
