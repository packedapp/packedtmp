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

import java.util.Set;

import app.packed.base.Contract;
import app.packed.introspection.ExecutableDescriptor;
import packed.internal.container.ExtensionModel;

/**
 * The default implementation of {@link ExecutableDescriptor}.
 * 
 * @implNote ExtensionModel implements Comparable which we do not want to expose. So we have this thin wrapper. Well we
 *           expose the context which implements {@link Comparable}... So should probably fix that as well.
 **/
// Well just added Comparator to ED... Maybe we can just use ExtensionModel directly now (see implNote)
final class PackedExtensionDescriptor implements ExtensionDescriptor {

    /** The extension model we wrap. */
    private final ExtensionModel model;

    /** No public instantiation. */
    private PackedExtensionDescriptor(ExtensionModel model) {
        this.model = requireNonNull(model);
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(ExtensionDescriptor o) {
        return model.compareTo(((PackedExtensionDescriptor) o).model);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Contract>> contracts() {
        return model.contracts().keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> dependencies() {
        return model.directDependencies();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension> type() {
        return model.extensionType();
    }

    /**
     * Returns a descriptor for the specified extension type.
     * 
     * @param extensionType
     *            the extension type to return a descriptor for
     * @return a descriptor for the specified extension type
     */
    static ExtensionDescriptor of(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return new PackedExtensionDescriptor(ExtensionModel.of(extensionType));
    }
}

// A method for transitive dependencies...
// DirectedVertexGraph<T>
// Or Just DependencyGraph<T>
// DependencyGraph<Module>
// DependencyGraph<ExtensionDescriptor>
// DependencyGraph<ServiceDescriptor>...
//// What about export... Change stuff
