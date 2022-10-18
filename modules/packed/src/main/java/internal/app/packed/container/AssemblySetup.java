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
import app.packed.application.BuildGoal;
import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerHook;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.UserOrExtension;
import app.packed.container.Wirelet;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationDriver;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * The internal configuration of an assembly
 */
public final class AssemblySetup extends RealmSetup {

    /** A handle that can invoke {@link Assembly#doBuild()}. */
    private static final MethodHandle MH_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Assembly.class, "doBuild", void.class,
            AssemblySetup.class, ContainerSetup.class);

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_ASSEMBLY_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onAssemblyClose", void.class);

    /** The application that is being built. */
    public final ApplicationSetup application;

    /** The assembly used to create this installer. */
    public final Assembly assembly;

    /** A model of the assembly. */
    public final AssemblyModel assemblyModel;

    /** The container defined by this assembly. */
    public final ContainerSetup container;

    /**
     * All extensions that are used in the same assembly (if non embedded) An order set of extension according to the
     * natural extension dependency order.
     */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>((c1, c2) -> -c1.model.compareTo(c2.model));

    /** Whether or not this realm is configurable. */
    private boolean isClosed;

    /**
     * This constructor is used when linking an assembly.
     * 
     * @param linkTo
     *            the container that is being linked to
     * @param assembly
     *            the assembly of the container to link
     * @param wirelets
     *            optional wirelets
     */
    public AssemblySetup(ContainerSetup linkTo, Assembly assembly, Wirelet[] wirelets) {
        this.assembly = requireNonNull(assembly, "assembly is null");
        this.assemblyModel = AssemblyModel.of(assembly.getClass());
        this.application = linkTo.application;
        if (assembly instanceof ComposerAssembly) {
            throw new IllegalArgumentException("Cannot specify an instance of " + ComposerAssembly.class + " when linking");
        }
        this.container = new ContainerSetup(application, this, linkTo, wirelets);
    }

    /**
     * This constructor is used for the root assembly of an application.
     * 
     * @param driver
     *            the application driver
     * @param goal
     *            the build target
     * @param assembly
     *            the assembly of the application
     * @param wirelets
     *            optional wirelets
     */
    public AssemblySetup(PackedApplicationDriver<?> applicationDriver, BuildGoal goal, Assembly assembly, Wirelet[] wirelets) {
        this.assembly = requireNonNull(assembly, "assembly is null");
        this.assemblyModel = AssemblyModel.of(assembly.getClass());
        this.application = new ApplicationSetup(applicationDriver, goal, this, wirelets);

        this.container = application.container;
    }

    public void build() {
        // Invoke Assembly::doBuild, which in turn will invoke Assembly::build
        try {
            MH_ASSEMBLY_DO_BUILD.invokeExact(assembly, this, container);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        isClosed = true;

        boolean isRoot = container.treeParent == null;
        // call Extension.onUserClose on the root container in the assembly.
        // This is turn calls recursively down Extension.onUserClose on all
        // ancestor extensions in the same realm.

        // We use .pollFirst because extensions might add new extensions while being closed
        // In which case an Iterator might throw ConcurrentModificationException

        // Test and see if we are closing the root container of the application
        if (isRoot) {
            // Root container
            // We must also close all extensions application-wide.
            ArrayList<ExtensionTreeSetup> list = new ArrayList<>(extensions.size());

            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                list.add(e.extensionRealm);
                onAssemblyClose(e.instance());
                e = extensions.pollFirst();
            }

            container.application.injectionManager.finish(container.lifetime.pool, container);

            // Close all extensions application wide
            for (ExtensionTreeSetup extension : list) {
                extension.close();
            }

            // The application has been built successfully
            application.close();

        } else {
            // Similar to above, except we do not close extensions application-wide
            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                onAssemblyClose(e.instance());
                e = extensions.pollFirst();
            }
        }

    }

    public boolean isClosed() {
        return isClosed;
    }

    /** {@return a mirror for this assembly.} */
    public AssemblyMirror mirror() {
        return new BuildtimeAssemblyMirror(this);
    }

    private void onAssemblyClose(Extension<?> instance) {
        try {
            MH_EXTENSION_ON_ASSEMBLY_CLOSE.invokeExact(instance);
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }
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
            return List.of(); // TODO implement
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
            for (ContainerSetup p = org.treeParent; p != null; p = p.treeParent) {
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
                for (var e = cs.treeFirstChild; e != null; e = e.treeNextSiebling) {
                    children(assembly, e, list);
                }
            } else {
                list.add(cs.assembly.mirror());
            }
            return list;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isRoot() {
            return assembly.container.treeParent == null;
        }

        /** {@inheritDoc} */
        @Override
        public ApplicationMirror application() {
            return assembly.application.mirror();
        }
    }
}
