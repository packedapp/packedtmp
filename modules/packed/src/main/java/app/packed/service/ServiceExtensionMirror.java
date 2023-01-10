package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.packed.binding.Key;
import app.packed.extension.ExtensionMirror;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.service.ExportedService;
import internal.app.packed.service.ServiceManagerEntry;

/** A mirror for the {@link ServiceExtension}. */
public final class ServiceExtensionMirror extends ExtensionMirror<ServiceExtension> {

    /** The container the extension is a part of. */
    private final ContainerSetup container;

    /**
     * Creates a new mirror
     * 
     * @param container
     *            the container the extension is a part of
     */
    ServiceExtensionMirror(ContainerSetup container) {
        this.container = requireNonNull(container);
    }

    /**
     * Returns the service contract for the container.
     * 
     * @return the service contract for the container
     * @throws IllegalStateException
     *             if dependencies have not been resolved
     */
    public ServiceContract contract() {
        if (!container.assembly.isClosed()) {
            // Den fungere ikke hvis vi ikke har resolvet alle services.
            // Fordi vi ved jo ikke om en required service fx bliver provided af et link af en container
            // senere hen, alternativet er kun at have
            throw new IllegalStateException();
        }

        ServiceContract.Builder builder = ServiceContract.builder();

        // Add all exports
        container.sm.exports.keySet().forEach(k -> builder.provide(k));

        // All all requirements
        for (Entry<Key<?>, ServiceManagerEntry> e : container.sm.entries.entrySet()) {
            ServiceManagerEntry sme = e.getValue();
            if (sme.provider == null) {
                if (sme.isRequired) {
                    builder.require(e.getKey());
                } else {
                    builder.requireOptional(e.getKey());
                }
            }
        }

        return builder.build();
    }

    // Detaljeret info, ogsaa med dependency graph som kan extractes...
    // Hvad skal vi returnere???

    // ServiceRegister, hvor hver service har specielle attributer??
    // Et Map af <Key, ServiceMirror> (Helt sikkert service mirror)
    // MapView<Key<?>, ServiceMirror>

    // or contract.keys()

    // Map<K, V> resolved
    // Map<K, V> unresolvedOptional?();

    /** { @return a map view of all the services that are exported by the container.} */
    public Map<Key<?>, ExportedServiceMirror> exports() {
        LinkedHashMap<Key<?>, ExportedServiceMirror> result = new LinkedHashMap<>();
        for (ExportedService e : container.sm.exports.values()) {
            ExportedServiceMirror mirror = (ExportedServiceMirror) e.os.mirror();
            result.put(e.key, mirror);
        }
        return Collections.unmodifiableMap(result);
    }

    /** { @return a map view of all the services that are provided internally in the container.} */
    public Map<Key<?>, ProvidedServiceMirror> provisions() {
        LinkedHashMap<Key<?>, ProvidedServiceMirror> result = new LinkedHashMap<>();
        for (ServiceManagerEntry e : container.sm.entries.values()) {
            ProvidedServiceMirror mirror = (ProvidedServiceMirror) e.provider.operation.mirror();
            result.put(e.key, mirror);
        }
        return Collections.unmodifiableMap(result);
    }
}
