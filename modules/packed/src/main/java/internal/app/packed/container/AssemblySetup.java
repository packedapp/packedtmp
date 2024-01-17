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

import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;

import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyMirror;
import app.packed.assembly.DelegatingAssembly;
import app.packed.component.ComponentOperator;
import app.packed.util.Nullable;
import internal.app.packed.component.AbstractTreeMirror;
import internal.app.packed.component.Mirrorable;
import internal.app.packed.component.PackedLocalKeyAndSource;
import internal.app.packed.component.PackedLocalMap;
import internal.app.packed.service.CircularServiceDependencyChecker;
import internal.app.packed.util.MagicInitializer;
import internal.app.packed.util.TreeNode;
import internal.app.packed.util.TreeNode.ActualNode;
import internal.app.packed.util.types.ClassUtil;

/** The internal configuration of an assembly. */
public final class AssemblySetup implements PackedLocalKeyAndSource, ActualNode<AssemblySetup> , AuthorSetup , Mirrorable<AssemblyMirror> {

    /** A magic initializer for {@link BeanMirror}. */
    public static final MagicInitializer<AssemblySetup> MIRROR_INITIALIZER = MagicInitializer.of(AssemblyMirror.class);

    /** The assembly instance. */
    public final Assembly assembly;

    /** The time when the assembly build finished if successful. */
    public long assemblyBuildFinishedTime;

    /** The time when the assembly build started. */
    public final long assemblyBuildStartedTime = System.nanoTime();

    /** The (root) container the assembly defines. */
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
    /// Hmm applications er vist separate assembly saa
    final TreeSet<ExtensionSetup> extensions = new TreeSet<>();

    /** Whether or not this assembly is available for configuration. */
    private boolean isConfigurable = true;

    /** A model of the assembly. */
    public final AssemblyModel model;

    public final TreeNode<AssemblySetup> node;

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
        this.node = new TreeNode<>(builder.parent == null ? null : builder.parent.assembly, this);
        this.assembly = assembly;
        this.model = AssemblyModel.of(assembly.getClass());
        this.delegatingAssemblies = builder.delegatingAssemblies == null ? List.of() : List.copyOf(builder.delegatingAssemblies);
        this.container = builder.newContainer(this);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentOperator author() {
        return ComponentOperator.application();
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
    @Override
    public AssemblyMirror mirror() {
        return MIRROR_INITIALIZER.run(() -> ClassUtil.newMirror(AssemblyMirror.class, AssemblyMirror::new, null), this);
    }

    /** Does post processing after we have called into the assembly to build. */
    public void postBuild() {
        // Cleanup after the assembly has been build successfully.

        // We have two paths depending on weather or not the container is the root in an application
        if (container.isApplicationRoot()) {
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

            assemblyBuildFinishedTime = System.nanoTime();
            // The application has been built successfully, generate code if needed
            container.application.close();

        } else {
            // Similar to above, except we do not call Extension#onApplicationClose
            for (ExtensionSetup e = extensions.pollLast(); e != null; e = extensions.pollLast()) {
                e.closeAssembly();
            }
            isConfigurable = false;
            assemblyBuildFinishedTime = System.nanoTime();
        }
    }

    public Assembly.State state() {
        throw new UnsupportedOperationException();
    }

    public static final class PackedAssemblyTreeMirror extends AbstractTreeMirror<AssemblyMirror, AssemblySetup> implements AssemblyMirror.OfTree {

        public PackedAssemblyTreeMirror(AssemblySetup root, @Nullable Predicate<? super AssemblySetup> filter) {
            super(root, filter);
        }

        /** {@inheritDoc} */
        @Override
        public void print() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public void printWithDuration() {
            throw new UnsupportedOperationException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public TreeNode<AssemblySetup> node() {
        return node;
    }

    /** {@inheritDoc} */
    @Override
    public PackedLocalMap locals() {
        return container.locals();
    }
}
