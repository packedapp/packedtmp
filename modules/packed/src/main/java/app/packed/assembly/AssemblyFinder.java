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
package app.packed.assembly;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import internal.app.packed.assembly.PackedAssemblyClasspathFinder;

/**
 * Interface for discovering and instantiating {@link Assembly assemblies}.
 *
 * <p>Assembly finders can operate on either the classpath or modulepath:
 * <ul>
 *   <li>{@link #ofClasspath(ClassLoader)} - for classpath-based discovery using {@link ClassLoader}</li>
 *   <li>{@link AssemblyModulepathFinder} - for modulepath-based discovery using {@link ModuleLayer}</li>
 * </ul>
 *
 * <p><b>Example - classpath usage:</b>
 * {@snippet :
 * AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());
 * finder.serviceLoader(PluginAssembly.class).forEach(this::link);
 * finder.findOptional("com.example.OptionalPlugin").ifPresent(this::link);
 * }
 *
 * <p><b>Example - generic assembly loading:</b>
 * {@snippet :
 * void loadPlugins(AssemblyFinder finder) {
 *     // Works with either classpath or modulepath finder
 *     finder.serviceLoader(PluginAssembly.class).forEach(this::link);
 *     finder.findOptional("com.example.OptionalPlugin").ifPresent(this::link);
 * }
 * }
 *
 * @see AssemblyModulepathFinder
 */
public sealed interface AssemblyFinder permits PackedAssemblyClasspathFinder, AssemblyModulepathFinder {

    /**
     * Finds and instantiates an assembly by its fully qualified class name.
     *
     * <p>The class must be accessible from the finder's context and must:
     * <ul>
     *   <li>Be a concrete subclass of {@link Assembly}</li>
     *   <li>Have a public no-argument constructor</li>
     * </ul>
     *
     * @param className
     *        the fully qualified class name of the assembly (e.g., "com.example.MyAssembly")
     * @return a new instance of the assembly
     * @throws app.packed.build.BuildException
     *         if the class cannot be found, is not an Assembly subclass, or cannot be instantiated
     */
    Assembly findOne(String className);

    /**
     * Finds and instantiates an assembly by its fully qualified class name, returning
     * an empty optional if the class cannot be found.
     *
     * <p>This method is useful for conditionally linking optional assemblies:
     * {@snippet :
     * finder.findOptional("com.example.OptionalPlugin").ifPresent(this::link);
     * }
     *
     * <p>The class must be accessible from the finder's context and must:
     * <ul>
     *   <li>Be a concrete subclass of {@link Assembly}</li>
     *   <li>Have a public no-argument constructor</li>
     * </ul>
     *
     * @param className
     *        the fully qualified class name of the assembly (e.g., "com.example.MyAssembly")
     * @return an optional containing the assembly instance, or empty if the class was not found
     * @throws app.packed.build.BuildException
     *         if the class was found but is not an Assembly subclass or cannot be instantiated
     */
    Optional<Assembly> findOptional(String className);

    /**
     * Discovers and instantiates assemblies using the {@link java.util.ServiceLoader} mechanism.
     *
     * <p>The discovery mechanism depends on the finder type:
     * <ul>
     *   <li><b>Classpath</b>: Assemblies must be registered in
     *       {@code META-INF/services/<fully-qualified-type-name>}</li>
     *   <li><b>Modulepath</b>: Assemblies must be declared in {@code module-info.java} using
     *       {@code provides ... with ...}</li>
     * </ul>
     *
     * <p>Each assembly class must have a public no-argument constructor.
     *
     * <p><b>Example:</b>
     * {@snippet :
     * finder.serviceLoader(PluginAssembly.class).forEach(this::link);
     * }
     *
     * @param <T>
     *        the assembly type to discover
     * @param assemblyType
     *        the service type (must be a subtype of {@link Assembly})
     * @return a stream of instantiated assemblies; may be empty if none found
     */
    <T extends Assembly> Stream<T> serviceLoader(Class<T> assemblyType);

    /**
     * Returns a new finder that additionally searches the specified paths.
     *
     * <p>The behavior depends on the finder type:
     * <ul>
     *   <li><b>Classpath</b>: Creates a child {@link ClassLoader} that searches
     *       the specified paths</li>
     *   <li><b>Modulepath</b>: Configures paths for creating new {@link ModuleLayer ModuleLayers}</li>
     * </ul>
     *
     * <p>Multiple calls accumulate paths. This method returns a new finder;
     * the current finder is not modified.
     *
     * @param paths
     *        paths to JAR files or directories to search
     * @return a new finder with the additional paths configured
     */
    AssemblyFinder withPaths(Path... paths);

    /**
     * Creates a new assembly finder that operates on the classpath.
     *
     * <p>The returned finder uses the specified {@link ClassLoader} to locate and
     * load assembly classes. Assemblies can be discovered via:
     * <ul>
     *   <li>{@link #findOne(String)} / {@link #findOptional(String)} - by class name</li>
     *   <li>{@link #serviceLoader(Class)} - via {@code META-INF/services} registration</li>
     * </ul>
     *
     * <p><b>Example:</b>
     * {@snippet :
     * AssemblyFinder finder = AssemblyFinder.ofClasspath(getClass().getClassLoader());
     * Assembly assembly = finder.findOne("com.example.MyAssembly");
     * App.run(assembly);
     * }
     *
     * @param classLoader
     *        the class loader to use for loading assembly classes
     * @return a new classpath-based assembly finder
     */
    static AssemblyFinder ofClasspath(ClassLoader classLoader) {
        requireNonNull(classLoader, "classLoader is null");
        return new PackedAssemblyClasspathFinder(classLoader);
    }
}
