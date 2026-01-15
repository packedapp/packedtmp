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
package sandbox.app.packed.container;

import app.packed.assembly.AssemblyDescriptor;

/**
 * Decided if some behaviour should be propagated between assemblies.
 * <p>
 * Typically you need to have an open relationship to be able to propagate mutating behaviour.
 */
// Is Propagation common outside of just assemblies
public interface ContainerPropagator {
    ContainerPropagator ALL = new All();
    ContainerPropagator LOCAL = new Local();

    /**
     * The default value returned by this method is <tt>true</tt> meaning that the current assembly is selected.
     *
     * @param descriptor
     * @return
     */
    // Jeg har ikke en endelig liste af BuildTransformers, eller maaske

    // Omvendt kan man sige den propagateToChild ogsaa kan have en default???

    // Tror maaske ikke vi skal have en default??? Saa man tager stilling til det hver gang
    default boolean applyToThis(AssemblyDescriptor descriptor) {
        return true;
    }

    // I think we an isOpen on the child.
    // Then we can a valid error message. Or Perform some voodoo..

    // Have something about the relationship??? AssemblyRelationShip.isOpen?
    boolean propagateToChild(AssemblyDescriptor parent, AssemblyDescriptor child);

    public final class All implements ContainerPropagator {

        /** {@inheritDoc} */
        @Override
        public boolean propagateToChild(AssemblyDescriptor parent, AssemblyDescriptor child) {
            return true;
        }
    }

    public final class IfOpen implements ContainerPropagator {

        /** {@inheritDoc} */
        @Override
        public boolean propagateToChild(AssemblyDescriptor parent, AssemblyDescriptor child) {
            return true;
        }
    }

    /** An assembly propagator that never propagates outside of its immediate target */
    public final class Local implements ContainerPropagator {

        /** {@inheritDoc} */
        @Override
        public boolean propagateToChild(AssemblyDescriptor parent, AssemblyDescriptor child) {
            return false;
        }
    }
}
