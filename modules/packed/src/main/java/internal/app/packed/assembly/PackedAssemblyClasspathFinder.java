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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyFinder;
import app.packed.build.BuildException;

/**
 * Classpath-based implementation of {@link AssemblyFinder}.
 */
public final class PackedAssemblyClasspathFinder implements AssemblyFinder {

    /** The class loader used to load assembly classes. */
    private final ClassLoader classLoader;

    /**
     * Creates a new finder with the specified class loader.
     *
     * @param classLoader
     *            the class loader to use
     */
    public PackedAssemblyClasspathFinder(ClassLoader classLoader) {
        this.classLoader = requireNonNull(classLoader, "classLoader is null");
    }

    /** {@inheritDoc} */
    @Override
    public Assembly findOne(String className) {
        requireNonNull(className, "className is null");

        // Load the class
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new BuildException("Assembly class not found: " + className, e);
        }

        // Verify it's an Assembly subclass
        if (!Assembly.class.isAssignableFrom(clazz)) {
            throw new BuildException("Class is not an Assembly: " + className);
        }

        // Instantiate
        return instantiate(clazz);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Assembly> findOptional(String className) {
        requireNonNull(className, "className is null");

        // Try to load the class
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }

        // Verify it's an Assembly subclass
        if (!Assembly.class.isAssignableFrom(clazz)) {
            throw new BuildException("Class is not an Assembly: " + className);
        }

        // Instantiate
        return Optional.of(instantiate(clazz));
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Assembly> Stream<T> serviceLoader(Class<T> assemblyType) {
        requireNonNull(assemblyType, "assemblyType is null");

        ServiceLoader<T> loader = ServiceLoader.load(assemblyType, classLoader);
        return StreamSupport.stream(loader.spliterator(), false);
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyFinder withPaths(Path... paths) {
        requireNonNull(paths, "paths is null");

        if (paths.length == 0) {
            return this;
        }

        // Convert paths to URLs
        URL[] urls = Arrays.stream(paths).map(PackedAssemblyClasspathFinder::pathToUrl).toArray(URL[]::new);

        // Create child classloader with current classloader as parent
        ClassLoader childLoader = new URLClassLoader(urls, classLoader);

        return new PackedAssemblyClasspathFinder(childLoader);
    }

    /**
     * Instantiates an assembly from a class.
     */
    private static Assembly instantiate(Class<?> clazz) {
        try {
            return (Assembly) clazz.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new BuildException("Assembly class has no public no-argument constructor: " + clazz.getName(), e);
        } catch (ReflectiveOperationException e) {
            throw new BuildException("Failed to instantiate assembly: " + clazz.getName(), e);
        }
    }

    /**
     * Converts a path to a URL.
     */
    private static URL pathToUrl(Path path) {
        requireNonNull(path, "path is null");
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new BuildException("Invalid path: " + path, e);
        }
    }
}
