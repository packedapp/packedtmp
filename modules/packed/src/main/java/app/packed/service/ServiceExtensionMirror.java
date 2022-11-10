package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import app.packed.container.ExtensionMirror;
import internal.app.packed.service.ExportedService;
import internal.app.packed.service.ServiceManagerEntry;
import internal.app.packed.service.old.InternalServiceExtension;

/** A specialized extension mirror for the {@link ServiceExtension}. */
public class ServiceExtensionMirror extends ExtensionMirror<ServiceExtension> {

    /** The service manager */
    private final InternalServiceExtension services;

    ServiceExtensionMirror(InternalServiceExtension services) {
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
    public Map<Key<?>, ExportedServiceMirror> exports() {
        LinkedHashMap<Key<?>, ExportedServiceMirror> result = new LinkedHashMap<>();
        for (ExportedService e : services.container.sm.exports.values()) {
            ExportedServiceMirror mirror = (ExportedServiceMirror) e.bos.mirror();
            result.put(e.key, mirror);
        }
        return Collections.unmodifiableMap(result);
    }

    /** { @return a map view of all the services that are provided internally in the container.} */
    public Map<Key<?>, ProvidedServiceMirror> provisions() {
        LinkedHashMap<Key<?>, ProvidedServiceMirror> result = new LinkedHashMap<>();
        for (ServiceManagerEntry e : services.container.sm.entries.values()) {
            ProvidedServiceMirror mirror = (ProvidedServiceMirror) e.provider.operation.mirror();
            result.put(e.key, mirror);
        }
        return Collections.unmodifiableMap(result);
    }
}
