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
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.BuildTaskGoal;
import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHook;
import app.packed.container.ContainerMirror;
import app.packed.container.UserOrExtension;
import app.packed.container.Wirelet;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationDriver;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public final class AssemblySetup extends RealmSetup {

    /** A handle that can invoke {@link Assembly#doBuild()}. */
    private static final MethodHandle MH_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Assembly.class, "doBuild", void.class,
            AssemblySetup.class, ContainerConfiguration.class);

    /** The application that is being built. */
    public final ApplicationSetup application;

    /** The assembly used to create this installer. */
    public final Assembly assembly;

    /** A model of the assembly. */
    public final AssemblyModel assemblyModel;

    /** The container defined by this assembly. */
    public final ContainerSetup container;

    private final PackedContainerHandle containerHandle;

    /**
     * All extensions that are used in the installer (if non embedded) An order set of extension according to the natural
     * extension dependency order.
     */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>((c1, c2) -> -c1.model.compareTo(c2.model));

    /**
     * Builds an application using the specified assembly and optional wirelets.
     * 
     * @param goal
     *            the build target
     * @param assembly
     *            the assembly of the application
     * @param wirelets
     *            optional wirelets
     * @return the application
     */
    public AssemblySetup(PackedApplicationDriver<?> applicationDriver, BuildTaskGoal goal, Assembly assembly, Wirelet[] wirelets) {
        this.assembly = requireNonNull(assembly, "assembly is null");
        this.assemblyModel = AssemblyModel.of(assembly.getClass());
        this.application = new ApplicationSetup(applicationDriver, goal, this, wirelets);

        this.container = application.container;
        this.containerHandle = new PackedContainerHandle(container);
    }

    public AssemblySetup(PackedContainerHandle handle, ContainerSetup linkTo, Assembly assembly, Wirelet[] wirelets) {
        this.assembly = requireNonNull(assembly, "assembly is null");
        this.assemblyModel = AssemblyModel.of(assembly.getClass());
        this.application = linkTo.application;
        if (assembly instanceof ComposerAssembly) {
            throw new IllegalArgumentException("Cannot specify an instance of " + ComposerAssembly.class);
        }
        this.container = new ContainerSetup(application, this, handle, linkTo, wirelets);
        this.containerHandle = handle;
    }

    public void build() {
        ContainerConfiguration configuration = containerHandle.toConfiguration(container);

        // Invoke Assembly::doBuild, which in turn will invoke Assembly::build
        try {
            MH_ASSEMBLY_DO_BUILD.invokeExact(assembly, this, configuration);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        // Close the realm, if the application has been built successfully (no exception was thrown)
        close();
    }

    void close() {
        super.close();

        // call Extension.onUserClose on the root container in the assembly.
        // This is turn calls recursively down Extension.onUserClose on all
        // ancestor extensions in the same realm.

        // We use .pollFirst because extensions might add new extensions while being closed
        // In which case an Iterator might throw ConcurrentModificationException

        // Test and see if we are closing the root container

        if (container.parent == null) {
            // Root container
            // We must also close all extensions application-wide.
            ArrayList<ExtensionRealmSetup> list = new ArrayList<>(extensions.size());

            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                list.add(e.extensionRealm);
                e.onUserClose();
                e = extensions.pollFirst();
            }

            container.application.injectionManager.finish(container.lifetime.pool, container);

            // Close all extensions application wide
            for (ExtensionRealmSetup extension : list) {
                extension.close();
            }

            // The application has been built successfully
            application.close();

        } else {
            // Similar to above, except we do not close extensions application-wide
            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                e.onUserClose();
                e = extensions.pollFirst();
            }
        }
    }

    /** {@return a mirror for this assembly.} */
    public AssemblyMirror mirror() {
        return new BuildtimeAssemblyMirror(this);
    }

    /** {@inheritDoc} */
    @Override
    public UserOrExtension realm() {
        return UserOrExtension.application();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> realmType() {
        if (assembly instanceof ComposerAssembly) {
            // TODO extract realm
            return assembly.getClass();
        } else {
            return assembly.getClass();
        }
    }

    /** Implementation of {@link AssemblyMirror}. */
    public record BuildtimeAssemblyMirror(AssemblySetup assembly) implements AssemblyMirror {

        /** {@inheritDoc} */
        @Override
        public List<Class<? extends ContainerHook>> containerHooks() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public ContainerMirror container() {
            return assembly.container.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Class<? extends Assembly> assemblyClass() {
            return assembly.assembly.getClass();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<AssemblyMirror> parent() {
            ContainerSetup org = assembly.container;
            for (ContainerSetup p = org.parent; p != null; p = p.parent) {
                if (org.assembly != p.assembly) {
                    return Optional.of(p.assembly.mirror());
                }
            }
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<AssemblyMirror> children() {
            return children(assembly, assembly.container, new ArrayList<>()).stream();
        }

        private ArrayList<AssemblyMirror> children(AssemblySetup assembly, ContainerSetup cs, ArrayList<AssemblyMirror> list) {
            if (assembly == cs.assembly) {
                for (ContainerSetup c : cs.containerChildren) {
                    children(assembly, c, list);
                }
            } else {
                list.add(cs.assembly.mirror());
            }
            return list;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isRoot() {
            return assembly.container.parent == null;
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror application() {
            return assembly.application.mirror();
        }
    }
}
