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
package app.packed.assembly.sandbox;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.assembly.Assembly;
import internal.app.packed.assembly.PackedAssemblyClasspathFinder;

/**
 * Discovers and instantiates {@link Assembly assemblies} from the classpath.
 *
 * <p>This finder operates using {@link ClassLoader} to locate and load assembly classes.
 * It is intended for applications that run on the classpath (not as Java modules).
 *
 * <p><b>Usage from within an assembly:</b>
 * {@snippet :
 * protected void build() {
 *     AssemblyClasspathFinder finder = assembly().classpathFinder();
 *
 *     // Find by class name
 *     link(finder.findOne("com.example.MyAssembly"));
 *
 *     // Discover via ServiceLoader (META-INF/services)
 *     finder.serviceLoader(PluginAssembly.class).forEach(this::link);
 * }
 * }
 *
 * <p><b>Standalone usage:</b>
 * {@snippet :
 * AssemblyClasspathFinder finder = AssemblyClasspathFinder.of(getClass().getClassLoader());
 * Assembly assembly = finder.findOne("com.example.MyAssembly");
 * App.run(assembly);
 * }
 *
 * @see AssemblyFinder
 * @see AssemblyModulepathFinder
 * @see java.util.ServiceLoader
 */
public interface AssemblyClasspathFinder extends AssemblyFinder {

    /**
     * Finds and instantiates an assembly by its fully qualified class name.
     *
     * <p>The class must be loadable by this finder's class loader and must:
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
     * @throws NullPointerException
     *         if className is null
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
     * <p>The class must be loadable by this finder's class loader and must:
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
     * @throws NullPointerException
     *         if className is null
     */
    Optional<Assembly> findOptional(String className);

    /**
     * Discovers and instantiates assemblies using the {@link ServiceLoader} mechanism.
     *
     * <p>Assemblies must be registered in {@code META-INF/services/<fully-qualified-type-name>}.
     * Each listed class must have a public no-argument constructor.
     *
     * <p><b>Example:</b>
     * {@snippet :
     * // File: META-INF/services/com.example.PluginAssembly
     * // Contents: com.example.plugins.FooPlugin
     * //           com.example.plugins.BarPlugin
     *
     * finder.serviceLoader(PluginAssembly.class).forEach(this::link);
     * }
     *
     * @param <T>
     *        the assembly type to discover
     * @param assemblyType
     *        the service type (must be a subtype of {@link Assembly})
     * @return a stream of instantiated assemblies; may be empty if none found
     * @throws NullPointerException
     *         if assemblyType is null
     */
    <T extends Assembly> Stream<T> serviceLoader(Class<T> assemblyType);

    /**
     * Returns a new finder that additionally searches the specified paths.
     *
     * <p>Creates a child {@link ClassLoader} (typically a {@link java.net.URLClassLoader})
     * that searches the specified paths for classes, delegating to this finder's
     * class loader for classes not found in the paths.
     *
     * <p>This method returns a new finder; the current finder is not modified.
     *
     * @param paths
     *        paths to JAR files or directories containing classes
     * @return a new finder with the additional paths
     * @throws NullPointerException
     *         if paths is null or contains null elements
     */
    AssemblyClasspathFinder withPaths(Path... paths);

    /**
     * Creates a new classpath finder using the specified class loader.
     *
     * @param classLoader
     *        the class loader to use for loading assembly classes
     * @return a new classpath finder
     * @throws NullPointerException
     *         if classLoader is null
     */
    static AssemblyClasspathFinder of(ClassLoader classLoader) {
        Objects.requireNonNull(classLoader, "classLoader is null");
        return new PackedAssemblyClasspathFinder(classLoader);
    }
}
