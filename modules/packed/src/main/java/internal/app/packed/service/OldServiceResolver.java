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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.packed.application.BuildException;
import app.packed.service.Key;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceLocator;
import internal.app.packed.application.PackedApplicationDriver;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.lifetime.LifetimeAccessor;
import internal.app.packed.lifetime.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSite.LifetimePoolAccessSite;
import internal.app.packed.operation.binding.InternalDependency;
import internal.app.packed.util.ThrowableUtil;

/**
 * A service manager is responsible for managing the services for a single container at build time.
 */
public final class OldServiceResolver {

    private final ArrayList<DependencyNode> dependecyNodes = new ArrayList<>();

    private final LinkedHashMap<Key<?>, Requirement> requirements = new LinkedHashMap<>();

    private final LinkedHashMap<Key<?>, BuildtimeService> resolvedExports = new LinkedHashMap<>();

    public void addConsumer(OperationSetup operation, LifetimeAccessor la) {
        DependencyNode dependant = new DependencyNode(operation, la);
        dependecyNodes.add(dependant);
        if (la != null) {
            operation.site.bean.container.lifetime.pool.addOrdered(p -> {
                MethodHandle mh = operation.buildInvoker();

                Object instance;
                try {
                    instance = mh.invoke(p);
                } catch (Throwable e) {
                    throw ThrowableUtil.orUndeclared(e);
                }

                if (instance == null) {
                    throw new NullPointerException(this + " returned null");
                }

                la.store(p, instance);
            });
        }
    }

    public ServiceContract newServiceContract(ContainerSetup container) {
        ServiceContract.Builder builder = ServiceContract.builder();

        container.sm.exports.keySet().forEach(k -> builder.provide(k));

        // Add requirements (mandatory or optional)
        if (requirements != null && requirements != null) {
            for (Requirement r : requirements.values()) {
                if (r.isOptional) {
                    builder.requireOptional(r.key);
                } else {
                    builder.require(r.key);
                }
            }
        }

        return builder.build();
    }

    public ServiceLocator newServiceLocator(PackedApplicationDriver<?> driver, LifetimeObjectArena region) {
        Map<Key<?>, MethodHandle> runtimeEntries = new LinkedHashMap<>();
        for (BuildtimeService export : resolvedExports.values()) {
            runtimeEntries.put(export.key(), export.buildInvoker(region));
        }
        return new PackedServiceLocator(region, Map.copyOf(runtimeEntries));
    }

    /**
     * @param provider
     */
    public void provideService(ProvidedService provider) {
        OperationSetup o = provider.operation;
        BuildtimeService bis;
        if (o.site instanceof LifetimePoolAccessSite bia) {
            // addService(o.bean, provider.entry.key);

            OperationSetup os = null;
            LifetimeAccessor accessor = null;
            if (o.site.bean.injectionManager.lifetimePoolAccessor == null) {
                os = o.site.bean.operations.get(0);
            } else {
                accessor = o.site.bean.injectionManager.lifetimePoolAccessor;
            }

            bis = new BuildtimeService(provider.entry.key, os, accessor);
        } else {
            boolean isStatic = !o.site.requiresBeanInstance();
            if (!isStatic && o.site.bean.injectionManager.lifetimePoolAccessor == null) {
                throw new BuildException("Not okay)");
            }
            DynamicAccessor accessor = provider.isConstant ? o.site.bean.container.lifetime.pool.reserve(Object.class) : null;
            bis = new BuildtimeService(provider.entry.key, o, accessor);

            addConsumer(o, accessor);
        }

        o.site.bean.container.safeUseExtensionSetup(ServiceExtension.class, null);
        resolvedExports.put(bis.key, bis);
    }

    public void resolve() {
        for (DependencyNode node : dependecyNodes) {
            List<InternalDependency> dependencies = InternalDependency.fromOperationType(node.operation.site.type);// null;//factory.dependencies();
            for (int i = 0; i < dependencies.size(); i++) {
                InternalDependency sd = dependencies.get(i);

                Object e;

                if (node.operation.site.bean.realm instanceof ExtensionTreeSetup ers) {
                    Key<?> requiredKey = sd.key();
                    Key<?> thisKey = Key.of(node.operation.site.bean.beanClass);
                    ContainerSetup container = node.operation.site.bean.container;
                    ExtensionSetup es = container.safeUseExtensionSetup(ers.realmType(), null);
                    BeanSetup bs = null;
                    if (thisKey.equals(requiredKey)) {
                        if (es.treeParent != null) {
                            bs = es.treeParent.injectionManager.lookup(requiredKey);
                        }
                    } else {
                        bs = es.injectionManager.lookup(requiredKey);
                    }
                    if (bs == null) {
                        throw new RuntimeException("Could not resolve key " + requiredKey + " for " + ers.realmType());
                    }

                    e = bs.injectionManager;

                } else {
                    e = resolvedExports.get(sd.key());
                }

                if (e == null) {
                    Requirement r = requirements.computeIfAbsent(sd.key(), Requirement::new);
                    r.missingDependency(sd);
                }
            }
        }
    }

    record DependencyNode(OperationSetup operation, LifetimeAccessor la) {}

    private record BuildtimeService(Key<?> key, OperationSetup operation, LifetimeAccessor accessor) {

        private MethodHandle buildInvoker(LifetimeObjectArena context) {
            if (accessor == null) {
                return operation.buildInvoker();
            } else {
                return LifetimeObjectArena.constant(key.rawType(), accessor.read(context));
            }
        }
    }

    static class Requirement {

        // Always starts out as optional
        boolean isOptional = true;

        final Key<?> key;

        Requirement(Key<?> key) {
            this.key = key;
        }

        void missingDependency(InternalDependency d) {
            if (!d.isOptional()) {
                isOptional = false;
            }
        }
    }

}
