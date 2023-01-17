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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import app.packed.bean.BeanSourceKind;
import app.packed.binding.Key;
import app.packed.service.ServiceLocator;
import internal.app.packed.lifetime.BeanInstanceAccessor;
import internal.app.packed.lifetime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.CollectionUtil;
import internal.app.packed.util.ThrowableUtil;

public final class OldServiceResolver {

    private final LinkedHashMap<Key<?>, ProvidedService> nodes = new LinkedHashMap<>();

    private Map<Key<?>, MethodHandle> runtimeEntries;

    // These are all non-constant Container beans also those not provided
    public void addConsumer(OperationSetup factoryOperation, BeanInstanceAccessor la) {
        requireNonNull(la);
        final AtomicReference<MethodHandle> ar = new AtomicReference<>();
        factoryOperation.bean.container.application.addCodegenAction(() -> {
            MethodHandle mh = factoryOperation.generateMethodHandle();
            ar.set(mh);
        });

        factoryOperation.bean.container.lifetime.pool.addOrdered(p -> {
            MethodHandle mh = ar.get();
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

    void generateExportedServiceLocator() {
        this.runtimeEntries = CollectionUtil.copyOf(nodes, n -> generateForExport2(n));
    }

    ServiceLocator newServiceLocator(PackedExtensionContext region) {
        return new PackedServiceLocator(region, Map.copyOf(runtimeEntries));
    }

    void provideService(ProvidedService provider) {
        nodes.put(provider.entry.key, provider);
    }

    private static MethodHandle generateForExport2(ProvidedService pis) {
        MethodHandle mh;
        OperationSetup o = pis.operation;

        BeanInstanceAccessor accessor = null;
        if (o instanceof OperationSetup.BeanAccessOperationSetup) {
            accessor = o.bean.lifetimePoolAccessor;
            // test if prototype bean
            if (accessor == null && o.bean.sourceKind != BeanSourceKind.INSTANCE) {
                o = o.bean.operations.get(0);
            }
        }
        if (pis.isBeanInstance() && pis.bean.sourceKind == BeanSourceKind.INSTANCE) {
            // It is a a constant
            mh = MethodHandles.constant(Object.class, pis.bean.source);
            mh = MethodHandles.dropArguments(mh, 0, PackedExtensionContext.class);
        } else if (accessor != null) {
            mh = MethodHandles.insertArguments(PackedExtensionContext.MH_CONSTANT_POOL_READER, 1, accessor.index());
        } else {
            mh = o.generateMethodHandle();
        }
        mh = mh.asType(mh.type().changeReturnType(Object.class));
        assert (mh.type().equals(MethodType.methodType(Object.class, PackedExtensionContext.class)));
        return mh;
    }
}
