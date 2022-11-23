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
import java.util.LinkedHashMap;
import java.util.Map;

import app.packed.application.BuildException;
import app.packed.bean.BeanSourceKind;
import app.packed.service.Key;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceLocator;
import internal.app.packed.lifetime.LifetimeAccessor;
import internal.app.packed.lifetime.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSite.LifetimePoolAccessSite;
import internal.app.packed.util.ThrowableUtil;

public final class OldServiceResolver {

    private final LinkedHashMap<Key<?>, DependencyNode> nodes = new LinkedHashMap<>();

    public void addConsumer(OperationSetup operation, LifetimeAccessor la) {
        if (la != null) {
            operation.site.bean.container.lifetime.pool.addOrdered(p -> {
                MethodHandle mh = operation.generateMethodHandle();
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

    ServiceLocator newServiceLocator(LifetimeObjectArena region) {
        Map<Key<?>, MethodHandle> runtimeEntries = new LinkedHashMap<>();
        for (var e : nodes.entrySet()) {
            Key<?> key = e.getKey();
            DependencyNode export = e.getValue();
            MethodHandle mh;
            if (export.accessor == null) {
                mh = export.operation.generateMethodHandle();
            } else {
                mh = LifetimeObjectArena.constant(key.rawType(), export.accessor.read(region));
            }
            runtimeEntries.put(key, mh);
        }
        return new PackedServiceLocator(region, Map.copyOf(runtimeEntries));
    }

    void provideService(ProvidedService provider) {
        OperationSetup o = provider.operation;
        DependencyNode bis;
        if (o.site instanceof LifetimePoolAccessSite bia) {
            OperationSetup os = null;
            LifetimeAccessor accessor = null;
            if (o.site.bean.injectionManager.lifetimePoolAccessor == null) {
                if (o.site.bean.sourceKind == BeanSourceKind.INSTANCE) {
                    
                }
                os = o.site.bean.operations.get(0);
            } else {
                accessor = o.site.bean.injectionManager.lifetimePoolAccessor;
            }
            bis = new DependencyNode(os, accessor);
        } else {
            boolean isStatic = !o.site.requiresBeanInstance();
            if (!isStatic && o.site.bean.injectionManager.lifetimePoolAccessor == null) {
                throw new BuildException("Not okay)");
            }
            DynamicAccessor accessor = provider.isConstant ? o.site.bean.container.lifetime.pool.reserve(Object.class) : null;
            bis = new DependencyNode(o, accessor);
            addConsumer(o, accessor);
        }
        o.site.bean.container.safeUseExtensionSetup(ServiceExtension.class, null);
        nodes.put(provider.entry.key, bis);
    }

    private record DependencyNode(OperationSetup operation, LifetimeAccessor accessor) {}
}
