/*
 * Copyright (c) 2026 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package internal.app.packed.service;

import java.util.Iterator;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import app.packed.binding.DublicateKeyProvisionException;
import app.packed.binding.Key;
import app.packed.namespaceold.NamespaceInstaller;
import app.packed.namespaceold.NamespaceTemplate;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceLocator;
import internal.app.packed.binding.BindingProvider;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.invoke.MethodHandleInvoker.ExportedServiceWrapper;
import internal.app.packed.invoke.ServiceSupport;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.ServiceProviderSetup.NamespaceServiceProviderHandle;
import internal.app.packed.service.util.PackedServiceLocator;
import internal.app.packed.service.util.ServiceMap;

/** Manages services in a single container. */
public final class MainServiceNamespaceHandle extends ServiceNamespaceHandle {

    /** The default namespace template used for the service namespace. */
    public static NamespaceTemplate<MainServiceNamespaceHandle> TEMPLATE = NamespaceTemplate.of(MainServiceNamespaceHandle.class,
            MainServiceNamespaceHandle::new);

    // All provided services are automatically exported
    public boolean exportAll;

    /** A map of exported service method method handles, must be computed. */
    @Nullable
    private Map<Key<?>, ExportedServiceWrapper> exportedServices;

    /** Exported services from the container. */
    public final ServiceMap<ExportedService> exports = new ServiceMap<>();

    @Nullable
    MainServiceNamespaceHandle parent;

    public MainServiceNamespaceHandle(NamespaceInstaller<?> installer) {
        super(installer);
        // For now we a ServiceLocator as the root of the application.
        // Bridges
    }

    public void init(@Nullable MainServiceNamespaceHandle parent, ContainerSetup container) {
        this.parent = parent;

        if (container != null && container.isApplicationRoot()) {
            container.application.addCodegenAction(() -> exportedServices = exportedServices());
        }
    }

    // 3 Muligheder -> Field, Method, BeanInstance
    public ExportedService export(Key<?> key, OperationSetup operation) {
        ExportedService es = new ExportedService(operation, key);
        ExportedService existing = exports.putIfAbsent(es.key, es);
        if (existing != null) {
            // A service with the key has already been exported
            throw new DublicateKeyProvisionException("Jmm " + es.key);
        }
        return es;
    }

    public Map<Key<?>, ExportedServiceWrapper> exportedServices() {
        return exports.toUnmodifiableMap(n -> ServiceSupport.toExportedService(n.operation));
    }

    public ServiceContract newContract() {
        ServiceContract.Builder builder = ServiceContract.builder();

        // Add all exports
        exports.keySet().forEach(k -> builder.provide(k));

        // All all requirements
        // This must included unresolved I think
        for (Iterator<NamespaceServiceProviderHandle> iterator = providers.iterator(); iterator.hasNext();) {
            NamespaceServiceProviderHandle provider = iterator.next();
            boolean isRequired = false;
            for (ServiceBindingSetup sbs : provider.bindings) {
                if (sbs.isRequired) {
                    isRequired = true;
                }
            }
            if (isRequired) {
                builder.require(provider.key());
            } else {
                builder.requireOptional(provider.key());
            }

        }
        return builder.build();
    }

    public ServiceLocator newExportedServiceLocator(ExtensionContext context) {
        Map<Key<?>, ExportedServiceWrapper> m = exportedServices;
        if (m == null) {
            throw new UnsupportedOperationException("Exported services not available");
        }
        return new PackedServiceLocator(context, m);
    }

    /**
     * Provides a service for the specified operation.
     * <p>
     * This method is called either because a bean is registered directly via {@link BeanHandle#serviceProvideAs(Key)} or
     * from {@link BaseExtension#newBeanIntrospector} because someone used a {@link Provide} annotation.
     *
     * @param key
     *            the key to provide a service for
     * @param operation
     *            the operation that provides the service
     * @return a provided service
     */
    public void provideService(Key<?> key, ServiceProvideOperationHandle oh, BindingProvider resolution) {

        super.provide(key, oh, resolution);

        OperationSetup operation = OperationSetup.crack(oh);
        if (exportAll) {
            export(key, operation);
        }
    }
}
