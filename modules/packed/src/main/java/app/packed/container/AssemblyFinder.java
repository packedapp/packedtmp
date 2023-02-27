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

import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 *
 */
public interface AssemblyFinder {

    /**
     * <p>
     * The default is value is this finders source class's class loader. Typically the assembly that created the assembly
     * finder.
     *
     * @param classLoader
     *            the class loader to use as a parent loader
     * @return this assembly finder
     */
    AssemblyFinder classLoader(ClassLoader classLoader);

    AssemblyFinder addModuleLayer(ModuleLayer moduleLayer);

    void forEach(Consumer<? super Assembly> action);

    default AssemblyFinder paths(String... paths) {
        for (String s : paths) {
            paths(Path.of(s));
        }
        return this;
    }

    AssemblyFinder paths(Path... paths);

    Assembly assembly(ServiceLoader<? super Assembly> loader);

    Assembly assembly(String className); // ?

    /**
     * @param moduleName
     * @param className
     * @return the assembly
     * @throws app.packed.application.BuildException
     *             if a matching assembly could not be found.
     * @throws UnsupportedOperationException
     *             if this finder operates on the classpath
     */
    Assembly assembly(String moduleName, String className);

    // default mode er fra Assembly.getModule==Unamanaged ? classpath : modulepath
    enum Mode {
        SEARCH_CLASSPATH, SEARCH_MODULEPATH;
    }
}
// Skal ogsaa kunne bruges i standalone mode.
// Her er det vel primaert en enkelt Assembly men leder efter
