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
package internal.app.packed.assembly;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ResolutionException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyModulepathFinder;
import app.packed.build.BuildException;

/**
 * Implementation of {@link AssemblyModulepathFinder}.
 */
public final class PackedAssemblyModulepathFinder implements AssemblyModulepathFinder {

    /** The lookup used for access control. */
    private final MethodHandles.Lookup lookup;

    /** The parent module layer (original layer from lookup, used for creating child layers). */
    private final ModuleLayer parentLayer;

    /** The active module layer (parentLayer if no modules loaded, child layer if withModules was called). */
    private final ModuleLayer activeLayer;

    /** The module finder for configured paths, or null if no paths configured. */
    private final ModuleFinder moduleFinder;

    /**
     * Creates a new finder from a lookup.
     *
     * @param lookup
     *            the lookup object
     */
   public PackedAssemblyModulepathFinder(MethodHandles.Lookup lookup) {
        this.lookup = requireNonNull(lookup);
        this.parentLayer = getModuleLayer(lookup);
        this.activeLayer = parentLayer; // initially same as parent
        this.moduleFinder = null;
    }

    /**
     * Internal constructor with all fields.
     */
    private PackedAssemblyModulepathFinder(MethodHandles.Lookup lookup, ModuleLayer parentLayer, ModuleLayer activeLayer, ModuleFinder moduleFinder) {
        this.lookup = requireNonNull(lookup);
        this.parentLayer = requireNonNull(parentLayer);
        this.activeLayer = requireNonNull(activeLayer);
        this.moduleFinder = moduleFinder; // may be null
    }

