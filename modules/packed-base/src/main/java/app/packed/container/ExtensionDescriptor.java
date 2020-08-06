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

import java.util.Optional;
import java.util.Set;

import app.packed.base.Contract;
import app.packed.container.Extension.Subtension;

/**
 * An extension descriptor.
 * <p>
 * This class describes an extension and defines various methods to obtain information about the extension. An instance
 * of this class is normally acquired by calling {@link #of(Class)}.
 * 
 * @apiNote In the future, if the Java language permits, {@link ExtensionDescriptor} may become a {@code sealed}
 *          interface, which would prohibit subclassing except by explicitly permitted types.
 */
public interface ExtensionDescriptor extends Comparable<ExtensionDescriptor> {

    // Beskriv algorithme
    // Throws IAE if the full name of two descriptors are identical. But the extension type is not the same
    // This can only happen in weird classloader situations.
    @Override
    int compareTo(ExtensionDescriptor o);

    /**
     * Returns all the different types of contracts the extension exposes.
     * 
     * @return all the different types of contracts the extension exposes
     */
    Set<Class<? extends Contract>> contracts();

    /**
     * Returns an immutable set of any other extensions this extension depends on. The returned set does not include
     * transitive dependencies.
     * <p>
     * The returned set includes all optional dependencies specified via {@link ExtensionSidecar#optionalDependencies()}
     * that could be successfully resolved.
     * 
     * @return any other extensions this extension depends on
     */
    Set<Class<? extends Extension>> dependencies();

    /**
     * Returns the depth of the extension. The depth is defined as 0 for {@link BaseExtension}. For all dependencies it is
     * the maximum depth of of its dependencies plus one. This has the nice property, that any dependency of an extension
     * will always have a depth that is less than the extension itself.
     * 
     * @return the depth of the extension
     */
    int depth();

    default String fullName() {
        //
        return type().getCanonicalName();
    }

    /**
     * Returns the module that the extension belongs to.
     * 
     * @return the module that the extension belongs to
     * @see Class#getModule()
     */
    default Module module() {
        return type().getModule();
    }

    /**
     * Returns the name of the extension. The name is always the simple name of the {@link #type() extension type} as
     * returned by {@link Class#getSimpleName()}.
     * 
     * @return the name of the extension.
     */
    String name();

    default Optional<Class<? extends Subtension>> subtensionType() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the type of extension this descriptor describes.
     * 
     * @return the type of extension this descriptor describes
     */
    Class<? extends Extension> type();

    /**
     * Returns a set of all dependencies that could not be resolved.
     * 
     * @return a set
     * @see ExtensionSidecar#optionalDependencies()
     */
    default Set<String> unresolvedDependencies() {
        return Set.of();// Dependencies that could not be resolved.
    }

    /**
     * Returns a descriptor for the specified extension type.
     * 
     * @param extensionType
     *            the extension type to return a descriptor for
     * @return a descriptor for the specified extension type
     * 
     * @throws InternalExtensionException
     *             if a descriptor for the specified extension type could not be generated
     */
    static ExtensionDescriptor of(Class<? extends Extension> extensionType) {
        return PackedExtensionDescriptor.of(extensionType);
    }
}
// 

// requiresExecution() // usesResources // ResourceUser
//
//default Set<Class<? extends Extension>> dependenciesWithTransitiveDependencies() {
//    dependencies().stream().map(ExtensionDescriptor::of).flatMap(ExtensionDescriptor::dependenciesWithTransitiveDependencies);
//    return null;
//}