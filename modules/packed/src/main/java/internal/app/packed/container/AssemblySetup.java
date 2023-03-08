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

import app.packed.container.Assembly;
import app.packed.container.AssemblyMirror;
import app.packed.container.DelegatingAssembly;
import app.packed.container.Realm;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanOwner;
import internal.app.packed.service.CircularServiceDependencyChecker;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/** The internal configuration of an assembly. */
public final class AssemblySetup implements BeanOwner {

    /** A MethodHandle for invoking {@link AssemblyMirror#initialize(AssemblySetup)}. */
    private static final MethodHandle MH_ASSEMBLY_MIRROR_INITIALIZE = LookupUtil.findVirtual(MethodHandles.lookup(), AssemblyMirror.class, "initialize",
            void.class, AssemblySetup.class);

    /** The assembly instance. */
    public final Assembly assembly;

    /** The container the assembly defines. */
    public final ContainerSetup container;

    /** A custom lookup object set via {@link #lookup(Lookup)} */
    @Nullable
    public Lookup customLookup;

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
     * Create a new assembly setup.
     *
     * @param builder
     *            the container builder
     * @param assembly
     *            the assembly instance
     */
    public AssemblySetup(PackedContainerBuilder builder, Assembly assembly) {
        assert (!(assembly instanceof DelegatingAssembly));
        this.assembly = assembly;
        this.model = AssemblyModel.of(assembly.getClass());
        this.delegatingAssemblies = builder.delegatingAssemblies == null ? List.of() : List.copyOf(builder.delegatingAssemblies);
        this.container = builder.newContainer(this);
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
        AssemblyMirror mirror = ClassUtil.newMirror(AssemblyMirror.class, AssemblyMirror::new, null);

        // Initialize AssemblyMirror by calling AssemblyMirror#initialize(AssemblySetup)
        try {
            MH_ASSEMBLY_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    /** Does post processing after we have called into the assembly to build. */
    public void postBuild() {
        // Cleanup after the assembly has been build successfully.

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

        } else {
            // Similar to above, except we do not call Extension#onApplicationClose
            for (ExtensionSetup e = extensions.pollLast(); e != null; e = extensions.pollLast()) {
                e.closeAssembly();
            }
            isConfigurable = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Realm realm() {
        return Realm.application();
    }
}
