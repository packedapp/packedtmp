package app.packed.container;

// Taenker den opfoere sig som enhver anden component...
// Og det primaert 

// Always a singleton... container lifecycle
/**
 * Extensions typically needs some kind of.
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

/**
 * A runtime representation of {@link Extension extensions}.
 * <p>
 * 
 */
public abstract class Extensor<E extends Extension> {

    static {

    }
}

//--------------- Hvor mange kan man have?
// Saa mange man har lyst til...
// Saa kan man jo optional injecte dem

//Maaaske stopper vi application code foer extension code

//---------------- Abstract vs Interface
// ... Det er fint den er abstract
// Bliver der maaske lavet 10.000 extensions i alt i Packeds livstid..