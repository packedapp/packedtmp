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
package sandbox.lifetime2.old;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.build.BuildException;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;

/**
 * Wirelets that can only be specified when building an application.
 * <p>
 * Attempts to use them when {@link ApplicationLauncher#launch(Wirelet...) launching} an image will fail with
 * {@link IllegalArgumentException}.
 */
// Jeg tror den doer og ryger over paa andre wirelets... Saa vi udelukkende gruppere dem efter type... IDK... Maaske beholder vi den alligevel...
//
public final class BuildWirelets {

    /** Not for you my friend. */
    private BuildWirelets() {}

    // Only shows interesting stack frames when failing
    // This means we need to invocations for AssemblySetup.build
    // And filter the stack frames
    static Wirelet compressedBuildtimeExceptions() {
        throw new UnsupportedOperationException();
    }

    public static Wirelet alwaysThrowBuildException() {
        return onExceptionInBuild(e -> {
            if (e instanceof BuildException be) {
                throw be;
            }
            throw new BuildException(e.getMessage(), e);
        });
    }

    public static Wirelet onExceptionInBuild(Consumer<Exception> handler) {
        // consumer must throw,
        // handler.consumer()
        // throw new BuildException("handler did not throw");
        throw new UnsupportedOperationException();
    }

    public static Wirelet disableRuntimeWirelets() {
        // En optimering der goer at vi ved der aldrig kommer nogle runtime wirelets
        // Taenker kun den er brugbar for saadan noget som Session der skal spawne hurtigt
        // Men altsaa hvad fx med navn

        // driver.named()
        throw new UnsupportedOperationException();
    }

    // if (ic.isRestarting()-> ServiceWirelets.Provide("Cool") : ServiceWirelets.Provide("FirstRun)
    static Wirelet delayToRuntime(Function<InstantiationContext, app.packed.container.Wirelet> wireletSupplier) {
        throw new UnsupportedOperationException();
    }

    static Wirelet delayToRuntime(Supplier<Wirelet> wireletSupplier) {
        throw new UnsupportedOperationException();
    }

    // Ideen er lidt at wireletten, ikke bliver processeret som en del af builded
    static Wirelet delayToRuntime(Wirelet wirelet) {
        throw new UnsupportedOperationException();
    }

    // Maaske er det en build wirelet???
    // Taenker
    // illegal, prohibit, restrict

    @SafeVarargs
    public static Wirelet forbidExtensions(Function<? super Class<? extends Extension<?>>, BuildException> throwMe,
            Class<? extends Extension<?>>... extensionTypes) {
        throw new UnsupportedOperationException();
    }

//    /**
//     * Returns a 'spying' wirelet that will perform the specified action every time a component has been wired.
//     * <p>
//     * If you only want to spy on specific types of components you can use {@link #spyOnWire(Class, Consumer)} instead.
//     *
//     * @param action
//     *            the action to perform
//     * @return the wirelet
//     */
//    public static Wirelet spyOnWire(Consumer<? super ComponentMirror> action) {
//        return new InternalWirelet.OnWireActionWirelet(action, null);
//    }
//
//    public static <T extends ComponentMirror> Wirelet spyOnWire(Class<T> mirrorType, Consumer<? super T> action) {
//        requireNonNull(mirrorType, "mirrorType is null");
//        requireNonNull(action, "action is null");
//        return spyOnWire(m -> {
//            if (mirrorType.isInstance(m)) {
//                action.accept(mirrorType.cast(m));
//            }
//        });
//    }

    // Ved ikke om scope er noedvendigt...
    // Det er jo udelukkende for test scenarier... Saa kan ikke rigtig
    // Hmmm, hvad med applikation hosts... Beholder vi den der???
//    static Wirelet spyOnWire(Consumer<? super ComponentMirror> action, ComponentScope scope) {
//        requireNonNull(scope, "scope is null");
//        return new InternalWirelet.OnWireActionWirelet(action, scope);
//    }

    interface InstantiationContext {}
}

class SandboxBuild {
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
//// Problemet med en wirelet og ikke en metode er at vi ikke beregne ApplicationBuildKind foerend
//// vi har processeret alle wirelets
//// Alternativ paa Driveren -> Fungere daarlig naar vi har child apps
//// eller selvstaendig metode -> Er nok den bedste
//@Deprecated
//static Wirelet reusableImage() {
//  throw new UnsupportedOperationException();
//}
//Wirelet assemblyTimeOnly(Wirelet w); Hmmm idk if useful
// Interface with only static methods are