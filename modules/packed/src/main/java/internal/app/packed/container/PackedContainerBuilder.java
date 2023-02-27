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
import app.packed.extension.container.ContainerBuilder;
import app.packed.extension.container.ContainerTemplate;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.Nullable;
import internal.app.packed.lifetime.PackedExtensionLink;
import internal.app.packed.lifetime.runtime.ApplicationLaunchContext;

/** Implementation of {@link ContainerBuilder}. */
public final class PackedContainerBuilder extends AbstractContainerBuilder implements ContainerBuilder {

    /** The extension that is installing the container. */
    final Class<? extends Extension<?>> installedBy;

    boolean isUsed;

    boolean newApplication;

    // Cannot take ExtensionSetup, as BaseExtension is not instantiated for a root container
    private PackedContainerBuilder(ContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        super(template);
        this.application = requireNonNull(application);
        this.parent = parent;
        this.installedBy = requireNonNull(installedBy);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle build(Assembly assembly, Wirelet... wirelets) {
        parent.assembly.checkIsConfigurable();
        checkNotUsed();

        // Create a new assembly, which call into #containerInstall
        processWirelets(wirelets);
        AssemblySetup as = new AssemblySetup(this, assembly);

        // Build the assembly
        as.build();

        return new PackedContainerHandle(as.container);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle build(Wirelet... wirelets) {
        checkNotUsed();
        parent.assembly.checkIsConfigurable();

        processWirelets(wirelets);
        ContainerSetup container = newContainer(parent.assembly);
        return new PackedContainerHandle(container);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerHandle buildAndUseThisExtension(Wirelet... wirelets) {
        PackedContainerHandle h = build(wirelets);
        h.container().useExtension(installedBy, null);
        return h;
    }

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
    public void specializeMirror(Supplier<? extends ContainerMirror> supplier) {
        throw new UnsupportedOperationException();
    }

    public static PackedContainerBuilder of(ContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        PackedContainerBuilder pcb = new PackedContainerBuilder(template, installedBy, application, parent);

        for (PackedExtensionLink b : pcb.template.links()) {
            b.use(pcb);
        }
        return pcb;
    }

    public static final class NewApplicationWirelet extends InternalWirelet {

        /** {@inheritDoc} */
        @Override
        public void onInstall(AbstractContainerBuilder installer) {
            if (installer.parent == null) {
                throw new Error("This wirelet cannot be used when creating a new application");
            }
         //   installer.newApplication = true;
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
        public void onInstall(AbstractContainerBuilder installer) {
            installer.nameFromWirelet = name;// has already been validated
        }
    }
}
