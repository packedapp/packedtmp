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

import java.lang.invoke.MethodHandles;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import app.packed.assembly.Assembly;
import internal.app.packed.assembly.PackedAssemblyModulepathFinder;

/**
 * Discovers and instantiates {@link Assembly assemblies} from the modulepath.
 *
 * <p>This finder operates using {@link ModuleLayer} to locate and load assembly classes
 * from Java modules. It supports loading modules dynamically from external paths by
 * creating child module layers.
 *
 * <p><b>Usage from within an assembly:</b>
 * {@snippet :
 * protected void build() {
 *     AssemblyModulepathFinder finder = assembly().modulepathFinder();
 *
 *     // Find from current module layer
 *     link(finder.findOne("com.example.MyAssembly"));
 *
 *     // Discover via ServiceLoader (module provides declarations)
 *     finder.serviceLoader(PluginAssembly.class).forEach(this::link);
 *
 *     // Load from external module path
 *     Assembly plugin = finder
 *         .withPaths(Path.of("/plugins"))
 *         .findOne("com.plugin", "com.plugin.PluginAssembly");
 *     link(plugin);
 * }
 * }
 *
 * <p><b>Standalone usage:</b>
 * {@snippet :
 * AssemblyModulepathFinder finder = AssemblyModulepathFinder.of(MethodHandles.lookup());
 * finder.serviceLoader(PluginAssembly.class).forEach(System.out::println);
 * }
 *
 * <h2>Module Layers</h2>
 * <p>When {@link #withPaths(Path...)} is used followed by {@link #findOne(String, String)},
 * a new {@link ModuleLayer} is created as a child of the current layer:
 * <pre>
 * Boot Layer
 *     └── Application Layer (your module)
 *             └── Plugin Layer (created by finder)
 * </pre>
 * <p>The child layer can access modules from parent layers, but not vice versa.
 *
 * @see AssemblyFinder
 * @see AssemblyFinder#ofClasspath(ClassLoader)
 * @see ServiceLoader
 * @see ModuleLayer
 */
public interface AssemblyModulepathFinder extends AssemblyFinder {

    @Override
    /**
     * Finds and instantiates an assembly by its fully qualified class name.
     *
     * <p>The class must be accessible from the current module layer context:
     * <ul>
     *   <li>The module containing the class must be in an accessible layer</li>
     *   <li>The package must be exported (or opened) to the calling module</li>
     *   <li>The class must have a public no-argument constructor</li>
     * </ul>
     *
     * <p>To load an assembly from a module not yet in a layer, use
     * {@link #withPaths(Path...)} followed by {@link #findOne(String, String)}.
     *
     * @param className
     *        the fully qualified class name of the assembly
     * @return a new instance of the assembly
     * @throws app.packed.build.BuildException
     *         if the class cannot be found or instantiated
     * @throws NullPointerException
     *         if className is null
     */
    Assembly findOne(String className);

    @Override
    /**
     * Finds and instantiates an assembly by its fully qualified class name, returning
     * an empty optional if the class cannot be found.
     *
     * <p>This method is useful for conditionally linking optional assemblies:
     * {@snippet :
     * finder.findOptional("com.example.OptionalPlugin").ifPresent(this::link);
     * }
     *
     * @param className
     *        the fully qualified class name of the assembly
     * @return an optional containing the assembly instance, or empty if the class was not found
     * @throws app.packed.build.BuildException
     *         if the class was found but is not an Assembly subclass or cannot be instantiated
     * @throws NullPointerException
     *         if className is null
     */
    Optional<Assembly> findOptional(String className);

    /**
     * Finds and instantiates an assembly from a specific module.
     *
     * <p>If paths have been configured via {@link #withPaths(Path...)}, this method
     * creates a new {@link ModuleLayer} containing the specified module, loaded from
     * those paths. The new layer is a child of the finder's parent layer.
     *
     * <p>If no paths are configured, the module must already be present in an
     * accessible module layer.
     *
     * <p>Requirements:
     * <ul>
     *   <li>The module must exist (in configured paths or current layers)</li>
     *   <li>The package containing the class must be exported</li>
     *   <li>The class must have a public no-argument constructor</li>
     * </ul>
     *
     * @param moduleName
     *        the name of the module (e.g., "com.example.plugin")
     * @param className
     *        the fully qualified class name of the assembly
     * @return a new instance of the assembly
     * @throws app.packed.build.BuildException
     *         if the module or class cannot be found, or instantiation fails
     * @throws NullPointerException
     *         if moduleName or className is null
     */
    Assembly findOne(String moduleName, String className);

