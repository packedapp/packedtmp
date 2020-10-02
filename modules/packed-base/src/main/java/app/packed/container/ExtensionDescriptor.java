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

import app.packed.container.Extension.Subtension;
import packed.internal.container.ExtensionModel;

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

    /**
     * Beskriv algorithme
     * 
     * @param descriptor
     *            the descriptor to be compared.
     * 
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
     *         specified object.
     *
     * @throws IllegalArgumentException
     *             if the depth and full name of this descriptor and the the specified descriptor are equal. But they are
     *             loaded by different class loaders.
     */
    @Override
    int compareTo(ExtensionDescriptor descriptor);

    /**
     * Returns all the different types of contracts the extension exposes.
     * 
     * @return all the different types of contracts the extension exposes
     */
    // Set<Class<? extends Contract>> contracts();

    /**
     * Returns an immutable set of other extensions this extension depends on. The returned set does not include transitive
     * dependencies.
     * <p>
     * The returned set includes all optional dependencies specified via {@link ExtensionSetup#optionalDependencies()} that
     * could be successfully resolved.
     * 
     * @return any other extensions this extension depends on
     */
    ExtensionSet dependencies();

    /**
     * Returns the extension's depth.
     * <p>
     * The depth of an extension with no dependencies is 0. Otherwise it is the maximum depth of any of its direct
     * dependencies plus 1. This has the nice property, that any dependency (including transitive extensions) of an
     * extension will always have a depth that is less than the depth of the extension itself.
     * 
     * @return the depth of the extension
     */
    int depth();

    /**
     * Returns the full name of the extension.
     * 
     * @return the full name of the extension
     * @see Class#getCanonicalName()
     */
    // Skal vi have noget med at det er det vi sortere efter
    String fullName();

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
     * Returns a set of all dependencies defined in {@link ExtensionSetup#optionalDependencies()} that could not be
     * resolved.
     * 
     * @return a set of all unresolved dependencies
     * @see ExtensionSetup#optionalDependencies()
     */
    Set<String> unresolvedDependencies();

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
        return ExtensionModel.of(extensionType);
    }
}
// 
// requiresExecution() // usesResources // ResourceUser
//
//default Set<Class<? extends Extension>> dependenciesWithTransitiveDependencies() {
//    dependencies().stream().map(ExtensionDescriptor::of).flatMap(ExtensionDescriptor::dependenciesWithTransitiveDependencies);
//    return null;
//}