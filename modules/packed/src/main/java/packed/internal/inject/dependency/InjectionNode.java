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
package packed.internal.inject.dependency;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.build.BuildException;
import packed.internal.component.bean.BeanSetup;
import packed.internal.hooks.usesite.BootstrappedClassModel;
import packed.internal.hooks.usesite.UseSiteMemberHookModel;
import packed.internal.hooks.usesite.UseSiteMethodHookModel;
import packed.internal.inject.service.ServiceDelegate;
import packed.internal.inject.service.ServiceManagerSetup;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.inject.service.build.SourceMemberServiceSetup;
import packed.internal.lifetime.LifetimePool;
import packed.internal.lifetime.LifetimePoolMethodAccessor;
import packed.internal.lifetime.LifetimePoolSetup;
import packed.internal.lifetime.LifetimePoolWriteable;
import packed.internal.lifetime.PoolAccessor;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class InjectionNode implements LifetimePoolWriteable {

    @Nullable
    private final SourceMemberServiceSetup service;

    MethodHandle buildMethodHandle;

    /** The dependencies that must be resolved. */
    public final List<DependencyDescriptor> dependencies;

    /** A direct method handle. */
    public final MethodHandle directMethodHandle;

    public boolean needsPostProcessing = true;

    /** Resolved dependencies. Must match the number of parameters in {@link #directMethodHandle}. */
    public final DependencyProducer[] providers;

    /** The source (component) this dependent is or is a part of. */
    public final BeanSetup source;

    @Nullable
    private final UseSiteMemberHookModel sourceMember;

    public final int providerDelta;

    public InjectionNode(BeanSetup source, List<DependencyDescriptor> dependencies, MethodHandle mh) {
        this.source = requireNonNull(source);
        this.sourceMember = null;

        this.service = null; // Any build entry is stored in SourceAssembly#service
        this.dependencies = dependencies;
        this.directMethodHandle = mh;

        this.providerDelta = 0;
        this.providers = new DependencyProducer[directMethodHandle.type().parameterCount()];
    }

    public InjectionNode(BeanSetup source, UseSiteMemberHookModel smm, DependencyProducer[] dependencyProviders) {
        this.source = requireNonNull(source);
        this.sourceMember = requireNonNull(smm);

        if (smm.provideAskey != null) {
            if (!Modifier.isStatic(smm.getModifiers()) && source.singletonAccessor == null) {
                throw new BuildException("Not okay)");
            }
            ServiceManagerSetup sbm = source.parent.injection.getServiceManagerOrCreate();
            ServiceSetup sa = this.service = new SourceMemberServiceSetup(sbm, source, this, smm.provideAskey, smm.provideAsConstant);
            sbm.addAssembly(sa);
        } else {
            this.service = null;
        }
        this.dependencies = smm.dependencies;
        this.directMethodHandle = smm.methodHandle();

        this.providers = dependencyProviders;
        this.providerDelta = providers.length == dependencies.size() ? 0 : 1;

        if (!Modifier.isStatic(smm.getModifiers())) {
            dependencyProviders[0] = source;
        }
    }

    public final MethodHandle buildMethodHandle() {
        MethodHandle mh = buildMethodHandle;
        if (mh != null) {
            return mh;
        }

        if (providers.length == 0) {
            return buildMethodHandle = MethodHandles.dropArguments(directMethodHandle, 0, LifetimePool.class);
        } else if (providers.length == 1) {
            requireNonNull(providers[0]);
            // System.out.println(providers[0].getClass());
            // System.out.println(providers[0].dependencyAccessor());
            return buildMethodHandle = MethodHandles.collectArguments(directMethodHandle, 0, providers[0].dependencyAccessor());
        } else {
            mh = directMethodHandle;

            // We create a new method that a
            for (int i = 0; i < providers.length; i++) {
                DependencyProducer dp = providers[i];
                requireNonNull(dp);
                mh = MethodHandles.collectArguments(mh, i, dp.dependencyAccessor());
            }
            // reduce (RuntimeRegion, *)X -> (RuntimeRegion)X
            MethodType mt = MethodType.methodType(directMethodHandle.type().returnType(), LifetimePool.class);
            return buildMethodHandle = MethodHandles.permuteArguments(mh, mt, new int[providers.length]);
        }
    }

    @Nullable
    private PoolAccessor poolAccessor() {
        // buildEntry is null if it this Injectable is created from a source and not @AtProvides
        // In which case we store the build entry (if available) in the source instead
        if (service != null) {
            return service.accessor;
        } else if (sourceMember != null) {
            // AAhhhh vi bliver jo ogsaa noedt til at lave sidecars
            return null;
        }
        return source.singletonAccessor;
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
            pool.postProcessing.add(() -> buildMethodHandle());
        }
        needsPostProcessing = false;

        if (sourceMember != null) {
            if (source.singletonAccessor != null) {
                // Maybe shared with SourceAssembly
//                if (sourceMember.runAt == RunAt.INITIALIZATION) {
//
//                }
                if (sourceMember.provideAskey == null) {
                    MethodHandle mh1 = buildMethodHandle();

                    // RuntimeRegionInvoker
                    // the method on the sidecar: sourceMember.model.onInitialize

                    // MethodHandle(Invoker)void -> MethodHandle(MethodHandle,RuntimeRegion)void
                    if (sourceMember instanceof UseSiteMethodHookModel msm) {
                        if (msm.bootstrapModel.onInitialize != null) {
                            // System.out.println(msm.model.onInitialize);
                            MethodHandle mh2 = MethodHandles.collectArguments(msm.bootstrapModel.onInitialize, 0, LifetimePoolMethodAccessor.MH_INVOKER);

                            mh2 = mh2.bindTo(mh1);

                            source.application.container.lifetime.initializers.add(mh2);
                        }
                    }
                }
            }
        }
    }

    public void setDependencyProvider(int index, DependencyProducer p) {
        int providerIndex = index + providerDelta;
        if (providers[providerIndex] != null) {
            throw new IllegalStateException();
        }
        this.providers[providerIndex] = requireNonNull(p);
    }

    public void resolve(ServiceManagerSetup sbm) {
        boolean buildMH = true;
        for (int i = 0; i < dependencies.size(); i++) {
            int providerIndex = i + providerDelta;
            if (providers[providerIndex] == null) {
                DependencyDescriptor sd = dependencies.get(i);
                DependencyProducer e = null;
                if (source != null) {
                    BootstrappedClassModel sm = source.hookModel;
                    if (sm.sourceServices != null) {
                        e = sm.sourceServices.get(sd.key());
                    }
                }
                if (sbm != null) {
                    if (e == null) {
                        ServiceDelegate wrapper = sbm.resolvedServices.get(sd.key());
                        e = wrapper == null ? null : wrapper.getSingle();
                    }

                    sbm.dependencies().recordResolvedDependency(this, i, sd, e, false);
                }
                providers[providerIndex] = e;

                if (e == null) {
                    buildMH = false;
                }
            }
        }
        // Den er lidt her midlertidigt...
        if (buildMH) {
            buildMethodHandle();
        }
    }

    @Override
    public void writeToPool(LifetimePool pool) {
        MethodHandle mh = buildMethodHandle();

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
