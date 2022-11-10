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
package internal.app.packed.oldservice.inject;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.framework.Nullable;
import app.packed.service.Key;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.ExtensionTreeSetup;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.lifetime.LifetimeObjectArenaSetup;
import internal.app.packed.lifetime.pool.Accessor;
import internal.app.packed.lifetime.pool.LifetimePoolWriteable;
import internal.app.packed.oldservice.InternalServiceExtension;
import internal.app.packed.oldservice.ServiceDelegate;
import internal.app.packed.oldservice.build.BeanMemberServiceSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public class DependencyNode implements LifetimePoolWriteable {

    /** The dependencies that must be resolved. */
    public final List<InternalDependency> dependencies;

    public boolean needsPostProcessing = true;

    final OperationSetup operation;

    public final int providerDelta;

    final BeanInjectionManager bim;
    protected final BeanMemberServiceSetup service;

    // Field/Method hook
    public DependencyNode(List<InternalDependency> dependencies, OperationSetup operation, MethodHandle mh, int count, BeanMemberServiceSetup service,
            BeanInjectionManager bim) {
        this.operation = operation;
        this.dependencies = dependencies;
        this.providerDelta = count == dependencies.size() ? 0 : 1;
        this.bim = bim;
        this.service = service;
    }

    // All dependencies have been successfully resolved
    /**
     * All of this consumers dependencies have been resolved
     * 
     * @param pool
     */
    public void onAllDependenciesResolved(LifetimeObjectArenaSetup pool) {
        if (poolAccessor() != null) {
            pool.addOrdered(this);
            pool.postProcessing.add(() -> operation.buildInvoker());
        }
        needsPostProcessing = false;

    }

    @Nullable
    protected Accessor poolAccessor() {
        if (bim != null) {
            return bim.lifetimePoolAccessor;
        } else {
            if (service != null) {
                return service.accessor;
            }
            return null;
        }
    }

    public void resolve(InternalServiceExtension sbm) {
        for (int i = 0; i < dependencies.size(); i++) {
            int providerIndex = i + providerDelta;
            InternalDependency sd = dependencies.get(i);
            resolve0(sbm, sd, providerIndex);
        }
    }

    @Nullable
    private Object resolve0(InternalServiceExtension sbm, InternalDependency sd, int i) {
        Object e = null;

        if (sbm != null) {
            if (e == null) {
                if (operation.bean.realm instanceof ExtensionTreeSetup ers) {
                    Key<?> requiredKey = sd.key();
                    Key<?> thisKey = Key.of(operation.bean.beanClass);
                    ContainerSetup container = operation.bean.container;
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
                    ServiceDelegate wrapper = sbm.resolvedServices.get(sd.key());
                    e = wrapper == null ? null : wrapper.getSingle();
                }
            }

            sbm.ios.requirementsOrCreate().recordResolvedDependency(this, i, sd, e, false);
        }
        return e;
    }

    public void setProducer(int index, Object p) {

    }

    @Override
    public void writeToPool(LifetimeObjectArena pool) {
        MethodHandle mh = operation.buildInvoker();

        Object instance;
        try {
            instance = mh.invoke(pool);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        if (instance == null) {
            throw new NullPointerException(this + " returned null");
        }

        poolAccessor().store(pool, instance);
    }
}
