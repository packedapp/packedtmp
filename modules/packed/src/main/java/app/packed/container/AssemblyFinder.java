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

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.function.Consumer;

/**
 *
 */
// Mode : Standalone or from Assembly
// PathMode : Classpath, Modulepath
// cardinality : findAny, findOne, findAll

// Delt op i tre.
//// 1. Hvor kigger vi
//// 2. Filtre
//// 3. En terminal operation



// Where to Look
//// Paths

// Filters (filterOnType, filterOnModuleName)
//// type/moduleName/className

// Terminals
//// find one/all
//// link one/all
//// forEach


// Future
//// Taenk over hvordan man maaske vil kunne supportere mere komplekse layouts i fremtiden
//// Eller det er maaske JLink images?
public interface AssemblyFinder {

    AssemblyFinder addModuleLayer(ModuleLayer moduleLayer);

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

    // finders can ikke laengere modificeres
    default AssemblyFinder compose(AssemblyFinder... finders) {
        throw new UnsupportedOperationException();
    }

    Assembly findOne(ServiceLoader<? super Assembly> loader);

    Assembly findOne(String className);

    /**
     * @param moduleName
     * @param className
     * @return the assembly
     * @throws app.packed.application.BuildException
     *             if a matching assembly could not be found.
     * @throws UnsupportedOperationException
     *             if this finder operates on the classpath
     */
    Assembly findOne(String moduleName, String className);

    void forEach(Consumer<? super Assembly> action);

    default void linkOne(String moduleName, String className, Wirelet... wirelets) {

    }

    /**
     * Links all matching assemblies by calling {@link app.packed.extension.BaseExtension#link(Assembly, Wirelet...)} for
     * every assembly.
     *
     * @param wirelets
     *            optional wirelets to apply for each assembly
     *
     * @throws UnsupportedOperationException
     *             if used in stand-alone mode.
     */
    void linkAll(Wirelet... wirelets);

    AssemblyFinder paths(Path... paths);

    default AssemblyFinder paths(String... paths) {
        for (String s : paths) {
            paths(Path.of(s));
        }
        return this;
    }

    static AssemblyFinder onClassPath() {
        throw new UnsupportedOperationException();
    }

    // Looks on System
    static AssemblyFinder onModulePath() {
        throw new UnsupportedOperationException();
    }

    static AssemblyFinder onModulePath(MethodHandles.Lookup caller) {
        throw new UnsupportedOperationException();
    }

    // default mode er fra Assembly.getModule==Unamanaged ? classpath : modulepath
    enum Mode {
        SEARCH_CLASSPATH, SEARCH_MODULEPATH;
    }
}
// Skal ogsaa kunne bruges i standalone mode.
// Her er det vel primaert en enkelt Assembly men leder efter
