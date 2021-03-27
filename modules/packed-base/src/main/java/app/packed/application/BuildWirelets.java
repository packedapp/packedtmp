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
package app.packed.application;

import java.util.function.Consumer;

import app.packed.component.Component;
import app.packed.component.ComponentScope;
import app.packed.component.Wirelet;
import packed.internal.component.InternalWirelet;

/**
 * A set of wirelets that can be specified at build-time only. Attempts to use them on an {@link ApplicationImage} will
 * result in an extension being thrown.
 * <p>
 */
public final class BuildWirelets {
    // Features
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
    /** Not for you my friend. */
    private BuildWirelets() {}

    // Additional to people overridding artifacts, assemblies, ect.
    public static Wirelet checkRuleset(Object... ruleset) {
        throw new UnsupportedOperationException();
    }

    // NO FAIL <--- maaske brugbart for analyse

    // fail on warnings.

    // Throw XX exception instead of

    // Taenker vi printer dem...
    // Og er det kun roden der kan disable dem???
    public static Wirelet disableWarnings() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that will perform the specified action every time a component has been wired.
     * <p>
     * 
     * @param action
     *            the action to perform
     * @return the wirelet
     */
    public static Wirelet onWire(Consumer<? super Component> action) {
        return new InternalWirelet.OnWireActionWirelet(action);
    }

    public static Wirelet onWire(Consumer<? super Component> action, ComponentScope scope) {
        return new InternalWirelet.OnWireActionWirelet(action);
    }
    
    // Because it is just much easier than fiddling with loggers
    /**
     * A wirelet that will print various build information to {@code system.out}.
     * 
     * @return the wirelet
     */
    // Maaske til DevWirelets
    public static Wirelet printDebug() {
        throw new UnsupportedOperationException();
    }

    // NoBootstrapCaching...
    // BootstrapWirelets.noBootstrap()
    public static Wirelet sidecarCacheLess() {
        throw new UnsupportedOperationException();
    }

    static Wirelet sidecarCacheSpecific() {
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