package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.container.Bundle;
import app.packed.container.BundleMirror;
import app.packed.container.Wirelet;
import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;
import packed.internal.service.ServiceManagerSetup;

/**
 * A specialized extension mirror for {@link ServiceExtension}.
 * 
 * @see BundleMirror#extensions()
 * @see BundleMirror#useExtension(Class)
 */
@ExtensionMember(ServiceExtension.class)
public final class ServiceExtensionMirror extends ExtensionMirror {

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

    // ifPresent(), tryFind
    // Syntes find maaske er et lidt daarligt navn
    public static Optional<ServiceExtensionMirror> find(Bundle<?> assembly, Wirelet... wirelets) {
        return ExtensionMirror.find(ServiceExtensionMirror.class, assembly, wirelets);
    }

    /**
     * @param assembly
     * @param wirelets
     * @return
     * @throws NoSuchElementException
     *             if an
     * @see ExtensionMirror#of(Class, Bundle, Wirelet...)
     */
    public static ServiceExtensionMirror of(Bundle<?> assembly, Wirelet... wirelets) {
        return ExtensionMirror.of(ServiceExtensionMirror.class, assembly, wirelets);
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