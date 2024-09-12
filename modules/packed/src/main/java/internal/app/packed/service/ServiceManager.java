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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.packed.bean.BeanSourceKind;
import app.packed.extension.ExtensionContext;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceLocator;
import app.packed.service.mirror.oldMaybe.ExportedServiceMirror;
import app.packed.util.Key;
import app.packed.util.KeyAlreadyInUseException;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.runtime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationType.BeanAccessOperationSetup;
import internal.app.packed.operation.PackedOperationType.MemberOperationSetup;
import internal.app.packed.util.CollectionUtil;

/** Manages services in a single container. */
public final class ServiceManager {

    /** All entries in the service manager. This covers both bindings and service provisions. */
    public final LinkedHashMap<Key<?>, ServiceSetup> entries = new LinkedHashMap<>();

    // All provided services are automatically exported
    public boolean exportAll;

    /** A map of exported service method method handles, must be computed. */
    @Nullable
    private Map<Key<?>, MethodHandle> exportedServices;

    /** Exported services from the container. */
    public final LinkedHashMap<Key<?>, ExportedService> exports = new LinkedHashMap<>();

    @Nullable
    public final ServiceManager parent;

    public ServiceManager(@Nullable ServiceManager parent, ContainerSetup container) {
        // For now we a ServiceLocator as the root of the application.
        // Bridges
        this.parent = parent;

        if (container != null && container.isApplicationRoot()) {
            container.application.addCodegenAction(() -> exportedServices = exportedServices());
        }
    }

    public void addBean(BeanSetup bean) {
        provide(Key.of(bean.beanClass), bean.instanceAccessOperation(), bean.beanInstanceBindingProvider());
    }

    public ServiceBindingSetup bind(Key<?> key, boolean isRequired, OperationSetup operation, int operationBindingIndex) {
        ServiceSetup e = entries.computeIfAbsent(key, ServiceSetup::new);
        return e.bind(isRequired, operation, operationBindingIndex);
    }

    // 3 Muligheder -> Field, Method, BeanInstance
    public ExportedService export(Key<?> key, OperationSetup operation) {
        ExportedService es = new ExportedService(operation, key);
        ExportedService existing = exports.putIfAbsent(es.key, es);
        if (existing != null) {
            // A service with the key has already been exported
            throw new KeyAlreadyInUseException("Jmm " + es.key);
        }
        es.operation.mirrorSupplier = () -> new ExportedServiceMirror(es);
        return es;
    }

    public Map<Key<?>, MethodHandle> exportedServices() {
        Map<Key<?>, MethodHandle> result = CollectionUtil.copyOf(exports, n -> {
            MethodHandle mh;
            OperationSetup o = n.operation;

            int accessor = -1;
            if (o.pot instanceof BeanAccessOperationSetup) {
                accessor = o.bean.lifetimeStoreIndex;
                // test if prototype bean
                if (accessor == -1 && o.bean.beanSourceKind != BeanSourceKind.INSTANCE) {
                    o = o.bean.operations.all.get(0);
                }
            }
            if (!(o.pot instanceof MemberOperationSetup) && o.bean.beanSourceKind == BeanSourceKind.INSTANCE) {
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
        for (Entry<Key<?>, ServiceSetup> e : entries.entrySet()) {
            ServiceSetup sme = e.getValue();
            if (sme.provider() == null) {
                if (sme.isRequired) {
                    builder.require(e.getKey());
                } else {
                    builder.requireOptional(e.getKey());
                }
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
    public ServiceProviderSetup provide(Key<?> key, OperationSetup operation, BindingResolution resolution) {
        ServiceSetup entry = entries.computeIfAbsent(key, ServiceSetup::new);

        // TODO Check same lifetime as the container, or own prototype service

        ServiceProviderSetup provider = entry.setProvider(operation, resolution);

        if (exportAll) {
            export(key, operation);
        }

        return provider;
    }

    /**
     * @param result
     */
    public void provideAll(Map<Key<?>, MethodHandle> result) {}

    public void verify() {
        for (ServiceSetup e : entries.values()) {
            e.verify();
        }
    }
}
