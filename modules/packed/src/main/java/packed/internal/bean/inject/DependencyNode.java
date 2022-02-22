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
package packed.internal.bean.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

import app.packed.base.Key;
import packed.internal.bean.BeanInstanceDependencyNode;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.hooks.usesite.BeanMemberDependencyNode;
import packed.internal.bean.hooks.usesite.HookModel;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionApplicationSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.inject.service.ServiceDelegate;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.lifetime.LifetimePool;
import packed.internal.lifetime.LifetimePoolSetup;
import packed.internal.lifetime.LifetimePoolWriteable;
import packed.internal.lifetime.PoolEntryHandle;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public abstract sealed class DependencyNode implements LifetimePoolWriteable permits BeanInstanceDependencyNode,BeanMemberDependencyNode {

    /** The bean this dependency consumer is a part of. */
    public final BeanSetup bean;

    /** The dependencies that must be resolved. */
    public final List<InternalDependency> dependencies;

    public boolean needsPostProcessing = true;

    /** A method handle to the underlying constructor, method, field, factory ect. */
    public final MethodHandle originalMethodHandle;

    /** Resolved dependencies. Must match the number of parameters in {@link #originalMethodHandle}. */
    public final DependencyProducer[] producers;

    public final int providerDelta;

    //////////////////////////////////////////////
    //////////////////////////////////////////////

    /** The method handle that is used at runtime and delegates to {@link #originalMethodHandle} */
    MethodHandle runtimeMethodHandle;


    // Field/Method hook
    protected DependencyNode(BeanSetup bean, List<InternalDependency> dependencies, MethodHandle mh, DependencyProducer[] dependencyProviders) {
        this.bean = requireNonNull(bean);

        this.dependencies = dependencies;
        this.originalMethodHandle = mh;

        this.producers = dependencyProviders;
        this.providerDelta = producers.length == dependencies.size() ? 0 : 1;
    }

    // All dependencies have been successfully resolved
    /**
     * All of this consumers dependencies have been resolved
     * 
     * @param pool
     */
    public void onAllDependenciesResolved(LifetimePoolSetup pool) {
        // If analysis we should not need to create method handles...

        // If the injectable is a constant we need should to store an instance of it in the runtime region.
        // We do this here because the the cycle detection algorithm explorers the dependency BFS. So
        // we add each node on exit when all of its dependency have already been added. In this way
        // guarantee that all dependencies have already been visited
        if (poolAccessor() != null) {
            pool.addOrdered(this);
            pool.postProcessing.add(() -> runtimeMethodHandle());
        }
        needsPostProcessing = false;

    }

    protected abstract PoolEntryHandle poolAccessor();


    public void resolve(ServiceManagerSetup sbm) {
        boolean buildMH = true;
        for (int i = 0; i < dependencies.size(); i++) {
            int providerIndex = i + providerDelta;
            if (producers[providerIndex] == null) {
                InternalDependency sd = dependencies.get(i);
                DependencyProducer e = null;
                if (bean != null) {
                    //// Checker om der er hooks der provider servicen
                    HookModel sm = bean.hookModel;
                    if (sm.sourceServices != null) {
                        e = sm.sourceServices.get(sd.key());
                    }
                }

                if (sbm != null) {
                    if (e == null) {
                        if (bean.realm instanceof ExtensionApplicationSetup ers) {
                            Key<?> requiredKey = sd.key();
                            Key<?> thisKey = Key.of(bean.hookModel.clazz);
                            ContainerSetup parent = bean.parent;
                            ExtensionSetup es = parent.useExtensionSetup(ers.realmType(), null);
                            BeanSetup bs = null;
                            if (thisKey.equals(requiredKey)) {
                                if (es.parent != null) {
                                    bs = es.parent.beans.lookup(requiredKey);
                                }
                            } else {
                                bs = es.beans.lookup(requiredKey);
                            }

                            e = bs;

                        } else {
                            ServiceDelegate wrapper = sbm.resolvedServices.get(sd.key());
                            e = wrapper == null ? null : wrapper.getSingle();
                        }
                    }

                    sbm.dependencies().recordResolvedDependency(this, i, sd, e, false);
                }
                producers[providerIndex] = e;

                if (e == null) {
                    buildMH = false;
                }
            }
        }
        // Den er lidt her midlertidigt...
        if (buildMH) {
            runtimeMethodHandle();
        }
    }

    /**
     * 
     * <p>
     * All possible configuration issues from users and extension should already have been checked by this point. If this
     * method fails it is an internal problem with packed.
     * 
     * @return the runtime method handle
     */
    public final MethodHandle runtimeMethodHandle() {
        // See if we have already build the runtime method handle
        MethodHandle mh = runtimeMethodHandle;
        if (mh != null) {
            return mh;
        }

        // Temporary check, All dependencies should have been resolved by now
        if (producers != null) {
            for (int i = 0; i < producers.length; i++) {
                requireNonNull(producers[i]);
            }
        }

        // We create the runtime method handle a little different, depending on the
        // number of dependencies/producers it has
        if (producers.length == 0) {
            mh = MethodHandles.dropArguments(originalMethodHandle, 0, LifetimePool.class);
        } else if (producers.length == 1) {
            mh = MethodHandles.collectArguments(originalMethodHandle, 0, producers[0].dependencyAccessor());
        } else {
            mh = originalMethodHandle;

            // We create a new method that a
            for (int i = 0; i < producers.length; i++) {
                mh = MethodHandles.collectArguments(mh, i, producers[i].dependencyAccessor());
            }

            // reduce (RuntimeRegion, *)X -> (RuntimeRegion)X
            MethodType mt = MethodType.methodType(originalMethodHandle.type().returnType(), LifetimePool.class);
            mh = MethodHandles.permuteArguments(mh, mt, new int[producers.length]);
        }

        return runtimeMethodHandle = mh;
    }

    public void setProducer(int index, DependencyProducer p) {
        int providerIndex = index + providerDelta;
        if (producers[providerIndex] != null) {
            throw new IllegalStateException();
        }
        this.producers[providerIndex] = requireNonNull(p);
    }

    @Override
    public void writeToPool(LifetimePool pool) {
        MethodHandle mh = runtimeMethodHandle();

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
