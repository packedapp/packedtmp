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
package app.packed.lifetime.sandbox;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.operation.OperationHandle;
import app.packed.operation.Op;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationMirror;

/** An extension point for {@link EntryPointExtension}. */

// Der er en single faetter. Koere bare altid mig

// Der flere, hvor vi selecter dem fra en Wirelet

// Nogen andre kalder ind direkte via en Operation
public class OldEntryPointExtensionPoint extends ExtensionPoint<BaseExtension> {

    /** No entry. */
    OldEntryPointExtensionPoint() {}

//    /**
//     * {@return the extension that is managing the
//     */
//    public Optional<Class<? extends Extension<?>>> dispatcher() {
//        return Optional.ofNullable(extension().shared.dispatcher);
//    }

    /**
     * @param beanOperation
     * @return the entry point id
     *
     * @throws
     * @throws BuildException
     *             if another extension is already managing end points
     */
    // BuildException -> Altsaa tit er jo fordi brugeren har brugt annoteringer
    // for 2 forskellige extensions

    // return mirror?

    // Hvordan dispatcher vi videre til extensionen?
    // Vi kan registere en MethodHandle + Bean
    // Sige Main fungere som dobbelt
    // Callable er skidt syntes jeg
    // Skal vi have noget generelt dispatch teknologi?
    // Nå nej vi har jo entry point selectoren...
//    public int registerEntryPoint(Class<?> hook) {
//        return extension().shared.takeOver(extension(), usedBy());// .registerEntryPoint(usedBy(), isMain);
//    }

    public OperationHandle specializeMirror(OperationHandle configuration, int id, Supplier<? extends OperationMirror> supplier) {
        // Ved ikke lige helt hvordan den skal fungere
        return configuration;
    }

    // If you have more than 1 entry point you must register one
    // You c
    // Kunne godt taenke mig en anden mechanism. Hvor vi wrapper en wirelet i en anden
    // fx Wirelet EPEP.bootstrapWirelet(MyExtension, SomeClassInSamePackage);
    // Function<T, Wirelet> bootstrapper();
    public <W extends Wirelet> void registerSelector(Class<W> wireletKind, ToIntFunction<W> selector) {}
}
class Zandboxx {

    public void setShutdownStrategy(Supplier<Throwable> maker) {
        // Ideen er lidt at sige hvad sker der paa runtime.
        // Hvis vi shutter down... Default er at der bliver
        // Sat en cancellation exception som reaason
        // CancellationException
        // Men ved ikke om vi skal have en special exception istedet for???
        // En checked istedet ligesom TimeoutException
    }

    // installMain
    // lazyMain()?
    <T extends Runnable> InstanceBeanConfiguration<?> installMain(Class<T> beanClass) {
        // IDK, skal vi vente med at tilfoeje dem
        throw new UnsupportedOperationException();
    }

    <T extends Runnable> InstanceBeanConfiguration<?> installMainInstance(T beanInstance) {
        throw new UnsupportedOperationException();
    }

    // Laver en ny bean. Med
    public OperationConfiguration entryPointMain(Op<?> operation) {
        throw new UnsupportedOperationException();
    }
}
//
//// Kan bare tage en ToIntFunction
//public interface EntryPointSelection<W extends Wirelet> {
//    int entryPoint(W wirelet);
//}
//
///**
// * Selects
// *
// * <p>
// * If an extension that is not the managing extension. Attempts to have an instance of this interface injected. The
// * build will fail with an {@link InternalExtensionException}.
// */
//// Behoever kun blive brugt hvis man har mere end et EntryPoint
//// Maaske tager man evt. bare det foerste entry point som default
//// hvis der ikke blive sat noget
//
//// @AutoService
//// Kan injectes i enhver bean som er owner = managedBy...
//// For andre beans smider man InjectionException?
//
//public interface EntryPointSelector {
//
//    /**
//     * @param id
//     *            the id of the entry point that should be invoked
//     * @throws IllegalArgumentException
//     *             if no entry point with the specified id exists
//     * @throws IllegalStateException
//     *             if the method is invoked more than once
//     * @see EntryPointMirror#id()
//     */
//    void selectEntryPoint(int id);
//}
// Ideen er at man kan wrappe sin entrypoint wirelet..
// Eller hva...
// Du faar CLI.wirelet ind som kan noget med sine hooks
//static Wirelet wrap(Wirelet w) {
//  return w;
//}
