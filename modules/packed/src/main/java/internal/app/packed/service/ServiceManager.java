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

import static internal.app.packed.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanSourceKind;
import app.packed.binding.Key;
import app.packed.framework.Nullable;
import app.packed.service.ExportedServiceCollisionException;
import app.packed.service.ExportedServiceMirror;
import app.packed.service.ProvideService;
import app.packed.service.ProvidedServiceCollisionException;
import app.packed.service.ProvidedServiceMirror;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceLocator;
import app.packed.service.UnsatisfiableServiceDependencyException;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.binding.BindingProvider;
import internal.app.packed.binding.BindingProvider.FromLifetimeArena;
import internal.app.packed.binding.BindingProvider.FromOperation;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.BeanInstanceAccessor;
import internal.app.packed.lifetime.runtime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.MethodOperationSetup;
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
        if (container.treeParent == null) {
            container.application.addCodeGenerator(() -> {

                this.exportedServices = CollectionUtil.copyOf(exports, n -> {
                    MethodHandle mh;
                    OperationSetup o = n.os;

                    BeanInstanceAccessor accessor = null;
                    if (o instanceof OperationSetup.BeanAccessOperationSetup) {
                        accessor = o.bean.lifetimePoolAccessor;
                        // test if prototype bean
                        if (accessor == null && o.bean.sourceKind != BeanSourceKind.INSTANCE) {
                            o = o.bean.operations.get(0);
                        }
                    }
                    if (!(o instanceof MemberOperationSetup) && o.bean.sourceKind == BeanSourceKind.INSTANCE) {
                        // It is a a constant
                        mh = MethodHandles.constant(Object.class, o.bean.source);
                        mh = MethodHandles.dropArguments(mh, 0, PackedExtensionContext.class);
                    } else if (accessor != null) {
                        mh = MethodHandles.insertArguments(PackedExtensionContext.MH_CONSTANT_POOL_READER, 1, accessor.index());
                    } else {
                        mh = o.generateMethodHandle();
                    }
                    mh = mh.asType(mh.type().changeReturnType(Object.class));
                    assert (mh.type().equals(MethodType.methodType(Object.class, PackedExtensionContext.class)));
                    return mh;
                });
            });
        }
    }

    public ServiceLocator exportedServices(PackedExtensionContext context) {
        Map<Key<?>, MethodHandle> m = exportedServices;
        if (m == null) {
            throw new UnsupportedOperationException("Exported services not available");
        }
        return new PackedServiceLocator(context, m);
    }

    public Set<Key<?>> keysAvailableInternally() {
        HashSet<Key<?>> result = new HashSet<>();
        // All all requirements
        for (Entry<Key<?>, ServiceManagerEntry> e : entries.entrySet()) {
            ServiceManagerEntry sme = e.getValue();
            if (sme.provider != null) {
                result.add(e.getKey());
            }
        }
        return Set.copyOf(result);
    }

    public ServiceBindingSetup serviceBind(Key<?> key, boolean isRequired, OperationSetup operation, int index) {
        return entries.compute(key, (k, v) -> {
            if (v == null) {
                v = new ServiceManagerEntry(k);
            }
            if (isRequired) {
                v.isRequired = true;
            }
            ServiceBindingSetup sbs = new ServiceBindingSetup(operation, index, v, isRequired);
            ServiceBindingSetup existing = v.bindings;
            if (existing == null) {
                v.bindings = sbs;
            } else {
                existing.nextFriend = sbs;
                v.bindings = sbs;
            }
            return v;
        }).bindings;
    }

    // 3 Muligheder -> Field, Method, BeanInstance
    public ExportedService serviceExport(Key<?> key, OperationSetup operation) {
        ExportedService es = new ExportedService(operation, key);
        ExportedService existing = exports.putIfAbsent(es.key, es);
        if (existing != null) {
            // A service with the key has already been exported
            throw new ExportedServiceCollisionException("Jmm");
        }
        es.os.mirrorSupplier = () -> new ExportedServiceMirror(es);
        operation.bean.container.useExtension(ServiceExtension.class, null);
        return es;
    }

    /**
     * Provides a service for the specified operation.
     * <p>
     * This method is called either because a bean is registered directly via {@link BeanHandle#serviceProvideAs(Key)} or
     * from {@link ServiceExtension#newBeanIntrospector} because someone used the {@link ProvideService} annotation.
     * 
     * @param key
     *            the key for which to provide a service for
     * @param operation
     *            the operation that provides the service
     * @return a provided service
     */
    // 3 Muligheder -> Field, Method, BeanInstance
    public ProvidedService serviceProvide(Key<?> key, OperationSetup operation, BindingProvider r) {
        BeanSetup bean = operation.bean;
        ServiceManagerEntry entry = entries.computeIfAbsent(key, ServiceManagerEntry::new);

        // Check lifetimes

        // Fail if there is there is already an existing provider with the same key
        if (entry.provider != null) {
            throw new ProvidedServiceCollisionException(makeDublicateProvideErrorMsg(entry.provider, operation));
        }

        // Create a new provider
        ProvidedService provider = entry.provider = new ProvidedService(operation, entry, r);

        operation.mirrorSupplier = () -> new ProvidedServiceMirror(entry.provider);

        // add the service provider to the bean, this is used for cyclic dependency check later on
        bean.serviceProviders.add(provider);

        if (exportAll) {
            serviceExport(key, operation);
        }

        return provider;
    }

    public void verify() {
        for (ServiceManagerEntry e : entries.values()) {
            if (e.provider == null) {
                for (var b = e.bindings; b != null; b = b.nextFriend) {
                    System.out.println("Binding not resolved " + b);
                }
                throw new UnsatisfiableServiceDependencyException("For key " + e.key);
            }
        }
    }

    private static String makeDublicateProvideErrorMsg(ProvidedService provider, OperationSetup otherOperation) {
        OperationSetup existingTarget = provider.operation;
        OperationSetup thisTarget = otherOperation;

        Key<?> key = provider.entry.key;

        if (existingTarget.bean == thisTarget.bean) {
            return "This bean is already providing a service for Key<" + key.toStringSimple() + ">, beanClass = " + format(existingTarget.bean.beanClass);
        }
        if (provider.resolution instanceof FromLifetimeArena) {
            return "Cannot provide a service for Key<" + key.toStringSimple() + ">, as another bean of type " + format(existingTarget.bean.beanClass)
                    + " is already providing a service for the same key";

            // return "Another bean of type " + format(existingTarget.bean.beanClass) + " is already providing a service for Key<" +
            // key.toStringSimple() + ">";

            // return "Another bean of type " + format(existingTarget.bean.beanClass) + " is already providing a service for Key<" +
            // key.toStringSimple() + ">";
        } else if (provider.resolution instanceof FromOperation os) {
            if (os.operation instanceof MethodOperationSetup m) {
                String ss = StringFormatter.formatShortWithParameters(m.method());
                return "A method " + ss + " is already providing a service for Key<" + key + ">";
            }
        }
        return thisTarget + "A service has already been bound for key " + key;
    }
}
