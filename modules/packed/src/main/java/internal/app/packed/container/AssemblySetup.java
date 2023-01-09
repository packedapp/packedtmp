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
import java.util.TreeSet;

import app.packed.application.BuildException;
import app.packed.application.BuildGoal;
import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.BuildableAssembly;
import app.packed.container.DelegatingAssembly;
import app.packed.container.Realm;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.framework.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.PackedApplicationDriver;
import internal.app.packed.jfr.BuildApplicationEvent;
import internal.app.packed.service.CircularServiceDependencyChecker;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The internal configuration of an assembly. */
public final class AssemblySetup extends RealmSetup {

    /** A handle that can invoke {@link BuildableAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BuildableAssembly.class, "doBuild",
            void.class, AssemblyModel.class, ContainerSetup.class);

    /** A MethodHandle for invoking {@link AssemblyMirror#initialize(AssemblySetup)}. */
    private static final MethodHandle MH_ASSEMBLY_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), AssemblyMirror.class,
            "initialize", void.class, AssemblySetup.class);

    /** A handle that can invoke {@link ComposerAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_COMPOSER_ASSEMBLY_DO_BUILD = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComposerAssembly.class, "doBuild",
            void.class, AssemblyModel.class, ContainerSetup.class);

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_DELEGATING_ASSEMBLY_DELEGATE_TO = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), DelegatingAssembly.class,
            "delegateTo", Assembly.class);

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_APPLICATION_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onApplicationClose", void.class);

    /** A handle for invoking the protected method {@link Extension#onAssemblyClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_ASSEMBLY_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onAssemblyClose", void.class);

    /** The application that is being the assembly is used to built. */
    public final ApplicationSetup application;

    /** The assembly instance. */
    public final Assembly assembly;

    /** A model of the assembly. */
    public final AssemblyModel assemblyModel;

    /** The container that the assembly defines. */
    public final ContainerSetup container;

    /** Any delegating assemblies this assembly was wrapped in. */
    public final List<Class<? extends DelegatingAssembly>> delegatingAssemblies;

    /** All extensions that are used in the assembly ordered accordingly to their natural order. */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>();

    /** Whether or not assembly is open for configuration. */
    private boolean isClosed;

    /**
     * This constructor is used for an assembly that defines an application.
     * 
     * @param driver
     *            the application driver
     * @param goal
     *            the build goal
     * @param linkTo
     *            the container that is being linked to
     * @param assembly
     *            the application's assembly
     * @param wirelets
     *            optional wirelets
     */
    public AssemblySetup(@Nullable PackedApplicationDriver<?> applicationDriver, @Nullable BuildGoal goal, @Nullable ContainerSetup linkTo, Assembly assembly,
            Wirelet[] wirelets) {
        // We need to unpack any delegating assemblies
        Assembly a = requireNonNull(assembly, "assembly is null");
        if (a instanceof DelegatingAssembly) {
            int attempts = 100;
            ArrayList<Class<? extends DelegatingAssembly>> delegatingAssemblies = new ArrayList<>(1);
            AssemblyModel model = null;
            while (a instanceof DelegatingAssembly da) {
                if (attempts-- == 0) {
                    throw new BuildException("Inifite loop suspected, cannot have more than 100 delegating assemblies");
                }
                try {
                    a = (Assembly) MH_DELEGATING_ASSEMBLY_DELEGATE_TO.invokeExact(da);
                } catch (Throwable e) {
                    throw ThrowableUtil.orUndeclared(e);
                }
                if (a == null) {
                    throw new BuildException(
                            "Delagating assembly: " + da.getClass() + " cannot return from " + DelegatingAssembly.class.getSimpleName() + "::delegateTo");
                }
                model = AssemblyModel.of(a.getClass());
                delegatingAssemblies.add(da.getClass());
            }
            this.delegatingAssemblies = List.copyOf(delegatingAssemblies);
            this.assemblyModel = model;
        } else {
            this.delegatingAssemblies = List.of();
            this.assemblyModel = AssemblyModel.of(a.getClass());
        }

        this.assembly = a;

        // Set rest of fields depending on weather it is the root assembly of an application, or we are linking
        if (linkTo == null) {
            this.application = new ApplicationSetup(applicationDriver, goal, this, wirelets);
            this.container = application.container;
        } else {
            if (a instanceof ComposerAssembly) {
                throw new IllegalArgumentException("Cannot link an instance of " + ComposerAssembly.class + ", assembly must extend "
                        + BuildableAssembly.class.getSimpleName() + " instead");
            }
            this.application = linkTo.application;
            this.container = new ContainerSetup(application, this, linkTo, wirelets);
        }
    }

    public void build() {
        boolean isRootContainer = container.treeParent == null;

        BuildApplicationEvent abe = null;

        if (isRootContainer) {
            abe = new BuildApplicationEvent();
            abe.assemblyClass = assembly.getClass();
            abe.begin();
        }

        // We need to call two different, depending on on the type of the assembly
        if (assembly instanceof BuildableAssembly ca) {
            // Invoke Assembly::doBuild, which in turn will invoke Assembly::build
            try {
                MH_ASSEMBLY_DO_BUILD.invokeExact(ca, assemblyModel, container);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        } else {
            // Invoke ComposerAssembly::doBuild
            ComposerAssembly<?> cas = (ComposerAssembly<?>) assembly;
            try {
                MH_COMPOSER_ASSEMBLY_DO_BUILD.invokeExact(cas, assemblyModel, container);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        isClosed = true;

        // call Extension.onUserClose on the root container in the assembly.
        // This is turn calls recursively down Extension.onUserClose on all
        // ancestor extensions in the same realm.

        // We use .pollFirst because extensions might add new extensions while being closed
        // In which case an Iterator might throw ConcurrentModificationException

        // Test and see if we are closing the root container of the application

        // Problemet er jo vi kan tilfoeje nye extensions mens vi lukker ned

        // ExtensionSetup[] exts = container.extensions.values().toArray(new ExtensionSetup[container.extensions.size()]);
        // Arrays.sort(exts);

        if (isRootContainer) {
            // Root container
            // We must also close all extension trees.
            ArrayList<ExtensionSetup> list = new ArrayList<>(extensions.size());

            ExtensionSetup e = extensions.pollFirst();
            while (e != null) {
                list.add(e);
                onAssemblyClose(e.instance());
                e = extensions.pollFirst();
            }

            // Hmm what about circular dependencies for extensions?
            CircularServiceDependencyChecker.dependencyCyclesFind(container);

            // Close every extension tree
            for (ExtensionSetup extension : list) {
                try {
                    MH_EXTENSION_ON_APPLICATION_CLOSE.invokeExact(extension.instance());
                } catch (Throwable t) {
                    throw ThrowableUtil.orUndeclared(t);
                }

                extension.extensionTree.close();
            }

            // The application has been built successfully.
            // If we need to launch it, generate code for it
            application.finish();
            abe.applicationName = container.name;
            abe.commit();
        } else {
            // Similar to above, except we do not close extension trees
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
        AssemblyMirror mirror = ClassUtil.mirrorHelper(AssemblyMirror.class, AssemblyMirror::new, null);

        // Initialize AssemblyMirror by calling AssemblyMirror#initialize(AssemblySetup)
        try {
            MH_ASSEMBLY_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
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
    public Realm realm() {
        return Realm.application();
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> realmType() {
        return assembly.getClass();
    }
}
