/*
 * Copyright (c) 2008 Kasper Nielsen.
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;
import java.util.Map;

import app.packed.bean.BeanSourceKind;
import app.packed.binding.Key;
import app.packed.binding.KeyAlreadyProvidedException;
import app.packed.extension.ExtensionContext;
import app.packed.namespace.NamespaceTemplate;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceLocator;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingAccessor;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.runtime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationTarget.BeanAccessOperationTarget;
import internal.app.packed.operation.PackedOperationTarget.MemberOperationTarget;
import internal.app.packed.service.ServiceProviderSetup.NamespaceServiceProviderSetup;
import internal.app.packed.service.util.SequencedServiceMap;

/** Manages services in a single container. */
public final class MainServiceNamespaceHandle extends ServiceNamespaceHandle {

    /** The default namespace template used for the service namespace. */
    public static NamespaceTemplate TEMPLATE = NamespaceTemplate.of(MainServiceNamespaceHandle.class, c -> {});

    // All provided services are automatically exported
    public boolean exportAll;

    /** A map of exported service method method handles, must be computed. */
    @Nullable
    private Map<Key<?>, MethodHandle> exportedServices;

    /** Exported services from the container. */
    public final SequencedServiceMap<ExportedService> exports = new SequencedServiceMap<>();

    @Nullable
    public final MainServiceNamespaceHandle parent;

    public MainServiceNamespaceHandle(NamespaceTemplate.Installer installer, @Nullable MainServiceNamespaceHandle parent, ContainerSetup container) {
        super(installer);
        // For now we a ServiceLocator as the root of the application.
        // Bridges
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
            throw new KeyAlreadyProvidedException("Jmm " + es.key);
        }
//        es.operation.mirrorSupplier = h -> new ExportedServiceMirror(h, es);
        return es;
    }

    public Map<Key<?>, MethodHandle> exportedServices() {
        Map<Key<?>, MethodHandle> result = exports.toUnmodifiableMap(n -> {
            MethodHandle mh;
            OperationSetup o = n.operation;

            int accessor = -1;
            if (o.target instanceof BeanAccessOperationTarget) {
                accessor = o.bean.lifetimeStoreIndex;
                // test if prototype bean
                if (accessor == -1 && o.bean.beanSourceKind != BeanSourceKind.INSTANCE) {
                    o = o.bean.operations.first();
                }
            }
            if (!(o.target instanceof MemberOperationTarget) && o.bean.beanSourceKind == BeanSourceKind.INSTANCE) {
                // It is a a constant
                mh = MethodHandles.constant(Object.class, o.bean.beanSource);
                mh = MethodHandles.dropArguments(mh, 0, ExtensionContext.class);
            } else if (accessor >= 0) {
                mh = MethodHandles.insertArguments(PackedExtensionContext.MH_CONSTANT_POOL_READER, 1, accessor);
            } else {
                mh = o.generateMethodHandle();
            }
            mh = mh.asType(mh.type().changeReturnType(Object.class));
            assert (mh.type().equals(MethodType.methodType(Object.class, ExtensionContext.class)));
            return mh;
        });
        return result;
    }

    public ServiceContract newContract() {
        ServiceContract.Builder builder = ServiceContract.builder();

        // Add all exports
        exports.keySet().forEach(k -> builder.provide(k));

        // All all requirements
        // This must included unresolved I think
        for (Iterator<NamespaceServiceProviderSetup> iterator = providers.iterator(); iterator.hasNext();) {
            NamespaceServiceProviderSetup provider = iterator.next();
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
        Map<Key<?>, MethodHandle> m = exportedServices;
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
    public void provideOperation(Key<?> key, OperationSetup operation, BindingAccessor resolution) {
        super.provide(key, operation, resolution);

        if (exportAll) {
            export(key, operation);
        }

    }

}
