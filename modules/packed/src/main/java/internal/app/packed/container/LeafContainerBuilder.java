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
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.assembly.Assembly;
import app.packed.build.BuildGoal;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLocal;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.lifetime.LifecycleKind;
import app.packed.util.Key;
import app.packed.util.Nullable;
import sandbox.extension.container.ContainerHandle;
import sandbox.extension.container.ContainerTemplate;

// Would love
/** Implementation of {@link ContainerBuilder} for a leaf container. */
public final class LeafContainerBuilder extends NonBootstrapContainerBuilder {

    /** The extension that is installing the container. */
    final Class<? extends Extension<?>> installedBy;

    // boolean isUsed;

    /** Whether we are creating a new application */
    // boolean newApplication;

    // Cannot take ExtensionSetup, as BaseExtension is not instantiated for a root container
    private LeafContainerBuilder(PackedContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        super(template);
        this.parent = parent;
        this.installedBy = requireNonNull(installedBy);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends ContainerConfiguration> ContainerHandle<?> build(Assembly assembly, Function<? super ContainerTemplate.Installer, T> configurationCreator,
            Wirelet... wirelets) {
        checkNotUsed();
        checkIsConfigurable();

        processBuildWirelets(wirelets);

        ContainerSetup container = buildNow(assembly);
        return new PackedContainerHandle<>(container);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends ContainerConfiguration> ContainerHandle<?> install(Function<? super ContainerTemplate.Installer, T> configurationCreator,
            Wirelet... wirelets) {
        checkNotUsed();
        checkIsConfigurable();

        // Be careful if moving into newContainer, some tests
        // can fail easily
        processBuildWirelets(wirelets);

        ContainerSetup container = newContainer(this, parent.application, parent.assembly, configurationCreator);
        return new PackedContainerHandle<>(container);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends ContainerConfiguration> ContainerHandle<?> buildAndUseThisExtension(Function<? super ContainerTemplate.Installer, T> configurationCreator,
            Wirelet... wirelets) {
        ContainerHandle<?> handle = install(configurationCreator, wirelets);
        ContainerSetup.crack(handle).useExtension(installedBy, null);
        return handle;
    }

    /** {@inheritDoc} */
    @Override
    public <T> ContainerTemplate.Installer carrierProvideConstant(Key<T> key, T constant) {
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
    public LifecycleKind lifetimeKind() {
        return LifecycleKind.MANAGED;
    }

    public <T> ContainerTemplate.Installer localConsume(ContainerLocal<T> local, Consumer<T> action) {
//        PackedAbstractContainerLocal<?> cl = (PackedAbstractContainerLocal<?>) local;

//        cl.g
//        action.accept((T) cl.get(this));
//        return this;
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <T> ContainerTemplate.Installer localSet(ContainerLocal<T> local, T value) {
        locals.put((PackedContainerLocal<?>) local, value);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerTemplate.Installer named(String name) {
        this.name = name;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerTemplate.Installer specializeMirror(Supplier<? extends ContainerMirror> supplier) {
        this.containerMirrorSupplier = supplier;
        return this;
    }

    public static LeafContainerBuilder of(ContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        LeafContainerBuilder pcb = new LeafContainerBuilder((PackedContainerTemplate) template, installedBy, application, parent);

        for (PackedContainerTemplatePack b : pcb.template.links().packs) {
            if (b.onUse() != null) {
                b.onUse().accept(pcb);
            }
//            b.build(pcb);
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
