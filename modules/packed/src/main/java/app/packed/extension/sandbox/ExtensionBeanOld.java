package app.packed.extension.sandbox;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionSupport;

//// 3 maader at installere den paa

// always      | Whenever the extension is used.. The extension bean is always added
// on-demand   | Somebody depends on it either directly or indirectly (for example via an extensor)
// explicit    | Explicitly wired via a method on Extension

//// Wirelets giver kun mening explicit... Evt. kan man installere nogen statiske??? nahhh
//// Wirelets er nok primaert en bruger ting, og det er primaert ogsaa en container ting
//// Det er nok snare reglen end undtagelsen... at man wire almindelige componenter

// Taenker den opfoere sig som enhver anden component...
// Og det primaert 

// Always a singleton... container lifecycle
/**
 * 
 * 
 * Typically a runtime extension will have the extension injected.
 * <p>
 * Extension runtimes are always initialized and started in application extension order...
 * total order in application I would think...
 * 
 * <p>
 * Extension runtimes must be defined in the same module as the extension. Failure to do so will result in an
 * {@link InternalExtensionException} being thrown.
 */
// Can have other runtime extensions injected which are typically injected from
// parent containers.

// Extensions can install a runtime that will take care of the extensions runtime needs

// This class is needed to identify objects that are injected into hooks at runtime.

// E
// Was ExtensionRuntime

// https://www.thefreedictionary.com/words-containing-exten

// Lifecycle wise saa tror jeg vi bruger application-wide extension-order
// application lifetime 
// Can use some hooks.. start, stop, ect..

// Single constructor...
// Inject??? Nahhh

/// ----- SystemExtenstor -----
// Vi slipper ikke udenom ref count hvis vi har en SystemExtensor...
// PGA af classLoading...

// Men maaske en BuildExtensor...

/**
 * Extensors are runtime representations of {@link Extension extensions}.
 * <p>
 * 
 */
// Extensors must
//* Be located in the same module as the extension itself
// * have a single constructor

// If DI-> Can inject extensors that are visible. And for same extension

// Den her klasse giver ikke rigtig mening, hvis vi har on-demand... Eller always for den sags skyld
/**
 *
 */
// TreeBean
public abstract class ExtensionBeanOld {

    // Den her gaa paa hooks paa extensor klassen...
    /// Syntes ogsaa vi skal have noget lazy paa Extension... f.eks. installer kun denne extensor hvis JFR Extensionen er i
    // containeren...
    /// Eller paa et andet tidspunkt i traet???? F.eks. Hvis vi nu linker en specific extension (web) container... Saa vil
    // vi jo gerne enable JFR
    // hvis det er noedvendigt... Men det kan vi jo kun se paa ved at kigge paa en parent...
    //// Paa en eller anden maade skal vi kunne tree searche...
    //// Maaske have en specific "enabled" swith for hver extension... der kan kigge opad... Ved ikke om det skal vaere en
    // klasse/metode ect
    //// $extensionSupportsEnabling() look for any parent in the tree
    //// Problemet er jo at use() automatisk enabler en extension

    @SuppressWarnings("unchecked")
    static void $ignoreHooksIfExtensionisNotUsed(Class<? extends Extension>... extensionTypes) {
        // Vi kan bruge f.eks. @ExposeJFR paa en mode... Men den autoaktivere ikke extension'en
        // De bliver kun brugt
        throw new UnsupportedOperationException();
    }

    // Maaske har vi ikke et deciseret flag?? Man
    /// Kan vi lave det generisk saa vi baade bruger det paa build-time, run-time og i user code?
    static abstract class ExtensionFeatureFlag {
        // taenker man kan faa injected parents.
        // void $connectVia(ddd)

        // Der er saadan noget som evaluation time...
        // static - dynamic (
        // https://martinfowler.com/articles/feature-toggles.html
    }

    static class MyExt extends Extension {

        class Sub extends ExtensionSupport {
            // Den kan f.eks. overskrives via wirelets?
            //// ExtensionWirelets.disableFeature(ENABLED)?
            public static ExtensionFeatureFlag ENABLED = new ExtensionFeatureFlag() {};
        }
    }

    static class OtherExt extends Extension {
        // conditionalInstallExtensor(MyExt.Sub.ENABLED, MyJFRExtensor.class);
    }
}

//--------------- Hvor mange kan man have?
// Saa mange man har lyst til...
// Saa kan man jo optional injecte dem

//Maaaske stopper vi application code foer extension code

//---------------- Abstract vs Interface
// ... Det er fint den er abstract
// Bliver der maaske lavet 10.000 extensions i alt i Packeds livstid..