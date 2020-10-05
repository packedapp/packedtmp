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
import java.util.List;

import app.packed.base.Nullable;
import packed.internal.component.RegionAssembly;
import packed.internal.component.RuntimeRegion;
import packed.internal.component.SourceAssembly;
import packed.internal.component.SourceModel;
import packed.internal.component.SourceModelSidecarMethod;
import packed.internal.component.SourceModelSidecarMethod.RunAt;
import packed.internal.inject.InjectionManager;
import packed.internal.inject.service.assembly.AtProvideServiceAssembly;
import packed.internal.inject.sidecar.AtProvides;
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
public class Injectable {

    @Nullable
    private final AtProvideServiceAssembly<?> service;

    MethodHandle buildMethodHandle;

    /** The dependencies that must be resolved. */
    public final List<DependencyDescriptor> dependencies;

    /** A direct method handle. */
    public final MethodHandle directMethodHandle;

    public boolean needsPostProcessing = true;

    /** Resolved dependencies. Must match the number of parameters in {@link #directMethodHandle}. */
    public final DependencyProvider[] providers;

    /** The source (component) this injectable belongs to. */
    public final SourceAssembly source;

    @Nullable
    public final SourceModelSidecarMethod sourceMember;

    public final int providerDelta;

    // Creates the Source...
    public Injectable(SourceAssembly source, List<DependencyDescriptor> dependencies, MethodHandle mh) {
        this.source = requireNonNull(source);
        this.sourceMember = null;

        this.service = null; // Any build entry is stored in SourceAssembly#service
        this.dependencies = dependencies;
        this.directMethodHandle = mh;

        this.providerDelta = 0;
        this.providers = new DependencyProvider[directMethodHandle.type().parameterCount()];
    }

    public Injectable(SourceAssembly source, SourceModelSidecarMethod smm) {
        this.source = requireNonNull(source);
        this.sourceMember = requireNonNull(smm);

        this.service = null;
        this.dependencies = smm.dependencies;
        this.directMethodHandle = smm.directMethodHandle;

        this.providers = smm.createProviders();

        if (providers.length != dependencies.size()) {
            providers[0] = source;
            this.providerDelta = 1;
        } else {
            this.providerDelta = 0;
        }
    }

    public Injectable(SourceAssembly source, AtProvideServiceAssembly<?> buildEntry, AtProvides ap) {
        this.source = requireNonNull(source);
        this.sourceMember = null;

        this.service = requireNonNull(buildEntry);
        this.dependencies = ap.dependencies;
        this.directMethodHandle = ap.methodHandle;

        this.providers = new DependencyProvider[directMethodHandle.type().parameterCount()];

        if (providers.length != dependencies.size()) {
            providers[0] = source;
            this.providerDelta = 1;
        } else {
            this.providerDelta = 0;
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
            return buildMethodHandle = MethodHandles.collectArguments(directMethodHandle, 0, providers[0].dependencyAccessor());
        } else {
            mh = directMethodHandle;

            // We create a new method that a
            for (int i = 0; i < providers.length; i++) {
                DependencyProvider dp = providers[i];
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
        if (sourceMember != null) {
            // AAhhhh vi bliver jo ogsaa noedt til at lave sidecars
            return -1;
        } else if (service == null) {
            return source.regionIndex;
        }
        return service.regionIndex;
    }

    public void onResolveSuccess(RegionAssembly region) {
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
                MethodHandle mh1 = buildMethodHandle();

                // RuntimeRegionInvoker
                // the method on the sidecar: sourceMember.model.onInitialize

                // MethodHandle(Invoker)void -> MethodHandle(MethodHandle,RuntimeRegion)void
                MethodHandle mh2 = MethodHandles.collectArguments(sourceMember.model.onInitialize, 0, RuntimeRegionInvoker.MH_INVOKER);

                System.out.println(mh2);
                mh2 = mh2.bindTo(mh1);

                region.initializers.add(mh2);
            }
        }
    }

    public void resolve(InjectionManager im) {
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

                if (e == null) {
                    e = im.services(true).resolvedServices.get(sd.key());
                }

                im.services(true).dependencies().recordResolvedDependency(im, this, i, sd, e, false);
                providers[providerIndex] = e;
            }
        }
    }
}
