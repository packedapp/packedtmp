package app.packed.inject.service;

import java.util.Set;

import app.packed.base.Key;
import app.packed.component.ComponentMirror;
import app.packed.inject.InjectionSiteMirror;
import app.packed.mirror.Mirror;

// extends ComponentFeatureMirror???
/** A mirror of a service. */
// ServicePointMirror <- place where a
public interface ServiceMirror extends Mirror {

    // annotation, config method
    Object configSite(); // or ConfigMirror...

    Set<ServiceMirror> dependencies();

    // export()
    int id(); // Ideen var at kunne sammenligne services, der blot var exporteret...

    /** {@return the key of the service.} */
    Key<?> key();

    /** {@return the component that provides the service.} */
    ComponentMirror providedBy();

    // Taenker
    // Set<ProvidedService> providedTo();
    Set<InjectionSiteMirror> usedBy();

    Set<ComponentMirror> usedByComponents();
}
// Skal vi have et id???

// Noget med injection
// Noget om den er cached
// Noget om hvor den kommer fra
// Altsaa der kan jo vare en chain af exports
// Dependencies som ikke er for samme container
// Depth +1 -> "imports"