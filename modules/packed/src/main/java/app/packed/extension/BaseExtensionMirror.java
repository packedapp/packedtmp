package app.packed.extension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import app.packed.binding.Key;
import app.packed.service.ServiceContract;
import app.packed.service.mirror.oldMaybe.ExportedServiceMirror;
import app.packed.service.mirror.oldMaybe.ProvidedServiceMirror;
import app.packed.util.Nullable;
import internal.app.packed.service.ExportedService;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.service.ServiceSetup;

/** A mirror for {@link BaseExtension}. */
public final class BaseExtensionMirror extends ExtensionMirror<BaseExtension> {

    /**
     * Creates a new base extension mirror
     *
     * @param handle
     *            a handle for the extension
     */
    BaseExtensionMirror(ExtensionHandle<BaseExtension> handle) {
        super(handle);
    }

    /**
     * {@return a service contract for the container}
     * <p>
     * If the configuration of the container has not been completed. This method return a contract on a best effort basis.
     */
    public ServiceContract serviceContract() {
        return handle.extension().container.servicesMain().newContract();
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
    @SuppressWarnings("exports")
    public Map<Key<?>, ExportedServiceMirror> serviceExports() {
        LinkedHashMap<Key<?>, ExportedServiceMirror> result = new LinkedHashMap<>();
        for (ExportedService e : handle.extension().container.servicesMain().exports.values()) {
            ExportedServiceMirror mirror = (ExportedServiceMirror) e.operation.mirror();
            result.put(e.key, mirror);
        }
        return Collections.unmodifiableMap(result);
    }

    /** { @return a map of all the services that are provided internally in the container.} */
    @SuppressWarnings("exports")
    public Map<Key<?>, ProvidedServiceMirror> serviceProviders() {
        // Not really a map view
        LinkedHashMap<Key<?>, ProvidedServiceMirror> result = new LinkedHashMap<>();
        for (ServiceSetup e : handle.extension().container.servicesMain().entries.values()) {
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
