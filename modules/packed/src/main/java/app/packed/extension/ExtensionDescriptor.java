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
package app.packed.extension;

import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.Version;
import java.util.Optional;
import java.util.Set;

import internal.app.packed.container.ExtensionModel;

/**
 * An immutable extension descriptor.
 * <p>
 * This class describes various static properties of an extension. An instance of this interface is normally acquired by
 * calling {@link #of(Class)}.
 * <p>
 * Unlike {@link ExtensionMirror} which contains information about a particular <strong>usage</strong> of an extension.
 * The information provided by this descriptor are static information about the extension itself.
 */
// Rename to ExtensionModel????
public sealed interface ExtensionDescriptor permits ExtensionModel {

    /** {@return an immutable unordered set containing every dependency the extension declares.} */
    Set<Class<? extends Extension<?>>> dependencies();

    default boolean dependsOn(Class<? extends Extension<?>> extension) {
        return dependencies().contains(extension);
    }

    /**
     * Returns the full name of the extension.
     * <p>
     * The full name is defined as the canonical name of the {@link #type() extension type} as returned by
     * {@link Class#getCanonicalName()}.
     *
     * @return the full name of the extension
     * @see #name()
     */
    String fullName();

    /**
     * Returns the module the extension is part of.
     * <p>
     * This module of an extension is always the module that the {@link #type() extension type} is located in.
     *
     * @return the module the extension is part of
     * @see Class#getModule()
     */
    default Module module() {
        return type().getModule();
    }

    /**
     * Returns the version of the extension's module if present.
     * <p>
     * A version is present if the following holds:
     * <ul>
     * <li>The extension class is in a named module on the module path
     * <li>The "--module-version" option have been specified when compiling the module. Most modern build systems will
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
     * Packed itself places no semantic meaning to module versions. And this method is purely for informational purposes.
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
     * The name is defined as the simple name of the {@link #type() extension type} as returned by
     * {@link Class#getSimpleName()}.
     *
     * @return the name of the extension.
     * @see #fullName()
     */
    String name();

    /** {@return the type of extension this descriptor describes.} */
    Class<? extends Extension<?>> type();

    /**
     * Returns a descriptor for the specified extension type.
     *
     * @param extensionType
     *            the type of extension to return a descriptor for
     * @return a descriptor for the specified extension type
     * @throws RuntimeException
     *             if the definition of the extension was invalid. For example, if there are circles in the extension
     *             dependency hierarchy.
     */
    static ExtensionDescriptor of(Class<? extends Extension<?>> extensionType) {
        return ExtensionModel.of(extensionType);
    }
}

interface SandboxExtensionDescriptor {

    /**
     * An extension might have an attached library (or more than 1?).
     *
     * @return
     */
    default Optional<Module> libraryModule() {
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
        Optional<Module> m = libraryModule();
        if (m.isPresent()) {
            ModuleDescriptor descriptor = m.get().getDescriptor();
            return descriptor == null ? Optional.empty() : descriptor.version();
        }
        return Optional.empty();
    }
}
// allDependencies
// default Set<Class<? extends Extension<?>>> dependenciesWithTransitiveDependencies() {
//dependencies().stream().map(ExtensionDescriptor::of).flatMap(ExtensionDescriptor::dependenciesWithTransitiveDependencies);
//return null;
// }
