package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.component.Assembly;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.container.ExtensionMirror;
import packed.internal.service.ServiceManagerSetup;

/**
 * A specialized extension mirror for {@link ServiceExtension}.
 * 
 * @see ContainerMirror#extensions()
 * @see ContainerMirror#useExtension(Class)
 */
public final class ServiceExtensionMirror extends ExtensionMirror<ServiceExtension> {

    /** The service manager */
    private final ServiceManagerSetup services;

    ServiceExtensionMirror(ServiceManagerSetup services) {
        this.services = requireNonNull(services);
    }

    /**
     * Returns the service contract for the container.
     * 
     * @return the service contract for the container
     * @throws IllegalStateException
     *             if dependencies have not been resolved
     */
    // Den fungere ikke hvis vi ikke har resolvet alle services.
    // Fordi vi ved jo ikke om en required service fx bliver provided af et link af en container
    // senere hen

    // Alternativet er kun at have
    public ServiceContract contract() {
        return services.newServiceContract();
    }

    // Detaljeret info, ogsaa med dependency graph som kan extractes...
    // Hvad skal vi returnere???

    // ServiceRegister, hvor hver service har specielle attributer??
    // Et Map af <Key, ServiceMirror> (Helt sikkert service mirror)
    // MapView<Key<?>, ServiceMirror>

    // or contract.keys()
    public Set<Key<?>> exportedKeys() {
        return contract().provides();
    }

    // Map<K, V> resolved
    // Map<K, V> unresolvedOptional?();

    /** { @return a map view of all the services that are exported from the container.} */
    public Map<Key<?>, ServiceMirror> exports() {
        throw new UnsupportedOperationException();
    }

    public static Optional<ServiceExtensionMirror> find(Assembly<?> assembly, Wirelet... wirelets) {
        return ContainerMirror.of(assembly, wirelets).findExtension(ServiceExtensionMirror.class);
    }

    public static ServiceExtensionMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return ContainerMirror.of(assembly, wirelets).useExtension(ServiceExtensionMirror.class);
    }
}

///**
// * Returns a mirror for the specified extension.
// * 
// * @param extension
// *            the extension to return a mirror for
// * @return the mirror
// */
//// Altsaa den her giver ingen mening...
//// Kan lige saa godt putte mirror paa selve extensionen...
///* public??? */ static ServiceExtensionMirror of(ServiceExtension extension) {
//    return extension.mirror();
//}