    /**
     * Finds and instantiates an assembly from a specific module, returning an empty
     * optional if the module or class cannot be found.
     *
     * <p>This method is useful for conditionally linking optional plugin modules:
     * {@snippet :
     * finder.withPaths(pluginDir)
     *     .findOptional("com.optional.plugin", "com.optional.plugin.Main")
     *     .ifPresent(this::link);
     * }
     *
     * <p>If paths have been configured via {@link #withPaths(Path...)}, this method
     * creates a new {@link ModuleLayer} containing the specified module if found.
     *
     * @param moduleName
     *        the name of the module (e.g., "com.example.plugin")
     * @param className
     *        the fully qualified class name of the assembly
     * @return an optional containing the assembly instance, or empty if the module
     *         or class was not found
     * @throws app.packed.build.BuildException
     *         if the module and class were found but the class is not an Assembly
     *         subclass or cannot be instantiated
     * @throws NullPointerException
     *         if moduleName or className is null
     */
    Optional<Assembly> findOptional(String moduleName, String className);

    /**
     * Returns the names of all modules available in the configured paths.
     *
     * <p>This method is useful for discovering what modules are available before
     * loading them:
     * {@snippet :
     * AssemblyModulepathFinder finder = baseFinder.withPaths(pluginDir);
     * Set<String> available = finder.availableModules();
     * System.out.println("Available plugins: " + available);
     * }
     *
     * <p>If no paths have been configured, returns an empty set.
     *
     * @return the set of module names available in the configured paths
     * @see #loadedModules()
     */
    Set<String> availableModules();

    /**
     * Returns the names of all modules currently loaded in the active layer.
     *
     * <p>This includes modules loaded via {@link #withModules(String...)} or
     * {@link #withAllModules()}.
     *
     * {@snippet :
     * var finder = baseFinder.withPaths(pluginDir).withModules("plugin.a", "plugin.b");
     * Set<String> loaded = finder.loadedModules();
     * // loaded contains "plugin.a", "plugin.b"
     * }
     *
     * @return the set of module names in the active layer
     * @see #availableModules()
     * @see #loadedModuleVersions()
     */
    Set<String> loadedModules();

    /**
     * Returns the versions of all modules available in the configured paths.
     *
     * <p>This method is useful for inspecting module versions before loading:
     * {@snippet :
     * var versions = finder.withPaths(pluginDir).availableModuleVersions();
     * versions.forEach((name, version) ->
     *     System.out.println(name + ": " + version.map(Object::toString).orElse("unversioned")));
     * }
     *
     * <p>If no paths have been configured, returns an empty map.
     *
     * @return a map from module name to its version (empty optional if unversioned)
     * @see #availableModules()
     * @see #loadedModuleVersions()
     */
    Map<String, Optional<ModuleDescriptor.Version>> availableModuleVersions();

    /**
     * Returns the versions of all modules currently loaded in the active layer.
     *
     * {@snippet :
     * var finder = baseFinder.withPaths(pluginDir).withAllModules();
     * var versions = finder.loadedModuleVersions();
     * versions.forEach((name, version) ->
     *     System.out.println(name + ": " + version.map(Object::toString).orElse("unversioned")));
     * }
     *
     * @return a map from module name to its version (empty optional if unversioned)
     * @see #loadedModules()
     * @see #availableModuleVersions()
     */
    Map<String, Optional<ModuleDescriptor.Version>> loadedModuleVersions();

    /**
     * Returns the parent module layer used by this finder.
     *
     * <p>When loading modules via {@link #withPaths(Path...)}, new layers are
     * created as children of this layer.
     *
     * @return the parent module layer
     */
    ModuleLayer parentLayer();

    @Override
    /**
     * Discovers and instantiates assemblies using the {@link ServiceLoader} mechanism.
     *
     * <p>Assemblies must be declared in their module's {@code module-info.java}:
     * {@snippet :
     * module com.example.plugin {
     *     requires app.packed;
     *     provides com.example.PluginAssembly with com.example.plugin.MyPlugin;
     * }
     * }
     *
     * <p>Uses {@link ServiceLoader#load(ModuleLayer, Class)} to discover services
     * from the finder's module layer and its parent layers.
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
     * Returns the active module layer used by this finder.
     *
     * <p>The active layer is:
     * <ul>
     *   <li>The parent layer, if no modules have been loaded via {@link #withModules(String...)}</li>
     *   <li>The child layer created by {@link #withModules(String...)}, if modules were loaded</li>
     * </ul>
     *
     * <p>This is the layer that {@link #findOne(String)}, {@link #serviceLoader(Class)}, and
     * other lookup methods search.
     *
     * @return the active module layer
     */
    ModuleLayer layer();

