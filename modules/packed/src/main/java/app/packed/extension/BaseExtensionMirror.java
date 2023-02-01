package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import app.packed.bindings.Key;
import app.packed.framework.Nullable;
import app.packed.service.ExportedServiceMirror;
import app.packed.service.ProvidedServiceMirror;
import app.packed.service.ServiceContract;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.service.ExportedService;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.service.ServiceSetup;

/** A mirror for {@link BaseExtension}. */
// Or just directly on Application, Container... Move services into the main stream
public final class BaseExtensionMirror extends ExtensionMirror<BaseExtension> {

    /** The container the extension is a part of. */
    private final ContainerSetup container;

    /**
     * Creates a new mirror
     *
     * @param container
     *            the container the extension is a part of
     */
    BaseExtensionMirror(ContainerSetup container) {
        this.container = requireNonNull(container);
    }

    /**
     * {@return a service contract for the container}
     * <p>
     * If the configuration of the container has not been completed. This method return a contract on a best effort basis.
     */
    public ServiceContract serviceContract() {
        return container.sm.newContract();
    }

    // Detaljeret info, ogsaa med dependency graph som kan extractes...
    // Hvad skal vi returnere???

    // ServiceRegister, hvor hver service har specielle attributer??
    // Et Map af <Key, ServiceMirror> (Helt sikkert service mirror)
    // MapView<Key<?>, ServiceMirror>

    // or contract.keys()

    // Map<K, V> resolved
    // Map<K, V> unresolvedOptional?();

    /** { @return a map of all the services that are exported by the container.} */
    public Map<Key<?>, ExportedServiceMirror> serviceExports() {
        LinkedHashMap<Key<?>, ExportedServiceMirror> result = new LinkedHashMap<>();
        for (ExportedService e : container.sm.exports.values()) {
            ExportedServiceMirror mirror = (ExportedServiceMirror) e.operation.mirror();
            result.put(e.key, mirror);
        }
        return Collections.unmodifiableMap(result);
    }

    /** { @return a map of all the services that are provided internally in the container.} */
    public Map<Key<?>, ProvidedServiceMirror> serviceProviders() {
        // Not really a map view
        LinkedHashMap<Key<?>, ProvidedServiceMirror> result = new LinkedHashMap<>();
        for (ServiceSetup e : container.sm.entries.values()) {
            @Nullable
            ServiceProviderSetup provider = e.provider();
            if (provider != null) {
                ProvidedServiceMirror mirror = (ProvidedServiceMirror) provider.operation.mirror();
                result.put(e.key, mirror);
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
