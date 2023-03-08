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

import app.packed.application.BuildGoal;
import app.packed.container.Assembly;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.ContainerLocal;
import app.packed.extension.Extension;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;
import sandbox.extension.container.ContainerBuilder;
import sandbox.extension.container.ContainerTemplate;

/** Implementation of {@link ContainerBuilder} for a leaf container. */
public final class LeafContainerBuilder extends PackedContainerBuilder implements ContainerBuilder {

    /** The extension that is installing the container. */
    final Class<? extends Extension<?>> installedBy;

    boolean isUsed;

    boolean newApplication;

    // Cannot take ExtensionSetup, as BaseExtension is not instantiated for a root container
    private LeafContainerBuilder(PackedContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        super(template);
        this.parent = parent;
        this.installedBy = requireNonNull(installedBy);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle build(Assembly assembly, Wirelet... wirelets) {
        checkNotUsed();
        checkIsConfigurable();

        ContainerSetup container = buildFromAssembly(assembly, wirelets);
        return new PackedContainerHandle(container);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle build(Wirelet... wirelets) {
        checkNotUsed();
        checkIsConfigurable();

        // Be careful if moving into newContainer, some tests
        // can fail easily
        processWirelets(wirelets);

        ContainerSetup container = newContainer(parent.application, parent.assembly);
        return new PackedContainerHandle(container);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle buildAndUseThisExtension(Wirelet... wirelets) {
        PackedContainerHandle handle = build(wirelets);
        handle.container().useExtension(installedBy, null);
        return handle;
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

    @SuppressWarnings("unchecked")
    public <T> ContainerBuilder consumeLocal(ContainerLocal<T> local, Consumer<T> action) {
        PackedContainerLocal<?> pcl = (PackedContainerLocal<?>) local;
        action.accept((T) pcl.get(this));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BuildGoal goal() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <T> ContainerBuilder lifetimeHolderProvideConstant(Key<T> key, T constant) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public LifetimeKind lifetimeKind() {
        return LifetimeKind.MANAGED;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerBuilder named(String name) {
        this.name = name;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> ContainerBuilder setLocal(ContainerLocal<T> local, T value) {
        locals.put((PackedContainerLocal<?>) local, value);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ContainerBuilder specializeMirror(Supplier<? extends ContainerMirror> supplier) {
        throw new UnsupportedOperationException();
    }

    public static LeafContainerBuilder of(ContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        LeafContainerBuilder pcb = new LeafContainerBuilder((PackedContainerTemplate) template, installedBy, application, parent);

        for (PackedContainerLifetimeTunnel b : pcb.template.links().tunnels) {
            b.build(pcb);
        }
        return pcb;
    }

    public static final class NewApplicationWirelet extends InternalWirelet {

        /** {@inheritDoc} */
        @Override
        public void onInstall(PackedContainerBuilder installer) {
            if (installer.parent == null) {
                throw new Error("This wirelet cannot be used when creating a new application");
            }
            // installer.newApplication = true;
        }

    }

    /** A wirelet that will set the name of the component. Used by {@link Wirelet#named(String)}. */
    public static final class OverrideNameWirelet extends InternalWirelet {

        /** The (validated) name to override with. */
        private final String name;

        /**
         * Creates a new name wirelet
         *
         * @param name
         *            the name to override any existing container name with
         */
        public OverrideNameWirelet(String name) {
            this.name = NameCheck.checkComponentName(name); // throws IAE
        }

        /** {@inheritDoc} */
        @Override
        public void onImageInstantiation(ContainerSetup c, ApplicationLaunchContext ic) {
            ic.name = name;
        }

        /** {@inheritDoc} */
        @Override
        public void onInstall(PackedContainerBuilder installer) {
            installer.nameFromWirelet = name;// has already been validated
        }
    }
}