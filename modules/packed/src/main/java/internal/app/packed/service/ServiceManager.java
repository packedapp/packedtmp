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

import app.packed.bean.BeanSourceKind;
import app.packed.bindings.Key;
import app.packed.bindings.KeyAlreadyInUseException;
import app.packed.bindings.UnsatisfiableDependencyException;
import app.packed.extension.ExtensionContext;
import app.packed.framework.Nullable;
import app.packed.service.ExportedServiceMirror;
import app.packed.service.ProvidedServiceMirror;
import app.packed.service.ServiceLocator;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingResolution.FromLifetimeArena;
import internal.app.packed.binding.BindingResolution.FromOperation;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.runtime.PackedExtensionContext;
import internal.app.packed.operation.OperationMemberTarget.OperationMethodTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.StringFormatter;

/** Manages services in a single container. */
public final class ServiceManager {

    public final LinkedHashMap<Key<?>, ServiceManagerEntry> entries = new LinkedHashMap<>();

    // All provided services are automatically exported
    public boolean exportAll;

    /** A map of exported service method method handles, must be computed */
    @Nullable
    private Map<Key<?>, MethodHandle> exportedServices;

    /** Exported services from the container. */
    public final LinkedHashMap<Key<?>, ExportedService> exports = new LinkedHashMap<>();

    public ServiceManager(ContainerSetup container) {
        // For now we a ServiceLocator as the root of the application.
        // Bridges

        if (container.treeParent == null) {
            container.application.addCodeGenerator(() -> {
                this.exportedServices = CollectionUtil.copyOf(exports, n -> {
                    MethodHandle mh;
                    OperationSetup o = n.os;

                    int accessor = -1;
                    if (o instanceof OperationSetup.BeanAccessOperationSetup) {
                        accessor = o.bean.lifetimeStoreIndex;
                        // test if prototype bean
                        if (accessor == -1 && o.bean.beanSourceKind != BeanSourceKind.INSTANCE) {
                            o = o.bean.operations.get(0);
                        }
                    }
                    if (!(o instanceof MemberOperationSetup) && o.bean.beanSourceKind == BeanSourceKind.INSTANCE) {
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
            });
        }
    }

    public ServiceBindingSetup bind(Key<?> key, boolean isRequired, OperationSetup operation, int operationBindingIndex) {
        return entries.compute(key, (k, e) -> {
            if (e == null) {
                e = new ServiceManagerEntry(k);
            }
            if (isRequired) {
                e.isRequired = true;
            }

            // Create the new binding
            ServiceBindingSetup binding = new ServiceBindingSetup(operation, operationBindingIndex, e, isRequired);

            // Add this binding to the list of bindings for the entry
            ServiceBindingSetup existing = e.bindings;
            if (existing == null) {
                e.bindings = binding;
            } else {
                existing.nextFriend = binding;
                e.bindings = binding;
            }

            return e;
        }).bindings;
    }

    // 3 Muligheder -> Field, Method, BeanInstance
    public ExportedService export(Key<?> key, OperationSetup operation) {
        ExportedService es = new ExportedService(operation, key);
        ExportedService existing = exports.putIfAbsent(es.key, es);
        if (existing != null) {
            // A service with the key has already been exported
            throw new KeyAlreadyInUseException("Jmm");
        }
        es.os.mirrorSupplier = () -> new ExportedServiceMirror(es);
        return es;
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
    public ProvidedService provide(Key<?> key, OperationSetup operation, BindingResolution resolution) {
        BeanSetup bean = operation.bean;

        ServiceManagerEntry entry = entries.computeIfAbsent(key, ServiceManagerEntry::new);

        // Check same lifetime as the container, or own prototype service

        // Check if there is an existing provider for the same key, in which case we fail
        if (entry.provider != null) {
            throw new KeyAlreadyInUseException(makeDublicateProvideErrorMsg(entry.provider, operation));
        }

        // Create a new provider
        ProvidedService provider = entry.provider = new ProvidedService(operation, entry, resolution);

        operation.mirrorSupplier = () -> new ProvidedServiceMirror(entry.provider);

        // add the service provider to the bean, this is used for cyclic dependency check later on
        bean.serviceProviders.add(provider);

        if (exportAll) {
            export(key, operation);
        }

        return provider;
    }

    public void verify() {
        for (ServiceManagerEntry e : entries.values()) {
            if (e.provider == null) {
                for (var b = e.bindings; b != null; b = b.nextFriend) {
                    System.out.println("Binding not resolved " + b);
                }
                throw new UnsatisfiableDependencyException("For key " + e.key);
            }
        }
    }

    private static String makeDublicateProvideErrorMsg(ProvidedService existingProvider, OperationSetup newProvider) {
        OperationSetup existingTarget = existingProvider.operation;
        OperationSetup thisTarget = newProvider;

        Key<?> key = existingProvider.entry.key;

        if (existingTarget.bean == thisTarget.bean) {
            return "This bean is already providing a service for Key<" + key.toString() + ">, beanClass = "
                    + StringFormatter.format(existingTarget.bean.beanClass);
        }
        if (existingProvider.resolution instanceof FromLifetimeArena) {
            return "Cannot provide a service for Key<" + key.toString() + ">, as another bean of type " + StringFormatter.format(existingTarget.bean.beanClass)
                    + " is already providing a service for the same key";

            // return "Another bean of type " + format(existingTarget.bean.beanClass) + " is already providing a service for Key<" +
            // key.toStringSimple() + ">";

            // return "Another bean of type " + format(existingTarget.bean.beanClass) + " is already providing a service for Key<" +
            // key.toStringSimple() + ">";
        } else if (existingProvider.resolution instanceof FromOperation os) {
            if (os.operation() instanceof MemberOperationSetup m && m.target instanceof OperationMethodTarget t) {
                String ss = StringFormatter.formatShortWithParameters(t.method());
                return "A method " + ss + " is already providing a service for Key<" + key + ">";
            }
        }
        return thisTarget + "A service has already been bound for key " + key;
    }
}
