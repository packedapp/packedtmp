package app.packed.applicationold;

import app.packed.container.Bundle;

// Den er ikke mandatory...
// Men kan maaske 
abstract class AbstractApplication {

    // eller maaske maa man bare lave det fucking cast...
    // Maaske kan vi lave noget magisk ved at sige JobCompleter<T> som dependency...
    // Og saa regne baglaens fra assemblien...
    // Nahhh, det tror jeg ikke rigtig vi kan application!=assembly
    protected static void $extractJobFromTypeParameterOf(Class<? extends Bundle<?>> assemblyType, int parameterIndex) {
        
    }
}
