package app.packed.inject.service;

import java.util.Set;

import app.packed.component.ComponentMirror;
import app.packed.operation.mirror.ig.InjectionSite;
import app.packed.operation.usage.ServiceProvideMirror;

// extends ComponentFeatureMirror???
/** A mirror of a service. */
// ServicePointMirror <- place where a
public abstract class ServiceProvideOldMirror extends ServiceProvideMirror {

    // annotation, config method
    public abstract Object configSite(); // or ConfigMirror...

    public abstract Set<ServiceProvideOldMirror> realDependencies();

    // export()
    public abstract int id(); // Ideen var at kunne sammenligne services, der blot var exporteret...

    /** {@return the component that provides the service.} */
    public abstract ComponentMirror providedBy();

    // Taenker
    // Set<ProvidedService> providedTo();
    public abstract Set<InjectionSite> usedBy();

    public abstract Set<ComponentMirror> usedByComponents();
}
// Skal vi have et id???

// Noget med injection
// Noget om den er cached
// Noget om hvor den kommer fra
// Altsaa der kan jo vare en chain af exports
// Dependencies som ikke er for samme container
// Depth +1 -> "imports"