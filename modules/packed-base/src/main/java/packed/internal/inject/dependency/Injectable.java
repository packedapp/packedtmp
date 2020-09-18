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
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;
import packed.internal.inject.service.assembly.ServiceAssembly;
import packed.internal.inject.sidecar.AtProvides;

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

    private final ServiceAssembly<?> buildEntry;

    MethodHandle buildMethodHandle;

    /** The dependencies that must be resolved. */
    public final List<DependencyDescriptor> dependencies;

    public boolean needsPostProcessing;

    /** A direct method handle. */
    public final MethodHandle directMethodHandle;

    /** Resolved dependencies. */
    public final DependencyProvider[] resolved;

    /** The source (component) this injectable belongs to. */
    public final SourceAssembly source;

    public final InjectionManager im;

    @Nullable
    public final SourceModelMember sourceMember;

    public Injectable(ServiceAssembly<?> buildEntry, SourceAssembly source, AtProvides ap) {
        this.source = requireNonNull(source);
        this.dependencies = ap.dependencies;
        this.directMethodHandle = ap.methodHandle;
        this.buildEntry = requireNonNull(buildEntry);
        this.resolved = new DependencyProvider[directMethodHandle.type().parameterCount()];
        this.im = source.compConf.injectionManager();
        if (resolved.length != dependencies.size()) {
            resolved[0] = source;
        }
        // Vi detecter altid circle lige nu. Fordi circle detectionen.
        // ogsaa gemmer service instantierings raekkefoelgen

        // Det er jo faktisk helt ned til en sidecar vi skal instantiere det foer
        // selve servicen...

        // We have moved all the logic for adding some to various list.
        // Down into the dependency cycle check.
        needsPostProcessing = true;
        this.sourceMember = null;
    }

    public Injectable(SourceAssembly source, SourceModelMethod smm) {
        this.source = requireNonNull(source);
        this.im = source.compConf.injectionManager();
        this.sourceMember = requireNonNull(smm);

        buildEntry = null;
        dependencies = null;
        directMethodHandle = null;
        resolved = null;
    }

    public Injectable(SourceAssembly source, BaseFactory<?> factory) {
        this.source = requireNonNull(source);

        FactoryHandle<?> handle = factory.factory.handle;
        MethodHandle mh = source.compConf.realm.fromFactoryHandle(handle);
        this.im = source.compConf.injectionManager();
        this.dependencies = factory.factory.dependencies;
        this.directMethodHandle = requireNonNull(mh);
        this.resolved = new DependencyProvider[dependencies.size()];
        this.needsPostProcessing = true;// resolved.length > 0;
        buildEntry = null; // Any build entry is stored in SourceAssembly#service
        this.sourceMember = null;
    }

    public final MethodHandle buildMethodHandle() {
        if (buildMethodHandle != null) {
            return buildMethodHandle;
        }
        // Does not have have dependencies.
        if (resolved.length == 0) {
            return buildMethodHandle = MethodHandles.dropArguments(directMethodHandle, 0, RuntimeRegion.class);
        }

        // We create a new method that a
        MethodHandle mh = directMethodHandle;
        for (int i = 0; i < resolved.length; i++) {
            DependencyProvider dp = resolved[i];
            MethodHandle dep = dp.dependencyAccessor();
            mh = MethodHandles.collectArguments(mh, i, dep);
        }
        // We may e

        // We may need to reduce (RuntimeRegion, RuntimeRegion*)X -> (RuntimeRegion)X
        if (resolved.length > 1) {
            MethodType mt = MethodType.methodType(mh.type().returnType(), RuntimeRegion.class);
            buildMethodHandle = MethodHandles.permuteArguments(mh, mt, new int[resolved.length]);
        } else {
            buildMethodHandle = mh;
        }
        if (buildMethodHandle.type().parameterCount() != 1) {
            throw new IllegalStateException();
        }
        return buildMethodHandle;
    }

    public int regionIndex() {
        // buildEntry is null if it this Injectable is created from a source and not @AtProvides
        // In which case we store the build entry (if available) in the source instead
        if (buildEntry == null) {
            return source.regionIndex;
        }
        return buildEntry.regionIndex();
    }

    public boolean hasUnresolved() {
        for (int i = 0; i < resolved.length; i++) {
            if (resolved[i] == null) {
                return true;
            }
        }
        return false;
    }

    public void resolve() {
        int startIndex = resolved.length != dependencies.size() ? 1 : 0;
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyDescriptor sd = dependencies.get(i);
            DependencyProvider e = null;
            if (source != null) {
                SourceModel sm = source.model;
                if (sm.sourceServices != null) {
                    MethodHandle mh = sm.sourceServices.get(sd.key());
                    if (mh != null) {
                        e = new SidecarProvideDependency(mh);
                    }
                }
            }
            if (e == null) {
                e = im.services().resolvedServices.get(sd.key());
            }
            im.services().dependencies().recordResolvedDependency(im, this, sd, e, false);
            // may be null, in which case it is a required service that must be provided.
            // By the user
            resolved[i + startIndex] = e;
        }
    }
}