    /** {@inheritDoc} */
    @Override
    public Assembly findOne(String className) {
        requireNonNull(className, "className is null");

        // Use a classloader from the active layer
        ClassLoader loader = getActiveClassLoader();
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, loader);
        } catch (ClassNotFoundException e) {
            throw new BuildException("Assembly class not found: " + className, e);
        }

        return instantiateAndValidate(clazz);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Assembly> findOptional(String className) {
        requireNonNull(className, "className is null");

        ClassLoader loader = getActiveClassLoader();
        Class<?> clazz;
        try {
            clazz = Class.forName(className, true, loader);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }

        return Optional.of(instantiateAndValidate(clazz));
    }

    /** {@inheritDoc} */
    @Override
    public Assembly findOne(String moduleName, String className) {
        requireNonNull(moduleName, "moduleName is null");
        requireNonNull(className, "className is null");

        ModuleLayer layer = resolveLayerForModule(moduleName);

        // Load the class from the module's classloader
        Class<?> clazz;
        try {
            ClassLoader loader = layer.findLoader(moduleName);
            clazz = Class.forName(className, true, loader);
        } catch (ClassNotFoundException e) {
            throw new BuildException("Assembly class not found: " + className + " in module " + moduleName, e);
        }

        return instantiateAndValidate(clazz);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Assembly> findOptional(String moduleName, String className) {
        requireNonNull(moduleName, "moduleName is null");
        requireNonNull(className, "className is null");

        // First check if module exists in active layer
        if (activeLayer.findModule(moduleName).isPresent()) {
            // Module already loaded in active layer
            ClassLoader loader = activeLayer.findLoader(moduleName);
            try {
                Class<?> clazz = Class.forName(className, true, loader);
                return Optional.of(instantiateAndValidate(clazz));
            } catch (ClassNotFoundException e) {
                return Optional.empty();
            }
        }

        // Check if module is in configured paths
        if (moduleFinder != null && moduleFinder.find(moduleName).isPresent()) {
            ModuleLayer layer = createChildLayer(Set.of(moduleName));
            try {
                ClassLoader loader = layer.findLoader(moduleName);
                Class<?> clazz = Class.forName(className, true, loader);
                return Optional.of(instantiateAndValidate(clazz));
            } catch (ClassNotFoundException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> availableModules() {
        if (moduleFinder == null) {
            return Collections.emptySet();
        }

        return moduleFinder.findAll().stream().map(ref -> ref.descriptor().name()).collect(Collectors.toUnmodifiableSet());
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> loadedModules() {
        return activeLayer.modules().stream().map(Module::getName).collect(Collectors.toUnmodifiableSet());
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Optional<ModuleDescriptor.Version>> availableModuleVersions() {
        if (moduleFinder == null) {
            return Collections.emptyMap();
        }

        return moduleFinder.findAll().stream().collect(Collectors.toUnmodifiableMap(ref -> ref.descriptor().name(), ref -> ref.descriptor().version()));
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Optional<ModuleDescriptor.Version>> loadedModuleVersions() {
        return activeLayer.modules().stream().collect(Collectors.toUnmodifiableMap(Module::getName, module -> module.getDescriptor().version()));
    }

    /** {@inheritDoc} */
    @Override
    public ModuleLayer layer() {
        return activeLayer;
    }

    /** {@inheritDoc} */
    @Override
    public ModuleLayer parentLayer() {
        return parentLayer;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Assembly> Stream<T> serviceLoader(Class<T> assemblyType) {
        requireNonNull(assemblyType, "assemblyType is null");

        // Search the active layer (includes parent layers)
        ServiceLoader<T> loader = ServiceLoader.load(activeLayer, assemblyType);
        return StreamSupport.stream(loader.spliterator(), false);
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyModulepathFinder withAllModules() {
        if (moduleFinder == null) {
            throw new IllegalStateException("No paths configured. Use withPaths() before withAllModules().");
        }

        Set<String> allModules = availableModules();
        if (allModules.isEmpty()) {
            return this;
        }

        return withModules(allModules.toArray(String[]::new));
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyModulepathFinder withModules(String... moduleNames) {
        requireNonNull(moduleNames, "moduleNames is null");

        if (moduleNames.length == 0) {
            return this;
        }

        if (moduleFinder == null) {
            throw new IllegalStateException("No paths configured. Use withPaths() before withModules().");
        }

        // Validate no nulls
        for (String name : moduleNames) {
            requireNonNull(name, "moduleNames contains null");
        }

        // Create child layer with all specified modules (as child of activeLayer)
        Set<String> moduleSet = Set.of(moduleNames);
        ModuleLayer childLayer = createChildLayer(moduleSet);

        // Return new finder with child layer as active layer
        // Keep the same moduleFinder so additional modules can be loaded
        return new PackedAssemblyModulepathFinder(lookup, parentLayer, childLayer, moduleFinder);
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyModulepathFinder withPaths(Path... paths) {
        requireNonNull(paths, "paths is null");

        if (paths.length == 0) {
            return this;
        }

        // Validate no nulls
        for (Path path : paths) {
            requireNonNull(path, "paths contains null");
        }

        // Create or compose module finder
        ModuleFinder newFinder = ModuleFinder.of(paths);
        ModuleFinder combined = moduleFinder == null ? newFinder : ModuleFinder.compose(moduleFinder, newFinder);

        return new PackedAssemblyModulepathFinder(lookup, parentLayer, activeLayer, combined);
    }

    /**
     * Creates a child layer with the specified modules. The new layer is created as a child of activeLayer, so it can see
     * previously loaded modules.
     */
    private ModuleLayer createChildLayer(Set<String> moduleNames) {
        try {
            // Resolve against activeLayer so new modules can see previously loaded modules
            Configuration cf = activeLayer.configuration().resolve(moduleFinder, ModuleFinder.of(), moduleNames);

            return ModuleLayer.defineModulesWithOneLoader(cf, List.of(activeLayer), lookup.lookupClass().getClassLoader()).layer();
        } catch (ResolutionException e) {
            throw new BuildException("Failed to resolve modules " + moduleNames + ": " + e.getMessage(), e);
        }
    }

    /**
     * Resolves the module layer for loading a specific module. If the module is in the active layer (or its parents), uses
     * that. Otherwise creates a new child layer of activeLayer.
     */
    private ModuleLayer resolveLayerForModule(String moduleName) {
        // Check if already in active layer or any parent layer
        // findModule searches the layer and all its parents
        if (activeLayer.findModule(moduleName).isPresent()) {
            return activeLayer;
        }

        // Module not loaded - need to create a child layer of activeLayer
        if (moduleFinder == null) {
            throw new BuildException("Module not found: " + moduleName + ". Use withPaths() to configure paths if the module is external.");
        }

        if (moduleFinder.find(moduleName).isEmpty()) {
            throw new BuildException("Module not found in configured paths: " + moduleName);
        }

        // Create child layer of activeLayer so new module can see previously loaded modules
        return createChildLayer(Set.of(moduleName));
    }

    /**
     * Gets a classloader for the active layer.
     */
    private ClassLoader getActiveClassLoader() {
        // If we have modules loaded, use one of their classloaders
        // Otherwise fall back to the lookup class's classloader
        Set<Module> modules = activeLayer.modules();
        if (!modules.isEmpty()) {
            return modules.iterator().next().getClassLoader();
        }
        return lookup.lookupClass().getClassLoader();
    }

    /**
     * Validates that the class is an Assembly and instantiates it using the lookup for proper module access.
     */
    private Assembly instantiateAndValidate(Class<?> clazz) {
        if (!Assembly.class.isAssignableFrom(clazz)) {
            throw new BuildException("Class is not an Assembly: " + clazz.getName());
        }

        try {
            MethodHandle constructor = lookup.findConstructor(clazz, MethodType.methodType(void.class));
            return (Assembly) constructor.invoke();
        } catch (NoSuchMethodException e) {
            throw new BuildException("Assembly class has no public no-argument constructor: " + clazz.getName(), e);
        } catch (Throwable e) {
            throw new BuildException("Failed to instantiate assembly: " + clazz.getName(), e);
        }
    }

    /**
     * Gets the module layer from a lookup, handling unnamed modules.
     */
    private static ModuleLayer getModuleLayer(MethodHandles.Lookup lookup) {
        Module module = lookup.lookupClass().getModule();
        ModuleLayer layer = module.getLayer();

        // Unnamed modules (classpath) don't have a layer
        if (layer == null) {
            return ModuleLayer.boot();
        }
        return layer;
    }
}
