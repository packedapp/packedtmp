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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.InvalidDeclarationException;
import app.packed.base.Nullable;
import packed.internal.component.BuildtimeRegion;
import packed.internal.component.ComponentBuild;
import packed.internal.component.RuntimeRegion;
import packed.internal.component.source.SourceBuild;
import packed.internal.component.source.SourceModel;
import packed.internal.component.source.SourceModelMember;
import packed.internal.component.source.SourceModelMethod;
import packed.internal.component.source.SourceModelMethod.RunAt;
import packed.internal.inject.service.ServiceComposer;
import packed.internal.inject.service.Wrapper;
import packed.internal.inject.service.build.BuildtimeService;
import packed.internal.inject.service.build.SourceMemberBuildtimeService;
import packed.internal.sidecar.RuntimeRegionInvoker;

/**
 *
 */

// Types....

// Source Factory/Class
// @Provides
// Other Method

// Limitations
// Everything must have a source
// Injectable...
// Har vi en purpose????? Taenker ja
// Fordi vi skal bruge den til at resolve...
// Vi har ikke nogen region index, fordi det boerg ligge hos dependencien

// Vi skal have noget PackletModel. Tilhoere @Get. De her 3 AOP ting skal vikles rundt om MHs

// Something with dependencis
public class Dependant {

    @Nullable
    private final SourceMemberBuildtimeService service;

    MethodHandle buildMethodHandle;

    /** The dependencies that must be resolved. */
    public final List<DependencyDescriptor> dependencies;

    /** A direct method handle. */
    public final MethodHandle directMethodHandle;

    public boolean needsPostProcessing = true;

    /** Resolved dependencies. Must match the number of parameters in {@link #directMethodHandle}. */
    public final DependencyProvider[] providers;

    /** The source (component) this dependent is or is a part of. */
    public final SourceBuild source;

    @Nullable
    private final SourceModelMember sourceMember;

    public final int providerDelta;

    public Dependant(SourceBuild source, List<DependencyDescriptor> dependencies, MethodHandle mh) {
        this.source = requireNonNull(source);
        this.sourceMember = null;

        this.service = null; // Any build entry is stored in SourceAssembly#service
        this.dependencies = dependencies;
        this.directMethodHandle = mh;

        this.providerDelta = 0;
        this.providers = new DependencyProvider[directMethodHandle.type().parameterCount()];
    }

    public Dependant(ComponentBuild compConf, SourceBuild source, SourceModelMember smm, DependencyProvider[] dependencyProviders) {
        this.source = requireNonNull(source);
        this.sourceMember = requireNonNull(smm);

        if (smm.provideAskey != null) {
            if (!Modifier.isStatic(smm.getModifiers()) && source.regionIndex == -1) {
                throw new InvalidDeclarationException("Not okay)");
            }
            ServiceComposer sbm = compConf.memberOfCube.getServiceManagerOrCreate();
            BuildtimeService sa = this.service = new SourceMemberBuildtimeService(sbm, compConf, this, smm.provideAskey, smm.provideAsConstant);
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
            return buildMethodHandle = MethodHandles.dropArguments(directMethodHandle, 0, RuntimeRegion.class);
        } else if (providers.length == 1) {
            requireNonNull(providers[0]);
            System.out.println(providers[0].getClass());
            System.out.println(providers[0].dependencyAccessor());
            return buildMethodHandle = MethodHandles.collectArguments(directMethodHandle, 0, providers[0].dependencyAccessor());
        } else {
            mh = directMethodHandle;

            // We create a new method that a
            for (int i = 0; i < providers.length; i++) {
                DependencyProvider dp = providers[i];
                requireNonNull(dp);
                mh = MethodHandles.collectArguments(mh, i, dp.dependencyAccessor());
            }
            // reduce (RuntimeRegion, *)X -> (RuntimeRegion)X
            MethodType mt = MethodType.methodType(directMethodHandle.type().returnType(), RuntimeRegion.class);
            return buildMethodHandle = MethodHandles.permuteArguments(mh, mt, new int[providers.length]);
        }
    }

    public int regionIndex() {
        // buildEntry is null if it this Injectable is created from a source and not @AtProvides
        // In which case we store the build entry (if available) in the source instead
        if (service != null) {
            return service.regionIndex;
        } else if (sourceMember != null) {
            // AAhhhh vi bliver jo ogsaa noedt til at lave sidecars
            return -1;
        }
        return source.regionIndex;
    }

    public boolean hasUnresolved() {
        if (dependencies.size() == 0) {
            return false;
        }
        for (DependencyProvider p : providers) {
            if (p == null) {
                return true;
            }
        }
        return false;
    }

    // All dependencies have been successfully resolved
    public void onAllDependenciesResolved(BuildtimeRegion region) {
        // If the injectable is a constant we need should to store an instance of it in the runtime region.
        // We do this here because the the cycle detection algorithm explorers the dependency BFS. So
        // we add each node on exit when all of its dependency have already been added. In this way
        // guarantee that all dependencies have already been visited

        if (regionIndex() > -1) {
            region.regionStores.add(this);
        }
        needsPostProcessing = false;

        if (sourceMember != null) {
            if (source.regionIndex > -1) {
                // Maybe shared with SourceAssembly
                if (sourceMember.runAt == RunAt.INITIALIZATION) {

                }
                if (sourceMember.provideAskey == null) {
                    MethodHandle mh1 = buildMethodHandle();

                    // RuntimeRegionInvoker
                    // the method on the sidecar: sourceMember.model.onInitialize

                    // MethodHandle(Invoker)void -> MethodHandle(MethodHandle,RuntimeRegion)void
                    SourceModelMethod msm = (SourceModelMethod) sourceMember;
                    MethodHandle mh2 = MethodHandles.collectArguments(msm.model.onInitialize, 0, RuntimeRegionInvoker.MH_INVOKER);

                    System.out.println(mh2);
                    mh2 = mh2.bindTo(mh1);

                    region.initializers.add(mh2);
                }
            }
        }
    }

    public void setDependencyProvider(int index, DependencyProvider p) {
        int providerIndex = index + providerDelta;
        if (providers[providerIndex] != null) {
            throw new IllegalStateException();
        }
        this.providers[providerIndex] = requireNonNull(p);
    }

    public void resolve(ServiceComposer sbm) {
        for (int i = 0; i < dependencies.size(); i++) {
            int providerIndex = i + providerDelta;
            if (providers[providerIndex] == null) {
                DependencyDescriptor sd = dependencies.get(i);
                DependencyProvider e = null;
                if (source != null) {
                    SourceModel sm = source.model;
                    if (sm.sourceServices != null) {
                        e = sm.sourceServices.get(sd.key());
                    }
                }
                if (sbm != null) {
                    if (e == null) {
                        Wrapper wrapper = sbm.resolvedServices.get(sd.key());
                        e = wrapper == null ? null : wrapper.getSingle();
                    }

                    sbm.dependencies().recordResolvedDependency(this, i, sd, e, false);
                }
                providers[providerIndex] = e;
            }
        }
    }
}
