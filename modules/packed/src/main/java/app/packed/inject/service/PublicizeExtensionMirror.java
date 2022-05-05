package app.packed.inject.service;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import app.packed.base.Key;
import app.packed.container.ExtensionMirror;
import app.packed.operation.usage.ServiceExportMirror;
import app.packed.operation.usage.ServiceProvideMirror;
import packed.internal.inject.service.ContainerInjectionManager;

/** A specialized extension mirror for the {@link PublicizeExtension}. */
public class PublicizeExtensionMirror extends ExtensionMirror<PublicizeExtension> {

    /** The service manager */
    private final ContainerInjectionManager services;

    PublicizeExtensionMirror(ContainerInjectionManager services) {
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
        return services.ios.newServiceContract();
    }

    // Detaljeret info, ogsaa med dependency graph som kan extractes...
    // Hvad skal vi returnere???

    // ServiceRegister, hvor hver service har specielle attributer??
    // Et Map af <Key, ServiceMirror> (Helt sikkert service mirror)
    // MapView<Key<?>, ServiceMirror>

    // or contract.keys()

    // Map<K, V> resolved
    // Map<K, V> unresolvedOptional?();

    /** { @return a map view of all the services that are exported from the container.} */
    @SuppressWarnings("exports")
    public Map<Key<?>, ServiceExportMirror> exports() {
        throw new UnsupportedOperationException();
    }

    /** { @return a map view of all the services that are provided internally in the container.} */
    @SuppressWarnings("exports")
    public Map<Key<?>, ServiceProvideMirror> provisions() {
        throw new UnsupportedOperationException();
    }
}