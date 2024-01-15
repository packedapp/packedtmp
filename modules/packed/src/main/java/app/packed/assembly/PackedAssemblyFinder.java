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
package app.packed.assembly;

import static java.util.Objects.requireNonNull;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import app.packed.build.BuildException;
import internal.app.packed.container.AssemblySetup;

/**
 *
 */

// Tror maaske vi skal implementere en Class og en module thingy

final class PackedAssemblyFinder implements AssemblyFinder {

    AssemblySetup as;

    final Class<?> clazz;
    ModuleFinder mf;

    ArrayList<ModuleLayer> moduleLayers;

    ClassLoader parentLoader;

    PackedAssemblyFinder(Class<?> clazz, AssemblySetup as) {
        this.clazz = clazz;
        this.as = as;
        this.parentLoader = clazz.getClassLoader();
        this.moduleLayers = new ArrayList<>(1);
        moduleLayers.add(clazz.getModule().getLayer());
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Assembly> T findOne(ServiceLoader<T> loader) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Assembly findOne(String className) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Assembly findOne(String moduleName, String className) {
        ModuleLayer layer = loadLayer(moduleName);
        Class<?> c;
        try {
            c = layer.findLoader(moduleName).loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new BuildException("OOPS", e);
        }

        try {
            return (Assembly) c.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new BuildException("OOPS", e);
        }
    }

    private ModuleLayer loadLayer(String moduleName) {
        if (mf == null) {
            throw new IllegalStateException("Add least one path must be set using #addPath(Path)");
        }

        ModuleLayer parent = moduleLayers.get(0);

        Configuration cf = parent.configuration().resolve(mf, ModuleFinder.of(), Set.of(moduleName));

        // A new layer we are defining

        ModuleLayer layer = ModuleLayer.defineModulesWithOneLoader(cf, List.of(parent), parentLoader).layer();

        return layer;
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyFinder classLoader(ClassLoader classLoader) {
        this.parentLoader = requireNonNull(classLoader);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyFinder addModuleLayer(ModuleLayer moduleLayer) {
        moduleLayers.add(moduleLayer);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyFinder paths(Path... paths) {
        ModuleFinder m = ModuleFinder.of(paths);
        ModuleFinder existing = mf;
        this.mf = mf == null ? m : ModuleFinder.compose(existing, m);
        return this;
    }
}
