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
import packed.internal.component.RuntimeRegion;
import packed.internal.component.SourceAssembly;
import packed.internal.component.SourceModel;
import packed.internal.component.SourceModelMember;
import packed.internal.component.SourceModelMethod;
import packed.internal.inject.InjectionManager;
import packed.internal.inject.service.assembly.AtProvideServiceAssembly;
import packed.internal.inject.sidecar.AtProvides;
import packed.internal.sidecar.SidecarDependencyProvider;

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

    /** Resolved dependencies. */
    public final DependencyProvider[] resolved;

    /** The source (component) this injectable belongs to. */
    public final SourceAssembly source;

    @Nullable
    public final SourceModelMember sourceMember;

    public Injectable(SourceAssembly source, List<DependencyDescriptor> dependencies, MethodHandle mh) {
        this.source = requireNonNull(source);
        this.sourceMember = null;

        this.service = null; // Any build entry is stored in SourceAssembly#service
        this.dependencies = dependencies;
        this.directMethodHandle = mh;

        this.resolved = new DependencyProvider[directMethodHandle.type().parameterCount()];
    }

    public Injectable(SourceAssembly source, AtProvideServiceAssembly<?> buildEntry, AtProvides ap) {
        this.source = requireNonNull(source);
        this.sourceMember = null;

        this.service = requireNonNull(buildEntry);
        this.dependencies = ap.dependencies;
        this.directMethodHandle = ap.methodHandle;

        this.resolved = new DependencyProvider[directMethodHandle.type().parameterCount()];

        if (resolved.length != dependencies.size()) {
            resolved[0] = source;
        }
    }

    public Injectable(SourceAssembly source, SourceModelMethod smm) {
        this.source = requireNonNull(source);
        this.sourceMember = requireNonNull(smm);

        this.service = null;
        this.dependencies = smm.dependencies;
        this.directMethodHandle = smm.directMethodHandle;

        this.resolved = smm.createProviders();
    }

    public final MethodHandle buildMethodHandle() {
        MethodHandle mh = buildMethodHandle;
        if (mh != null) {
            return mh;
        }

        if (resolved.length == 0) {
            return buildMethodHandle = MethodHandles.dropArguments(directMethodHandle, 0, RuntimeRegion.class);
        } else if (resolved.length == 1) {
            return buildMethodHandle = MethodHandles.collectArguments(directMethodHandle, 0, resolved[0].dependencyAccessor());
        } else {
            mh = directMethodHandle;

            // We create a new method that a
            for (int i = 0; i < resolved.length; i++) {
                DependencyProvider dp = resolved[i];
                mh = MethodHandles.collectArguments(mh, i, dp.dependencyAccessor());
            }
            // reduce (RuntimeRegion, *)X -> (RuntimeRegion)X
            MethodType mt = MethodType.methodType(directMethodHandle.type().returnType(), RuntimeRegion.class);
            return buildMethodHandle = MethodHandles.permuteArguments(mh, mt, new int[resolved.length]);
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

    public void resolve() {
        InjectionManager im = source.compConf.injectionManager();
        int startIndex = resolved.length != dependencies.size() ? 1 : 0;
        for (int i = 0; i < dependencies.size(); i++) {
            if (resolved[i + startIndex] == null) {
                DependencyDescriptor sd = dependencies.get(i);
                DependencyProvider e = null;
                if (source != null) {
                    SourceModel sm = source.model;
                    if (sm.sourceServices != null) {
                        SidecarDependencyProvider mh = sm.sourceServices.get(sd.key());
                        if (mh != null) {
                            e = new SidecarProvideDependency(mh.dependencyAccessor());
                        }
                    }
                }
                if (e == null) {
                    e = im.services(true).resolvedServices.get(sd.key());
                }
                im.services(true).dependencies().recordResolvedDependency(im, this, sd, e, false);
                // may be null, in which case it is a required service that must be provided.
                // By the user
                resolved[i + startIndex] = e;
            }
        }
    }
}
