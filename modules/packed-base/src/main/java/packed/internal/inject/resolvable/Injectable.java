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
package packed.internal.inject.resolvable;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import packed.internal.component.Region;
import packed.internal.component.Resolver;
import packed.internal.component.SourceAssembly;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.service.AtProvides;

/**
 *
 */

// Limitations
// Everything must have a source
// Injectable...
// Har vi en purpose????? Taenker ja
// Fordi vi skal bruge den til at resolve...
// Vi har ikke nogen region index, fordi det boerg ligge hos dependencien

// Vi skal have noget PackletModel. Tilhoere @Get. De her 3 AOP ting skal vikles rundt om MHs

// Something with dependencis
public final class Injectable {

    public boolean detectForCycles;

    MethodHandle buildMethodHandle;

    /** The dependencies that must be resolved. */
    public final List<ServiceDependency> dependencies;

    /** A direct method handle. */
    public final MethodHandle directMethodHandle;

    /** Resolved dependencies. */
    public final DependencyProvider[] resolved;

    /** The source (component) this injectable belongs to. */
    public final SourceAssembly source;

    private final BuildtimeService<?> buildEntry;

    public BuildtimeService<?> entry() {
        if (buildEntry == null) {
            return source.service;
        }
        return buildEntry;
    }

    private Injectable(SourceAssembly source) {
        this.source = requireNonNull(source);
        this.dependencies = source.dependencies();
        this.directMethodHandle = source.fromFactory();
        this.resolved = new DependencyProvider[dependencies.size()];
        this.detectForCycles = true;// resolved.length > 0;
        if (detectForCycles) {
            source.component.container.im.dependencies().detectCyclesFor.add(this);
        }
        buildEntry = null;
    }

    private Injectable(BuildtimeService<?> buildEntry, SourceAssembly source, AtProvides ap) {
        this.source = requireNonNull(source);
        this.dependencies = ap.dependencies;
        this.directMethodHandle = ap.methodHandle;
        this.buildEntry = requireNonNull(buildEntry);
        this.resolved = new DependencyProvider[directMethodHandle.type().parameterCount()];
        if (resolved.length != dependencies.size()) {
            resolved[0] = source;
        }
        // Vi detecter altid circle lige nu. Fordi circle detectionen.
        // ogsaa gemmer service instantierings raekkefoelgen
        this.detectForCycles = resolved.length > 0;// dependencies.size() > 0;
        if (detectForCycles) {
            source.component.container.im.dependencies().detectCyclesFor.add(this);
        }
        if (!ap.isStaticMember && source.injectable() != null) {
            ArrayList<Injectable> al = source.component.container.im.dependencies().detectCyclesFor;
            if (!al.contains(source.injectable())) {
                System.out.println("________");
                al.add(source.injectable());
                source.injectable().detectForCycles = true;
            }
        }
    }

    public boolean hasUnresolved() {
        for (int i = 0; i < resolved.length; i++) {
            if (resolved[i] == null) {
                return true;
            }
        }
        return false;
    }

    public Class<?> rawType() {
        return directMethodHandle.type().returnType();
    }

    public void resolve(Resolver resolver) {
        int startIndex = resolved.length != dependencies.size() ? 1 : 0;
        for (int i = 0; i < dependencies.size(); i++) {
            resolved[i + startIndex] = resolver.resolve(this, dependencies.get(i));
            requireNonNull(resolved[i + startIndex]);
        }
    }

    public final MethodHandle buildMethodHandle() {
        if (buildMethodHandle != null) {
            return buildMethodHandle;
        }
        // Does not have have dependencies.
        if (resolved.length == 0) {
            buildMethodHandle = MethodHandles.dropArguments(directMethodHandle, 0, Region.class);
            return buildMethodHandle;
        }
        MethodHandle mh = directMethodHandle;
        for (int i = 0; i < resolved.length; i++) {
            DependencyProvider dp = resolved[i];
            requireNonNull(dp);
            MethodHandle dep = dp.toMethodHandle();
            System.out.println();
            System.out.println("Old " + mh + " i= " + i + "  new TYPE " + dep.type());

            mh = MethodHandles.collectArguments(mh, i, dep);
        }

        MethodType mt = MethodType.methodType(mh.type().returnType(), Region.class);
        int[] ar = new int[mh.type().parameterCount()];
        buildMethodHandle = MethodHandles.permuteArguments(mh, mt, ar);
        if (buildMethodHandle.type().parameterCount() != 1) {
            throw new IllegalStateException();
        }
        return buildMethodHandle;
    }

    /**
     * The source this injectable belongs to.
     * 
     * @return the source this injectable belongs to.
     */
    public SourceAssembly source() {
        return source;
    }

    /**
     * Create a new injectable from the factory of the specified source.
     * 
     * @param source
     *            the source
     * @return a new injectable
     */
    public static Injectable ofFactory(SourceAssembly source) {
        return new Injectable(source);
    }

    public static Injectable ofDeclaredMember(BuildtimeService<?> buildEntry, SourceAssembly source, AtProvides ap) {
        return new Injectable(buildEntry, source, ap);
    }
}
