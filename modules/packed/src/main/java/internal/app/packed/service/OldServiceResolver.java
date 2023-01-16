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
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import app.packed.application.BuildException;
import app.packed.bean.BeanSourceKind;
import app.packed.binding.Key;
import app.packed.service.ServiceLocator;
import internal.app.packed.lifetime.LifetimeAccessor;
import internal.app.packed.lifetime.LifetimeAccessor.DynamicAccessor;
import internal.app.packed.lifetime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.FieldOperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.MethodOperationSetup;
import internal.app.packed.util.ThrowableUtil;

@Deprecated
public final class OldServiceResolver {

    private final LinkedHashMap<Key<?>, DependencyNode> nodes = new LinkedHashMap<>();

    public void addConsumer(OperationSetup operation, LifetimeAccessor la) {
        final AtomicReference<MethodHandle> ar = new AtomicReference<>();
        if (la != null) {
            operation.bean.container.application.addCodegenAction(() -> {
                MethodHandle mh = operation.generateMethodHandle();
                ar.set(mh);
            });
            
            operation.bean.container.lifetime.pool.addOrdered(p -> {
                MethodHandle mh = ar.get(); // operation.generateMethodHandle();
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

    ServiceLocator newServiceLocator(PackedExtensionContext region) {
        Map<Key<?>, MethodHandle> runtimeEntries = new LinkedHashMap<>(nodes.size());
        for (var e : nodes.entrySet()) {
            Key<?> key = e.getKey();
            DependencyNode export = e.getValue();
            
            MethodHandle mh;
            if (export.pis.isConstant) {
                System.out.println("CONST");
            }
            if (export.accessor == null) {
                mh = export.operation.generateMethodHandle();
            } else {
                mh = PackedExtensionContext.constant(key.rawType(), export.accessor.read(region));
            }
            mh = mh.asType(mh.type().changeReturnType(Object.class));

            assert(mh.type().equals(MethodType.methodType(Object.class, PackedExtensionContext.class)));
            runtimeEntries.put(key, mh);
        }
        return new PackedServiceLocator(region, Map.copyOf(runtimeEntries));
    }

    void provideService(ProvidedService provider) {
        OperationSetup o = provider.operation;
        DependencyNode bis;
        if (o instanceof OperationSetup.LifetimePoolOperationSetup) {
            OperationSetup os = null;
            LifetimeAccessor accessor = null;
            if (o.bean.lifetimePoolAccessor == null) {
                if (o.bean.sourceKind == BeanSourceKind.INSTANCE) {

                }
                os = o.bean.operations.get(0);
            } else {
                accessor = o.bean.lifetimePoolAccessor;
            }
            bis = new DependencyNode(os, provider, accessor);
        } else {
            boolean isStatic;
            if (o instanceof MethodOperationSetup ss) {
                isStatic = Modifier.isStatic(ss.method().getModifiers());
            } else if (o instanceof FieldOperationSetup ss) {
                isStatic = Modifier.isStatic(ss.field().getModifiers());
            } else {
                throw new Error();
            }

            if (!isStatic && o.bean.lifetimePoolAccessor == null) {
                throw new BuildException("Not okay)");
            }
            DynamicAccessor accessor = provider.isConstant ? o.bean.container.lifetime.pool.reserve(Object.class) : null;
            bis = new DependencyNode(o, provider, accessor);
            addConsumer(o, accessor);
        }
        nodes.put(provider.entry.key, bis);
    }

    private record DependencyNode(OperationSetup operation, ProvidedService pis, LifetimeAccessor accessor) {}
}
