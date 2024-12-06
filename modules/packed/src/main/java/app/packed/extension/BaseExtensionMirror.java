package app.packed.extension;

import java.util.Map;

import app.packed.binding.Key;
import app.packed.service.ServiceContract;
import app.packed.service.mirrorold.ExportedServiceMirror;

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
        return handle.extension().container.servicesMain().exports.toUnmodifiableSequenceMap(e -> (ExportedServiceMirror) e.operation.mirror());
    }

//    // Tror ikke de her overlever
//    /** { @return a map of all the services that are provided internally in the container.} */
//    @SuppressWarnings("exports")
//    public Map<Key<?>, ProvidedServiceMirror> serviceProviders() {
//        // Not really a map view
//        LinkedHashMap<Key<?>, ProvidedServiceMirror> result = new LinkedHashMap<>();
//        for (NamespaceServiceProviderSetup provider : handle.extension().container.servicesMain().providers) {
//            ProvidedServiceMirror mirror = (ProvidedServiceMirror) provider.operation().mirror();
//            result.put(provider.key(), mirror);
//        }
//        return Collections.unmodifiableMap(result);
//    }
}
