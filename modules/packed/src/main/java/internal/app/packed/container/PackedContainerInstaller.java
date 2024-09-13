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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.assembly.Assembly;
import app.packed.assembly.DelegatingAssembly;
import app.packed.build.hook.BuildHook;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerLocal;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationInstaller;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import sandbox.extension.container.ContainerTemplate;

/** Implementation of {@link ContainerTemplate.Installer} */
public final class PackedContainerInstaller implements ContainerTemplate.Installer {

    /** A handle that can invoke {@link BuildableAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_ASSEMBLY_BUILD = LookupUtil.findVirtual(MethodHandles.lookup(), Assembly.class, "build", AssemblySetup.class,
            PackedContainerInstaller.class);

    /** Non-null if this container is being installed as the root container of an application. */
    @Nullable
    public final PackedApplicationInstaller applicationInstaller;

    /** The container we are creating */
    ContainerSetup container;

    /** Delegating assemblies. Empty unless any {@link DelegatingAssembly} has been used. */
    public final ArrayList<Class<? extends DelegatingAssembly>> delegatingAssemblies = new ArrayList<>();

    public final ArrayList<BuildHook> hooksFromWirelets = new ArrayList<>();

    /** The extension that is installing the container. */
    final Class<? extends Extension<?>> installedBy;

    /** Container locals that the container is initialized with. */
    final IdentityHashMap<PackedContainerLocal<?>, Object> locals = new IdentityHashMap<>();

    /** A supplier for creating container mirrors. */
    protected Function<? super ContainerHandle<?>, ? extends ContainerMirror> mirrorSupplier;

    /** The name of the container. */
    String name;

    @Nullable
    public String nameFromWirelet;

    /** The parent of the new container. Or <code>null</code> if a root container. */
    @Nullable
    ContainerSetup parent;

    /** The template for the new container. */
    public final PackedContainerTemplate template;

    /** A list of wirelets that have not been consumed yet. */
    public final ArrayList<Wirelet> unconsumedWirelets = new ArrayList<>();

    // Cannot take ExtensionSetup, as BaseExtension is not instantiated for a root container
    public PackedContainerInstaller(PackedContainerTemplate template, @Nullable PackedApplicationInstaller application, @Nullable ContainerSetup parent,
            Class<? extends Extension<?>> installedBy) {
        this.applicationInstaller = application;
        this.template = requireNonNull(template, "template is null");
        this.parent = parent;
        this.installedBy = installedBy;
    }

