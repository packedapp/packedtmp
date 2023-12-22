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
import java.util.stream.Stream;

/**
 * An assembly finder can be used to find one or more assemblies on the class- or module-path.
 * <p>
 * Probably 3 ways to do this
 *
 * Use a ServiceLoader
 *
 * Use this class
 *
 * There is some module layer thingies we need to think up
 *
 *
 * <p>
 *
 *
 * What about service loaders?
 *
 */
// Jeg tror ikke man kan skifte mode...
// Module paths kan aldrig laese class paths.
// Og classpath er ikke interesseret i at lase module paths

// Man kan ikke filtrere

// Mode : Standalone or from Assembly
// PathMode : Classpath, Modulepath (if source assembly

// cardinality : findAny, findOne, findAll

// kind: Static vs Dynamic (Dynamic meaning deployiesh)

/// Stoerste problem er Classpath vs Modulepath. Kan vi klare os med en klasse?
// Og man har jo fx /lib1 , lib2, hvor man gerne vil loade alle jars i lib1
// ind i det samme moduleLayer, hvilket hmm ikke er lige noget vi kan.
// Og jo som saadan ikke har noget med assembly finder at goere
// Maaske en Seperate ModuleLayout klasse?
// Vi kan ikke klare os med en ihvertfald

// Tror det er en god demo. Men hmm, tror vi skal noget andet paa lang sigt...

// Was Delt op i tre.
//// 1. Hvor kigger vi
//// 2. Filtre
//// 3. En terminal operation

// Now
//// Where to look
//// What to find (terminal)

// Where to Look
//// Paths

// Filters (filterOnType, filterOnModuleName)
//// type/moduleName/className

// Terminals
//// find one/all

// Future
//// Taenk over hvordan man maaske vil kunne supportere mere komplekse layouts i fremtiden
//// Eller det er maaske JLink images?
public sealed interface AssemblyFinder permits PackedAssemblyFinder {

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
    // Compose with
    default AssemblyFinder compose(AssemblyFinder... finders) {
        throw new UnsupportedOperationException();
    }

    default <T extends Assembly> Stream<T> findAll(ServiceLoader<T> loader) {
        throw new UnsupportedOperationException();
    }

    // Find exactly one
    // Fails if there are more than one
    <T extends Assembly> T findOne(ServiceLoader<T> loader);

    /**
     * Find and instantiates an assembly with the specified name.
     *
     * @param className
     *            the canonical name of the assembly
     * @return an instance of the assembly
     *
     * @throws BuildException
     *             if an assembly with the specified name was not present or could not be instantiated
     */
    // findOneNamed
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

    AssemblyFinder paths(Path... paths);

    default AssemblyFinder paths(String... paths) {
        for (String s : paths) {
            paths(Path.of(s));
        }
        return this;
    }

    /** {@return an assembly finder that uses the classpath to find assemblies} */
    static AssemblyFinder onClasspath() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return an assembly finder that uses the modulepath to find assemblies.}
     *
     * @param caller
     *            a lookup object that is used for visibility and for instantiating assembly instances
     */
    static AssemblyFinder onModulepath(MethodHandles.Lookup caller) {
        throw new UnsupportedOperationException();
    }

    /**
     * An assembly finder can be used to find assemblies either on the class or module path. But not both.
     */
    // I think just have two is methods(), isClassPath, isModulePath
    enum Mode {
        SEARCH_CLASSPATH, SEARCH_MODULEPATH;
    }
}
// Skal ogsaa kunne bruges i standalone mode.
// Her er det vel primaert en enkelt Assembly men leder efter

// default mode er fra Assembly.getModule==Unamanaged ? classpath : modulepath
interface Zarchive {

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
    // Vi supportere ikke linking direkte...
    // findAll().foreach(e->link(e));
    void linkAll(Wirelet... wirelets);

    default void linkOne(String moduleName, String className, Wirelet... wirelets) {

    }
}