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
package app.packed.component;

import java.util.Optional;

import app.packed.base.Attribute;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedAssemblyContext;
import packed.internal.component.PackedComponentModifierSet;

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

// map

// Skal vi have wirelets er spoergsmaalet
// Svaret er det kan give mening naar vi vil analysere...
// F.eks. AssemblyWirelets... printStuff()

public final class ComponentAnalyzer {

    // Maybe on Component???

    // If the specified system is a bundle. The bundle will be consumed
    // And ComponentModifier.Analysis will be set

    public static ComponentStream stream(ComponentSystem s) {
        // Should we take options??? Again I don't know if
        // options is more a system transform.
        return analyze(s).stream();
    }

    public static Component analyze(ComponentSystem s) {
        if (s instanceof Component) {
            return (Component) s;
        } else if (s instanceof ComponentDelegate) {
            return ((ComponentDelegate) s).component();
        } else {
            ComponentNodeConfiguration node = PackedAssemblyContext.assemble(PackedComponentModifierSet.I_ANALYSIS, (Bundle<?>) s, null);
            return node.adaptToComponent();
        }
    }

    public static Optional<Component> findExtension(ComponentSystem s, Attribute<?> attribute) {
        return stream(s).filter(c -> c.attributes().isPresent(attribute)).findAny();
    }

    /**
     * @param s
     *            the system to test
     * @return the component
     * @throws IllegalStateException
     *             if the system is not in an assembled state.
     */
    public static Component analyzeAssembly(ComponentSystem s) {
        throw new UnsupportedOperationException();
    }

    static void validate(ComponentSystem s, Object ruleset) {

    }

    // require runtime
    // require assembly time (maybe as options...)
    // I don't know if we want an option
    interface Option {
        static Option requireAssembly() {
            throw new UnsupportedOperationException();
        }

        static Option requireRuntime() {
            throw new UnsupportedOperationException();
        }

        // Ideen er lidt, at det feks ikke giver mening at lave en
        // container analyse paa andet en containere...

        // Should throw IAE if modifiers does not match
        static Option requireModifier(ComponentModifier modifier) {
            throw new UnsupportedOperationException();
        }

        // Det er primaert taenkt at vi f.eks. gerne transformere et stort system
        // til en enkelt container. Hvor alle referencer er EXTERNAL/ENVIRONMENT

        // Omvendt kan det jo vaere en steam option
        static Option includeEnvironment(ComponentModifier modifier) {
            throw new UnsupportedOperationException();
        }
    }
}
//