    @Override
    public <T> ContainerTemplate.Installer carrierProvideConstant(Key<T> key, T constant) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @throws IllegalStateException
     *             if the container is no longer configurable
     */
    public void checkNotInstalledYet() {
        if (!parent.assembly.isConfigurable()) {
            throw new IllegalStateException("This assembly is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T extends ContainerConfiguration> ContainerHandle<?> install(Assembly assembly, Function<? super ContainerHandle<?>, T> configurationCreator,
            Wirelet... wirelets) {
        checkNotInstalledYet();
        // TODO can install container (assembly.isConfigurable());
        processBuildWirelets(wirelets);
        ContainerSetup container = invokeAssemblyBuild(assembly);
        return new PackedContainerHandle<>(container);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends ContainerConfiguration> ContainerHandle<T> install(Function<? super ContainerHandle<?>, T> configurationCreator, Wirelet... wirelets) {
        checkNotInstalledYet();
        // TODO can install container
        processBuildWirelets(wirelets);
        ContainerSetup container = newContainer(parent.application, parent.assembly, configurationCreator);
        return new PackedContainerHandle<>(container);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends ContainerConfiguration> ContainerHandle<T> installAndUseThisExtension(Function<? super ContainerHandle<?>, T> configurationCreator,
            Wirelet... wirelets) {
        ContainerHandle<T> handle = install(configurationCreator, wirelets);
        ContainerSetup.crack(handle).useExtension(installedBy, null);
        return handle;
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
        AssemblySetup s;
        try {
            // Call package private method Assembly#build(PackedContainerBuilder builder)
            s = (AssemblySetup) MH_ASSEMBLY_BUILD.invokeExact(assembly, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return s.container;
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

    public ContainerSetup newContainer(ApplicationSetup application, AssemblySetup assembly,
            Function<? super ContainerHandle<?>, ? extends ContainerConfiguration> newConfiguration) {
        // Create the new container using this installer
        ContainerSetup container = new ContainerSetup(this, application, assembly);

        // Initializes the name of the container
        String nn = nameFromWirelet;

        // Set the name of the container if it was not set by a wirelet
        if (nn == null) {
            // I think try and move some of this to ComponentNameWirelet
            String n = name;
            if (n == null) {
                // TODO Should only be used on the root container in the assembly
                Class<? extends Assembly> source = assembly.assembly.getClass();
                if (Assembly.class.isAssignableFrom(source)) {
                    String nnn = source.getSimpleName();
                    if (nnn.length() > 8 && nnn.endsWith("Assembly")) {
                        nnn = nnn.substring(0, nnn.length() - 8);
                    }
                    if (nnn.length() > 0) {
                        // checkName, if not just App
                        // TODO need prefix
                        n = nnn;
                    }
                    if (nnn.length() == 0) {
                        n = "Assembly";
                    }
                } else {
                    n = "Unknown";
                }
            }
            nn = n;
        }

        String n = nn;
        if (parent != null) {
            HashMap<String, ContainerSetup> c = parent.node.children;
            if (c.size() == 0) {
                c.put(n, container);
            } else {
                int counter = 1;
                while (c.putIfAbsent(n, container) != null) {
                    n = n + counter++; // maybe store some kind of map<ComponentSetup, LongCounter> in BuildSetup.. for those that want to test
                                       // adding 1
                    // million of the same component type
                }
            }
        }
        container.node.name = n;

        this.container = container;

        // Create ContainerConfiguration
        container.initConfiguration(newConfiguration);

        // BaseExtension is automatically used by every container
        ExtensionSetup.install(BaseExtension.class, container, null);

        return container;
    }

    /**
     * @return
     */
    public ContainerSetup newHandleFromConfiguration() {
        return requireNonNull(container);
    }

    /**
     * Processes all wirelets that were specified when building the container.
     *
     * @param wirelets
     *            the wirelets to process
     */
    public void processBuildWirelets(Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        for (Wirelet wirelet : wirelets) {
            requireNonNull(wirelet, "wirelet is null");
            switch (wirelet) {
            case CompositeWirelet w -> processBuildWirelets(w.wirelets);
            case InternalBuildWirelet w -> w.onBuild(this);

            // Too map or not to map...

            // A non-build wirelet that will be processed at a later point
            default -> unconsumedWirelets.add(wirelet);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public ContainerTemplate.Installer specializeMirror(Function<? super ContainerHandle<?>, ? extends ContainerMirror> supplier) {
        checkNotInstalledYet();
        this.mirrorSupplier = supplier;
        return this;
    }

    public static PackedContainerInstaller of(PackedContainerTemplate template, Class<? extends Extension<?>> installedBy, ApplicationSetup application,
            @Nullable ContainerSetup parent) {
        PackedContainerInstaller pcb = new PackedContainerInstaller(template, null, parent, installedBy);

        for (PackedContainerTemplatePack b : pcb.template.links().packs) {
            if (b.onUse() != null) {
                b.onUse().accept(pcb);
            }
//            b.build(pcb);
        }
        return pcb;
    }

}
//@Deprecated
//public void processWirelets(Wirelet[] wirelets) {
//  requireNonNull(wirelets, "wirelets is null");
//  // Most of this method is just processing wirelets
//  Wirelet prefix = template.wirelet();
//
//  // We do not current set Container.WW
//  WireletWrapper ww = null;
//
//  if (wirelets.length > 0 || prefix != null) {
//      // If it is the root
//      Wirelet[] ws;
//      if (prefix == null) {
//          ws = CompositeWirelet.flattenAll(wirelets);
//      } else {
//          ws = CompositeWirelet.flatten2(prefix, Wirelet.combine(wirelets));
//      }
//
//      ww = new WireletWrapper(ws);
//
//      // May initialize the component's name, onWire, ect
//      // Do we need to consume internal wirelets???
//      // Maybe that is what they are...
//      int unconsumed = 0;
//      for (Wirelet w : ws) {
//          if (w instanceof InternalWirelet internal) {
//              internal.onInstall(this);
//          } else {
//              unconsumed++;
//          }
//      }
//      if (unconsumed > 0) {
//          ww.unconsumed = unconsumed;
//      }
//  }
////  this.wirelets = wirelets;
//}
// BootstrapAppContainerBuilder (does not take wirelets)

// RootContainerBuilder

// ExtensionContainerBuilder (Implements ContainerBuilder)

// LinkedContainerBuilder
