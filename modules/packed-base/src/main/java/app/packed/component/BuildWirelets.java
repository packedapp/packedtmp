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

/**
 * A set of wirelets that can be specified at build-time only. Attempts to use them on an pre-generated image will
 * result in an {@link IllegalStateException} being thrown.
 */
// Er ikke sikker paa ISE, vi maa generelt smide den samme exception for alle misplaced wirelets...
// Og der passer IAE altsaa bedre paa de fleste

//Features
// Debuggin
// We could have app.packed.build package.

// Taenker de kan bruges paa alle componenter. Og ikke kun rod..
// Eller??? Skal de fx. cachnes af en host???
// Saa nye guests ogsaa skal kunne overtage dem??
// Det samme gaelder med NameSpaceRules

// De her regner gaelder for build'et...

// Return InheritableWirelet().

// Wirelet printDebug().inherit();
// printDebug().inherit();
public final class BuildWirelets {

    /** Not for you my friend. */
    private BuildWirelets() {}
    // NO FAIL <--- maaske brugbart for analyse

    // fail on warnings.

    // Throw XX exception instead of

    // Additional to people overridding artifacts, bundles, ect.
    public static InheritableWirelet checkRuleset(Object... ruleset) {
        throw new UnsupportedOperationException();
    }

    // Taenker vi printer dem...
    // Og er det kun roden der kan disable dem???
    public static InheritableWirelet disableWarnings() {
        throw new UnsupportedOperationException();
    }

    // Because it is just much easier than fiddling with loggers
    /**
     * A wirelet that will print various build information to {@code system.out}.
     * 
     * @return the wirelet
     */
    public static InheritableWirelet printDebug() {
        throw new UnsupportedOperationException();
    }

    // NoBootstrapCaching...
    // BootstrapWirelets.noBootstrap()
    public static Wirelet sidecarCacheLess() {
        throw new UnsupportedOperationException();
    }

    static InheritableWirelet sidecarCacheSpecific() {
        // The wirelet itself contains the cache...
        // And can be reused (also concurrently)
        // Maaske kan man styre noget reload praecist...
        // Maaske kan man optionalt tage en Runnable?
        // Som brugere kan invoke
        throw new UnsupportedOperationException();
    }

    // Disable Host <--- Nej, det er et ruleset....
}
//Wirelet assemblyTimeOnly(Wirelet w); Hmmm idk if useful
/// Interface with only static methods are