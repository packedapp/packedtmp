package app.packed.inject;

import java.util.Set;

import app.packed.lang.Key;

/**
 * A context object that be injected into a method or constructor to query about what other dependencies are available
 * for injection at the particular use site.
 * <p>
 * May change from type to type even when registered in the same container.
 * <p>
 * we both need to check the type and the qualifier which must be readable by #visibility
 * <p>
 */
public interface InjectContext {

    /**
     * An immutable set of keys that are available for injection at the use site (typically a method or a constructor).
     * <p>
     * The set of keys that are available for injection will never change during the lifetime of an object.
     * 
     * 
     * @return set of keys that are available for injection at the use site
     */
    Set<Key<?>> keys();

    /**
     * Returns the class that determines the visibility. For example, a runtime might provide 10 services to an inject
     * target. However, because it is placed in another module (or its package private). Only 5 of those services will be
     * available for injection.
     * 
     * @return the class for which we calculate visibility
     */
    Class<?> visibility(); // Det er jo bare .getClass(); Med mindre det er en Composite...
}
// Set<Class<? extends Annotation>> Factory.primers()
//// Primers can be resolved in relationship to a container
//// Primers can use normal optional/nullable/composite stuff... or they can ignore it... The contract is really
// A textual one, and not a programmatic one....
// Maybe you can opt.. support Optional, Nullable, NotLazy, Provider, Not Composite
// Paa PrimeProvider
// No validation is performed because we can always override the default.... So there is just no way to do it....

// So Factory -> Set<Key<?>> requirements, Set<Key<?>> optionalRequirements, Set<Class<?>> primers()... 

/// ---------------
// Map<String, String> filtered(); A map of all filtered types.... For example, because there are not visible...
// Value is description...

//Er det bare en Sidecar der tager en InjectionRuntime+ComponentContext???
//ComponentContext... Skal ogsaa styres via Access.. @InjectContext(needs ComponentContext)...
//Naah, component burde da vaere fint... Den kan alle faa

//Nej den er speciel... Alle kan supportere den paa en eller anden maade...

//Man laver vel en skabelon naar man gerne vil finde bruge den...

//Vi ved ikke noget om primeannotation

//Inject context kender ikke noget til prime annotations.
//De styrer deres eget loeb, men det anbefales at de overholder f.eks. @Nullable

//En InjectContext vil aldrig dukke op som et requirement
// Can we cheat with Composite here? I think maybe....
// Not first priority though...
//// Det ville ikke fungere super godt hvis vi bruger en sidecar...

/////
// Altsaa der er jo nogen gange hvor ting ikke er tilgaengelig for en constructor. Men kun en metode??

//@Stuff Set<Key<?>> thisIsThing
//Provided via @Stuff + InjectionContext -> ic.keys();
///// -----------
// Vi kunne jo tit godt taenke os at fortaelle en historie...
// Du kan bruge den her service fordi du er annoteret med @Actor...
//// ConfigSite med @Actor som parent???