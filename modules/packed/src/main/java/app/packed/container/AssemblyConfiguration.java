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
package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.util.TreeView;

/**
 *
 */
// Is not a component... Syntes stadig maaske, IDK???
public class AssemblyConfiguration {

    /**
     * A tree with all the containers defined by the assembly.
     *
     * @return
     */
    public TreeView<ContainerConfiguration> containers() {
        throw new UnsupportedOperationException();
    }

    /** {@return an assembly finder that can be used to find assemblies on the class- or module-path.} */
    // Classpath if the assembly is on the classpath, otherwise modulepath

    // Maybe this is on the container level???? And not Assembly Level
    protected final AssemblyFinder assemblyFinder() {
        throw new UnsupportedOperationException();
    }

    public final void lookup(Lookup lookup) {
        requireNonNull(lookup, "lookup cannot be null");
    }

    // The delegate is always provided by others
    public void delegate(AssemblyDelegate delegate, AssemblyDelegate.Option... options) {

       // external methods that needs parameters should always return an assembly delegate,
    }
}
