package app.packed.inject;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.component.Assembly;
import app.packed.component.Wirelet;
import app.packed.container.ContainerMirror;
import app.packed.container.ExtensionMirror;

/**
 * A mirror of a {@link ServiceExtension}.
 */
// Taenker naar vi 
public interface ServiceExtensionMirror extends ExtensionMirror<ServiceExtension> {

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
    ServiceContract contract();

    // Detaljeret info, ogsaa med dependency graph som kan extractes...
    // Hvad skal vi returnere???

    // ServiceRegister, hvor hver service har specielle attributer??
    // Et Map af <Key, ServiceMirror> (Helt sikkert service mirror)
    // MapView<Key<?>, ServiceMirror>

    // or contract.keys()
    default Set<Key<?>> exportedKeys() {
        return exports().keySet();
    }

    // Map<K, V> resolved
    // Map<K, V> unresolvedOptional?();

    /** { @return a map view of all the services that are exported from the container.} */
    default Map<Key<?>, ServiceMirror> exports() {
        throw new UnsupportedOperationException();
    }

    public static Optional<ServiceExtensionMirror> find(Assembly<?> assembly, Wirelet... wirelets) {
        return ContainerMirror.of(assembly, wirelets).tryUse(ServiceExtensionMirror.class);
    }

    public static ServiceExtensionMirror of(Assembly<?> assembly, Wirelet... wirelets) {
        return ContainerMirror.of(assembly, wirelets).use(ServiceExtensionMirror.class);
    }

    /**
     * Returns a mirror for the specified extension.
     * 
     * @param extension
     *            the extension to return a mirror for
     * @return the mirror
     */
    /* public??? */ static ServiceExtensionMirror of(ServiceExtension extension) {
        return extension.mirror();
    }
}