    @Override
    /**
     * Returns a new finder that will search the specified paths for modules.
     *
     * <p>The paths are used when {@link #withModules(String...)} or {@link #findOne(String, String)}
     * is called to create a new {@link ModuleLayer}. The paths should point to:
     * <ul>
     *   <li>Directories containing exploded modules</li>
     *   <li>JAR files (modular JARs)</li>
     *   <li>JMOD files</li>
     * </ul>
     *
     * <p>Multiple calls accumulate paths. This method returns a new finder;
     * the current finder is not modified.
     *
     * @param paths
     *        paths to search for modules
     * @return a new finder with the additional paths configured
     * @throws NullPointerException
     *         if paths is null or contains null elements
     */
    AssemblyModulepathFinder withPaths(Path... paths);

    /**
     * Returns a new finder with the specified modules loaded into a child layer.
     *
     * <p>This method creates a new {@link ModuleLayer} containing the specified modules,
     * loaded from the paths configured via {@link #withPaths(Path...)}. The new layer
     * is a child of the current {@link #layer()}, so newly loaded modules can see
     * previously loaded modules.
     *
     * <p>The returned finder's {@link #layer()} will return this new child layer, and
     * methods like {@link #findOne(String)}, {@link #serviceLoader(Class)}, and
     * {@link #findOne(String, String)} will search within this layer.
     *
     * <p><b>Example - loading inter-dependent plugins:</b>
     * {@snippet :
     * // Load plugins that depend on each other into the same layer
     * AssemblyModulepathFinder pluginFinder = finder
     *     .withPaths(Path.of("/plugins"))
     *     .withModules("plugin.core", "plugin.extension");
     *
     * // Both modules are in the same layer, can see each other
     * pluginFinder.findOne("plugin.core.CoreAssembly");
     * pluginFinder.findOne("plugin.extension.ExtAssembly");
     *
     * // ServiceLoader finds services from both modules
     * pluginFinder.serviceLoader(PluginAssembly.class).forEach(this::link);
     * }
     *
     * <p><b>Example - chaining withModules calls:</b>
     * {@snippet :
     * // Each call builds on the previous layer
     * var finder = baseFinder.withPaths(pluginDir)
     *     .withModules("plugin.core")      // layer1: core
     *     .withModules("plugin.extension"); // layer2: extension (can see core)
     * }
     *
     * <p>This method returns a new finder; the current finder is not modified.
     *
     * @param moduleNames
     *        the names of modules to load
     * @return a new finder with the modules loaded into a child layer
     * @throws app.packed.build.BuildException
     *         if any module cannot be found in the configured paths
     * @throws IllegalStateException
     *         if no paths have been configured via {@link #withPaths(Path...)}
     * @throws NullPointerException
     *         if moduleNames is null or contains null elements
     * @see #withAllModules()
     */
    AssemblyModulepathFinder withModules(String... moduleNames);

    /**
     * Returns a new finder with all available modules loaded into a child layer.
     *
     * <p>This is a convenience method equivalent to:
     * {@snippet :
     * finder.withModules(finder.availableModules().toArray(String[]::new))
     * }
     *
     * <p>This method returns a new finder; the current finder is not modified.
     *
     * @return a new finder with all available modules loaded
     * @throws IllegalStateException
     *         if no paths have been configured via {@link #withPaths(Path...)}
     * @see #withModules(String...)
     * @see #availableModules()
     */
    AssemblyModulepathFinder withAllModules();

    /**
     * Creates a new modulepath finder.
     *
     * <p>The lookup object determines:
     * <ul>
     *   <li>The parent {@link ModuleLayer} (from the lookup class's module)</li>
     *   <li>Access privileges for instantiating assemblies</li>
     * </ul>
     *
     * @param caller
     *        a lookup object from the calling module
     * @return a new modulepath finder
     * @throws NullPointerException
     *         if caller is null
     */
    static AssemblyModulepathFinder of(MethodHandles.Lookup caller) {
        Objects.requireNonNull(caller, "caller is null");
        return new PackedAssemblyModulepathFinder(caller);
    }
}
