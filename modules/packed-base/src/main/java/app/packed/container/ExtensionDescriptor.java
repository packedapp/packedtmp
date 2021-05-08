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

import packed.internal.container.ExtensionModel;

/**
 * An extension descriptor.
 * <p>
 * This class describes an extension and defines various methods to obtain information about the extension. An instance
 * of this interface is normally acquired by calling {@link #of(Class)}.
 */
// Altsaa den er ret anderledes i forhold til ComponentDescriptor..
public /* sealed */ interface ExtensionDescriptor extends Comparable<ExtensionDescriptor> {

    /**
     * In order to xxxx is a total order between all loaded extension forms..
     * <p>
     * bla bla same fullname different classloaders. However, in practice this should only be a problem if attempting to add
     * both extensions to the same container
     * 
     * @param descriptor
     *            the descriptor to be compared.
     * 
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
     *         specified object.
     *
     * @throws IllegalArgumentException
     *             if full name of this descriptor and the specified descriptor are equal. But they are loaded by different
     *             class loaders.
     */
    // Maaske kan vi kigge på classloader parent... Nej
    // Det er jo kun inter container...
    // Saa man kan bare ikke loade 2 containere med samme navn...
    @Override
    int compareTo(ExtensionDescriptor descriptor);

    /**
     * Returns an immutable set of every direct dependency of this extension in any order.
     * 
     * @return an immutable set of every direct dependency of this extension in any order
     */
    Set<Class<? extends Extension>> dependencies();

    /**
     * Returns the depth of the extension in the global extension dependency graph.
     * <p>
     * Extensions that have no dependencies have depth 0. Otherwise the depth of an extension it is the maximum depth of any
     * of its direct dependencies plus 1. This has the nice property, that any dependencies (including transitive
     * dependencies) of an extension will always have a depth that is less than the depth of the extension itself.
     * 
     * @return the depth of the extension
     */
    int depth();

    /**
     * Returns the type of extension this descriptor describes.
     * 
     * @return the type of extension this descriptor describes
     */
    Class<? extends Extension> extensionClass();

    /**
     * Returns the full name of the extension.
     * <p>
     * The full name is defined as the canonical name of the {@link #extensionClass()} as returned by
     * {@link Class#getCanonicalName()}.
     * 
     * @return the full name of the extension
     * @see #name()
     */
    String fullName();

    /**
     * Returns the module the extension is part of.
     * <p>
     * This module of an extension is always the module that the {@link #extensionClass() extension class} is located in.
     * 
     * @return the module the extension is part of
     * @see Class#getModule()
     */
    default Module module() {
        return extensionClass().getModule();
    }

    /**
     * Returns the version of the extension's module if present.
     * <p>
     * A version is present if the following holds:
     * <ul>
     * <li>The extension class is in a named module (on the module path)
     * <li>The "--module-version" option must have been specified when compiling the module. Most modern build systems will
     * automatically do so.
     * </ul>
     * <p>
     * While many build systems adds "--module-version" most IDE's do not, as this information is typically stored in
     * external build files. This is why you will most likely only see a version string for jar based modules on the module
     * path.
     * <p>
     * For more information about module versioning see this blog post:
     * <a href="https://medium.com/nipafx-news/jpms-support-for-module-versions-a-research-log-291c5826eebd">JPMS Support
     * For Module Versions — A Research Log</a>
     * <p>
     * Packed itself has no support for versioning.
     * 
     * @return the version of the extension's module if present
     * @see Module#getDescriptor()
     * @see ModuleDescriptor#version()
     */
    default Optional<Version> moduleVersion() {
        // Unnamed modules does not have a module descriptor
        ModuleDescriptor descriptor = module().getDescriptor();
        return descriptor == null ? Optional.empty() : descriptor.version();
    }

    /**
     * Returns the name of the extension.
     * <p>
     * The name is defined as the simple name of the {@link #extensionClass()} as returned by {@link Class#getSimpleName()}.
     * 
     * @return the name of the extension.
     * @see #fullName()
     */
    String name();

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

interface ExtensionDescriptor2 {

    /**
     * An extension might have an attached library.
     * 
     * @return
     */
    default Optional<Module> library() {
        // Ideen er lidt som AppVersion fra Helm charts
        // Syntes den er rigtig smart
        // A library is typically something that is released separately from PAcked
        // But where an extension acts as a kind of bridge
        return Optional.empty();
    }

    // Hmm nu kalder vi den module version...
    // Saa maaske libraryModuleVersion()...
    // IDK could give people the wrong impression
    default Optional<Version> libraryVersion() {
        Optional<Module> m = library();
        if (m.isPresent()) {
            ModuleDescriptor descriptor = m.get().getDescriptor();
            return descriptor == null ? Optional.empty() : descriptor.version();
        }
        return Optional.empty();
    }
}
// allDependencies
// default Set<Class<? extends Extension>> dependenciesWithTransitiveDependencies() {
//dependencies().stream().map(ExtensionDescriptor::of).flatMap(ExtensionDescriptor::dependenciesWithTransitiveDependencies);
//return null;
// }
