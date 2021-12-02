package app.packed.inject.service;

import java.util.Set;

import app.packed.bean.member.operation.examples.ServiceProvideMirror;
import app.packed.component.ComponentMirror;
import app.packed.inject.mirror.InjectionSite;

// extends ComponentFeatureMirror???
/** A mirror of a service. */
// ServicePointMirror <- place where a
public interface ServiceProvideOldMirror extends ServiceProvideMirror {

    // annotation, config method
    Object configSite(); // or ConfigMirror...

    Set<ServiceProvideOldMirror> dependencies();

    // export()
    int id(); // Ideen var at kunne sammenligne services, der blot var exporteret...

    /** {@return the component that provides the service.} */
    ComponentMirror providedBy();

    // Taenker
    // Set<ProvidedService> providedTo();
    Set<InjectionSite> usedBy();

    Set<ComponentMirror> usedByComponents();
}
// Skal vi have et id???

// Noget med injection
// Noget om den er cached
// Noget om hvor den kommer fra
// Altsaa der kan jo vare en chain af exports
// Dependencies som ikke er for samme container
// Depth +1 -> "imports"