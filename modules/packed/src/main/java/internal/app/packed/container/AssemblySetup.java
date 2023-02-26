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
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import app.packed.application.BuildException;
import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.BuildableAssembly;
import app.packed.container.DelegatingAssembly;
import app.packed.container.Realm;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanOwner;
import internal.app.packed.jfr.BuildApplicationEvent;
import internal.app.packed.service.CircularServiceDependencyChecker;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/** The internal configuration of an assembly. */
public final class AssemblySetup implements BeanOwner {

    /** A MethodHandle for invoking {@link AssemblyMirror#initialize(AssemblySetup)}. */
    private static final MethodHandle MH_ASSEMBLY_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), AssemblyMirror.class, "initialize",
            void.class, AssemblySetup.class);

    /** A handle that can invoke {@link BuildableAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_BUILDABLE_ASSEMBLY_DO_BUILD = LookupUtil.findVirtual(MethodHandles.lookup(), BuildableAssembly.class, "doBuild",
            void.class, AssemblyModel.class, ContainerSetup.class);

    /** A handle that can invoke {@link ComposerAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_COMPOSER_ASSEMBLY_DO_BUILD = LookupUtil.findVirtual(MethodHandles.lookup(), ComposerAssembly.class, "doBuild",
            void.class, AssemblyModel.class, ContainerSetup.class);

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_DELEGATING_ASSEMBLY_DELEGATE_TO = LookupUtil.findVirtual(MethodHandles.lookup(), DelegatingAssembly.class,
            "delegateTo", Assembly.class);

    /** A custom lookup object set via {@link #lookup(Lookup)} */
    @Nullable
    public Lookup customLookup;

    /** The assembly instance. */
    public final Assembly assembly;

    /** The container the assembly defines. */
    public final ContainerSetup container;

    /** Any delegating assemblies this assembly was wrapped in. */
    public final List<Class<? extends DelegatingAssembly>> delegatingAssemblies;

    /**
     * All extensions that are used in the assembly ordered accordingly to their natural order.
     * <p>
     * We cannot use {@link ContainerSetup#extensions} as we remove every node when calling {@link #build()} in order to
     * allow adding new extensions while closing the assembly.
     */
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>();

    /** Whether or not assembly is open for configuration. */
    private boolean isConfigurable = true;

    /** A model of the assembly. */
    public final AssemblyModel model;

    /**
     * This constructor is used for an assembly that defines an application.
     *
     * @param containerBuilder
     *            the container builder
     * @param assembly
     *            the assembly
     */
    public AssemblySetup(AbstractContainerBuilder containerBuilder, Assembly assembly) {
        // We need to unpack any delegating assemblies
        Assembly a = requireNonNull(assembly, "assembly is null");
        if (a instanceof DelegatingAssembly) {
            int attempts = 100; // Just a ran
            ArrayList<Class<? extends DelegatingAssembly>> delegatingAssemblies = new ArrayList<>(1);
            AssemblyModel model = null;
            while (a instanceof DelegatingAssembly da) {
                if (attempts-- == 0) {
                    throw new BuildException(
                            "Inifite loop suspected, cannot have more than 100 delegating assemblies, assemblyClass = " + da.getClass().getCanonicalName());
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
            this.model = model;
        } else {
            this.delegatingAssemblies = List.of();
            this.model = AssemblyModel.of(a.getClass());
        }

        this.assembly = a;

        if (containerBuilder instanceof PackedContainerBuilder installer) {
            if (a instanceof ComposerAssembly) {
                throw new IllegalArgumentException("Cannot link an instance of " + ComposerAssembly.class + ", assembly must extend "
                        + BuildableAssembly.class.getSimpleName() + " instead");
            }
            this.container = installer.newContainer(this);
        } else {
            ApplicationSetup application = new ApplicationSetup(containerBuilder, this);
            this.container = application.container;
        }
    }

    public void build() {
        // Create a JFR build event if application root
        BuildApplicationEvent buildEvent = null;
        if (container.treeParent == null) {
            buildEvent = new BuildApplicationEvent();
            buildEvent.assemblyClass = assembly.getClass();
            buildEvent.begin();
        }

        // Call into the assembly provided by the user
        // We have two different paths depending on the type of assembly
        if (assembly instanceof BuildableAssembly ba) {
            // Invoke Assembly::doBuild, which in turn will invoke Assembly::build
            try {
                MH_BUILDABLE_ASSEMBLY_DO_BUILD.invokeExact(ba, model, container);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        } else {
            // Invoke ComposerAssembly::doBuild
            ComposerAssembly<?> cas = (ComposerAssembly<?>) assembly;
            try {
                MH_COMPOSER_ASSEMBLY_DO_BUILD.invokeExact(cas, model, container);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        // Cleanup after the assembly.
        // We have two paths depending on weather or not the container is the root in an application
        if (container.treeParent == null) {
            // We maintain an (ordered) list of extensions in the order they where closed.
            // Extensions might install other extensions while closing which is why we keep
            // polling
            ArrayList<ExtensionSetup> list = new ArrayList<>(extensions.size());

            for (ExtensionSetup e = extensions.pollLast(); e != null; e = extensions.pollLast()) {
                list.add(e);
                e.closeAssembly();
            }

            isConfigurable = false;

            // Close extension for the application
            for (ExtensionSetup extension : list) {
                extension.closeApplication();
            }

            // Check application dependency cycles. Or wait???
            CircularServiceDependencyChecker.dependencyCyclesFind(container);

            // The application has been built successfully, generate code if needed
            container.application.close();

            buildEvent.applicationName = container.name;
            buildEvent.commit();
        } else {
            // Similar to above, except we do not call Extension#onApplicationClose
            for (ExtensionSetup e = extensions.pollLast(); e != null; e = extensions.pollLast()) {
                e.closeAssembly();
            }
            isConfigurable = false;
        }
    }

    /**
     *
     * @throws IllegalStateException
     *             if the container is no longer configurable
     */
    public void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("This assembly is no longer configurable");
        }
    }

    /**
     * Returns whether or not the bean is still configurable.
     * <p>
     * If an assembly was used to create the container. The handle is never configurable.
     *
     * @return {@code true} if the bean is still configurable
     */
    @Override
    public boolean isConfigurable() {
        return isConfigurable;
    }

    /**
     * @param lookup
     *            the lookup to use
     * @see Assembly#lookup(Lookup)
     * @see AbstractComposer#lookup(Lookup)
     */
    public void lookup(Lookup lookup) {
        this.customLookup = requireNonNull(lookup, "lookup is null");
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

    /** {@inheritDoc} */
    @Override
    public Realm realm() {
        return Realm.application();
    }
}
