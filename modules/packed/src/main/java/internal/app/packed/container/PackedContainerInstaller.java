/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.assembly.Assembly;
import app.packed.assembly.DelegatingAssembly;
import app.packed.binding.Key;
import app.packed.build.hook.BuildHook;
import app.packed.container.ContainerBuildLocal;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerInstaller;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.component.AbstractComponentInstaller;
import internal.app.packed.container.wirelets.CompositeWirelet;
import internal.app.packed.container.wirelets.InternalBaseWirelet;
import internal.app.packed.util.accesshelper.AssemblyAccessHandler;

/** Implementation of {@link ContainerTemplate.Installer} */
public final class PackedContainerInstaller<H extends ContainerHandle<?>> extends AbstractComponentInstaller<ContainerSetup, PackedContainerInstaller<H>>
        implements ContainerInstaller<H> {

    /** Non-null if this container is being installed as the root container of an application. */
    @Nullable
    public final PackedApplicationInstaller<?> applicationInstaller;

    /** Delegating assemblies. Empty unless any {@link DelegatingAssembly} has been used. */
    public final ArrayList<Class<? extends DelegatingAssembly>> delegatingAssemblies = new ArrayList<>();

    public final ArrayList<BuildHook> hooksFromWirelets = new ArrayList<>();

    /** The extension that is installing the container. */
    final Class<? extends Extension<?>> installedBy;

    public boolean isFromAssembly;

    /** The name of the container. */
    String name;

    @Nullable
    String nameFromWirelet;

    /** The parent of the new container. Or <code>null</code> if a root container. */
    @Nullable
    public final ContainerSetup parent;

    /** The template for the new container. */
    public final PackedContainerTemplate<H> template;

    /** A list of wirelets that have not been consumed yet. */
    public final ArrayList<Wirelet> unconsumedWirelets = new ArrayList<>();

    // Cannot take ExtensionSetup, as BaseExtension is not instantiated for a root container
    public PackedContainerInstaller(PackedContainerTemplate<H> template, @Nullable PackedApplicationInstaller<?> application, @Nullable ContainerSetup parent,
            Class<? extends Extension<?>> installedBy) {
        super(Set.of(), new HashMap<>());
        this.applicationInstaller = application;
        this.template = requireNonNull(template, "template is null");
        this.parent = parent;
        this.installedBy = installedBy;
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationSetup application(ContainerSetup setup) {
        return setup.application;
    }

    @Override
    public <T> ContainerInstaller<H> provideGuestConstant(Key<T> key, T constant) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public H install(Assembly assembly, Wirelet... wirelets) {
        checkNotUsed();
        // TODO can install container (assembly.isConfigurable());
        this.isFromAssembly = true;
        processWirelets(wirelets);
        ContainerSetup container = invokeAssemblyBuild(assembly);
        return (H) container.handle();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public H install(Wirelet... wirelets) {
        checkNotUsed();
        // TODO can install container
        processWirelets(wirelets);
        ContainerSetup container = ContainerSetup.newContainer(this, parent.application, parent.assembly);
        return (H) container.handle();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public H installAndUseThisExtension(Wirelet... wirelets) {
        ContainerHandle<?> handle = install(wirelets);
        ContainerSetup.crack(handle).useExtension(installedBy, null);
        return (H) handle;
    }

    /**
     * Invokes {@link Assembly#build(PackedContainerInstaller)}.
     *
     * @param assembly
     *            the assembly to invoke build on
     * @return the new container that was created
     */
    public ContainerSetup invokeAssemblyBuild(Assembly assembly) {
        requireNonNull(assembly, "assembly is null");
        AssemblySetup s = AssemblyAccessHandler.instance().invokeAssemblyBuild(assembly, applicationInstaller, this);
        return s.container;
    }

    public <T> ContainerInstaller<H> localConsume(ContainerBuildLocal<T> local, Consumer<T> action) {
//        PackedAbstractContainerLocal<?> cl = (PackedAbstractContainerLocal<?>) local;

//        cl.g
//        action.accept((T) cl.get(this));
//        return this;
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ContainerInstaller<H> named(String name) {
        this.name = name;
        return this;
    }

    /**
     * Processes all wirelets that were specified when building the container.
     *
     * @param wirelets
     *            the wirelets to process
     */
    public void processWirelets(Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        for (Wirelet wirelet : wirelets) {
            requireNonNull(wirelet, "wirelet is null");
            switch (wirelet) {
            case CompositeWirelet w -> processWirelets(w.wirelets);
            case InternalBaseWirelet w -> w.onBuild(this);
            default -> unconsumedWirelets.add(wirelet);
            }
        }
    }

    public static PackedContainerInstaller<?> of(PackedContainerTemplate<?> template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        PackedContainerInstaller<?> pcb = new PackedContainerInstaller<>(template, null, parent, installedBy);

//        for (PackedContainerLink b : pcb.template.links().packs) {
//            if (b.onUse() != null) {
//                b.onUse().accept(pcb);
//            }
//        }
        return pcb;
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedContainerInstaller<H> setLocal(ContainerBuildLocal<T> local, T value) {
        return super.setLocal(local, value);
    }
}
