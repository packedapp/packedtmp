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

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Version;
import java.util.Optional;
import java.util.Set;

import app.packed.container.Extension.Subtension;
import packed.internal.container.ExtensionModel;

/**
 * An extension descriptor.
 * <p>
 * This class describes an extension and defines various methods to obtain information about the extension. An instance
 * of this class is normally acquired by calling {@link #of(Class)}.
 */
public /* sealed */ interface ExtensionDescriptor extends Comparable<ExtensionDescriptor> {

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
     * Returns an immutable set of the direct dependencies of this extension in any order.
     * 
     * @return an immutable set of the direct dependencies of this extension in any order
     */
    Set<Class<? extends Extension>> dependencies();

    /**
     * Returns the extension's depth.
     * <p>
     * The depth of an extension with no dependencies is 0. Otherwise it is the maximum depth of any of its direct
     * dependencies plus 1. This has the nice property, that any dependency (including transitive extensions) of an
     * extension will always have a depth that is less than the depth of the extension itself.
     * 
     * @return the depth of the extension
     */
    // in a global . Defined as 0 if no dependencies
    // * otherwise max(all dependencies depth) + 1.
    // Depth is the length of the path to its BaseExtension
    // 0 for BaseExtension otherwise the length of the longest path to BaseExtension
    // -> Dependencies of a given extension always have a depth that is less than the given extension.
    // Det er jo ikke et trae... Men en graph. Giver depth mening?
    // Man kan argumentere med at man laver en masse hylder, hvor de enkelte extensions saa er.

    int depth();

    /**
     * Returns the type of extension this descriptor describes.
     * 
     * @return the type of extension this descriptor describes
     */
    Class<? extends Extension> extensionClass();

    /**
     * Returns the full name of the extension. The full name is always the canonical name of the {@link #type() extension
     * type} as returned by {@link Class#getCanonicalName()}.
     * 
     * @return the full name of the extension
     * @see Class#getCanonicalName()
     */
    String fullName();

    /**
     * Returns the module of the extension.
     * 
     * @return the module of the extension
     * @see Class#getModule()
     */
    default Module module() {
        return extensionClass().getModule();
    }

    /**
     * Returns the name of the extension. The name is always the simple name of the {@link #extensionClass() extension
     * class} as returned by {@link Class#getSimpleName()}.
     * 
     * @return the name of the extension.
     */
    String name();

    /**
     * Returns the version of the extension if present. The version is only valid if the extension has been loaded on the
     * module path. And
     * 
     * This method relies on a build system that writes the module xxx to the class file
     * 
     * @return the version of the extension if present
     * @see Module#getDescriptor()
     * @see ModuleDescriptor#version()
     */
    default Optional<Version> version() {
        ModuleDescriptor descriptor = module().getDescriptor(); // is null for an unnamed module
        return descriptor == null ? Optional.empty() : descriptor.version();
    }

    /**
     * Returns a descriptor for the specified extension class.
     * 
     * @param extensionClass
     *            the extension to return a descriptor for
     * @return a descriptor for the specified extension
     */
    static ExtensionDescriptor of(Class<? extends Extension> extensionClass) {
        return ExtensionModel.of(extensionClass);
    }
}

interface ExtensionDescriptor2 extends ExtensionDescriptor {

//  /**
//   * Returns a set of all the optional dependencies defined in {@link UsesExtensions#optionalDependencies()} that could
//   * not be successfully resolved.
//   * 
//   * @return a set of all optional dependencies that could not be successfully resolved
//   * @see UsesExtensions#optionalDependencies()
//   */
// Nahh 
//  Set<String> unresolvedDependencies();

    default Optional<Module> library() {
        // Ideen er lidt som AppVersion fra Helm charts
        // Syntes den er rigtig smart
        // A library is typically something that is released separately from PAcked
        // But where an extension acts as a kind of bridge
        return Optional.empty();
    }

    default Optional<Version> libraryVersion() {
        Optional<Module> m = library();
        return m.isEmpty() ? Optional.empty() : m.get().getDescriptor().version();
    }

    /**
     * If the extension can be used from other extensions return the subtension type. Otherwise empty.
     * 
     * @return the subtension type if any
     * 
     * @see Subtension
     */
    // Syntes det aerligtalt ikke...
    // Den er let at finde.
    // For end-user er den kun forvirrende.. De bruger den aldrig...
    // Og hvis vi nu faar flere...
    // Kunne ogsaa bare vaere en automatisk build service...
    Optional<Class<? extends Subtension>> subtensionType();

}
/**
 * Returns all the different types of contracts this extension exposes.
 * 
 * @return all the different types of contracts this extension exposes
 */
// Set<Class<? extends Contract>> contracts();
// 
// requiresExecution() // usesResources // ResourceUser
//
//default Set<Class<? extends Extension>> dependenciesWithTransitiveDependencies() {
//    dependencies().stream().map(ExtensionDescriptor::of).flatMap(ExtensionDescriptor::dependenciesWithTransitiveDependencies);
//    return null;
//}