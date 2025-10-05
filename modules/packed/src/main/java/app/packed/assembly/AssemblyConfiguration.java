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
package app.packed.assembly;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.stream.Stream;

import app.packed.application.ApplicationConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.util.TreeView;
import internal.app.packed.assembly.AssemblySetup;
import internal.app.packed.assembly.PackedAssemblyFinder;
import internal.app.packed.util.PackedTreeView;

/**
 * The configuration of an assembly.
 */
public final class AssemblyConfiguration {

    /**
     * A marker configuration object indicating that an assembly (or composer) has already been used in a build process.
     * Should never be exposed to end-users.
     */
    static final AssemblyConfiguration USED = new AssemblyConfiguration(null);

    /** The internal configuration of the assembly. */
    final AssemblySetup assembly;

    /**
     * Create a new assembly configuration.
     *
     * @param assembly
     *            the internal configuration of the assembly
     */
    AssemblyConfiguration(AssemblySetup assembly) {
        this.assembly = assembly;
    }

    /**
     * {@return an assembly finder that can be used to find assemblies on the class- or module-path.}
     * <p>
     * If this assembly is on the modulepath the assembly finder will search for assemblies on the modulepath.
     * Otherwise the classpath will be searched.
     */
    public AssemblyFinder assemblyFinder() {
        return new PackedAssemblyFinder(getClass(), assembly);
    }

    public ApplicationConfiguration application() {
        return assembly.container.application.handle().configuration();
    }

    /** {@return the current state of the assembly.} */
    protected Assembly.State assemblyState() {
        throw new UnsupportedOperationException();
    }

    // Paa Assembly, ContainerConfiguration, Bean
    /**
     * Returns a stream of the component configurations defined by this
     *
     * @param <T>
     *            type of configurations to return
     * @param configurationType
     *            type of configurations to return
     * @return a stream of the selected configuration types
     *
     * @throws UnsupportedOperationException
     *             if configurations of the specified component type are not supported from this method
     */
    public <T extends ComponentConfiguration> Stream<T> configurations(Class<T> configurationType) {
        throw new UnsupportedOperationException();
    }

    // Maybe just containers().root();
    public ContainerConfiguration containerRoot() {
        return assembly.container.configuration();
    }

    /** {@return a tree view of all the containers defined by the assembly} */
    public TreeView<? extends ContainerConfiguration> containers() {
        return new PackedTreeView<>(assembly.container, c -> c.assembly == assembly, c -> c.configuration());
    }

    // I think maybe allow null to revert back to Assembly Based lookup
    public void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null");
    }
}

//// Assembly mirrors cannot be specialized for now
///**
// * Specializes the {@link AssemblyMirror} that represents this assembly.
// *
// * @param supplier
// *            the mirror supplier
// * @throws IllegalStateException
// *             if called from outside of {@link #build()}
// */
//public void specializeMirror(Supplier<? extends AssemblyMirror> supplier) {
//    requireNonNull(supplier, "supplier cannot be null");
//    throw new UnsupportedOperationException();
//}
//
//// Think just have a containers() method...
//public final void forEach(Consumer<? super ContainerConfiguration> consumer) {
//    forEach(c -> c.use(BaseExtension.class));
//    throw new UnsupportedOperationException();
//}