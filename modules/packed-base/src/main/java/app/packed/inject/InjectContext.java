package app.packed.inject;

import java.util.Set;

import app.packed.lang.Key;

//Er det bare en Sidecar der tager en InjectionRuntime+ComponentContext???
// ComponentContext... Skal ogsaa styres via Access.. @InjectContext(needs ComponentContext)...
// Naah, component burde da vaere fint... Den kan alle faa

//Nej den er speciel... Alle kan supportere den paa en eller anden maade...

// Man laver vel en skabelon naar man gerne vil finde bruge den...
public interface InjectContext {

    /**
     * The set of keys for which services are available.
     * 
     * @return set of keys for which services are available
     */
    Set<Key<?>> keys();

    /**
     * Returns the class that determines the visibility. For example, a runtime might provide 10 services to an inject
     * target. However, because it is placed in another module (or its package private). Only 5 of those services will be
     * available for injection.
     * 
     * @return the class for which we calculate visibility
     */
    Class<?> visibility();
}

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