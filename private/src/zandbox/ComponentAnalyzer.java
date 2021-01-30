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
package zandbox;

import app.packed.component.ComponentModifier;
import app.packed.component.Wirelet;

/**
 * Analysis servers 2 main purposes
 * 
 * Verification
 * 
 * Reporting
 *
 */
// Taenker 2 hovedformaal

// Validere
// Extract Information from a system

// Currently we do not take any kind of wirelets...

// Skal vi have wirelets er spoergsmaalet
// Svaret er det kan give mening naar vi vil analysere...
// F.eks. AssemblyWirelets... printStuff()

// Det eneste problem er den option...
// Ellers smider vi den paa ComponentSystem...

// Hmm, efter vi er begyndt lidt at flytte dem ud...
// Saa er det maaske primaert en builder a.la. cli.Main

final class ComponentAnalyzer {

    // Maybe on Component???
//    public static Component analyze(ComponentSystem s) {
//        return PackedBuildContext.forAnalysis(s);
//    }

//    /**
//     * @param s
//     *            the system to test
//     * @return the component
//     * @throws IllegalStateException
//     *             if the system is not in an assembled state.
//     */
//    public static Component analyzeAssembly(ComponentSystem s) {
//        
//        // App.print(sdfsdf)
//        // App.toDoc().print();
//        // ??
//        throw new UnsupportedOperationException();
//    }

//    public static Optional<Component> findExtension(ComponentSystem s, Attribute<?> attribute) {
//        return ComponentStream.of(s).filter(c -> c.attributes().isPresent(attribute)).findAny();
//    }

//    public static void print(ComponentSystem s) {
//        // Er det i virkeligheden bare et streaming hirakisk dokument???
//        // Det tror jeg...
//        // Vil du have det statisk view???
//        // Eller et dynamisk view???
//        //
//        
//        ComponentSystem.forEach(s, c -> System.out.println(c.path() + " " + c.modifiers() + " " + c.attributes()));
//    }

//    static void validate(ComponentSystem s, Object ruleset) {
//
//    }

    // require runtime
    // require assembly time (maybe as options...)
    // I don't know if we want an option
    
    // MAKE TO WIRELETS I THINK
    static class AnalyzerWirelets {
        // Omvendt kan det jo vaere en steam option
        static Wirelet includeEnvironment(ComponentModifier modifier) {
            throw new UnsupportedOperationException();
        }

        static Wirelet requireAssembly() {
            throw new UnsupportedOperationException();
        }

        // Ideen er lidt, at det feks ikke giver mening at lave en
        // container analyse paa andet en containere...

        // Should throw IAE if modifiers does not match
        static Wirelet requireModifier(ComponentModifier modifier) {
            throw new UnsupportedOperationException();
        }

        // Det er primaert taenkt at vi f.eks. gerne transformere et stort system
        // til en enkelt container. Hvor alle referencer er EXTERNAL/ENVIRONMENT
        static Wirelet requireRuntime() {
            throw new UnsupportedOperationException();
        }
    }
}
//