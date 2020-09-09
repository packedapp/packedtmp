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
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.service.AtProvides;
import packed.internal.service.buildtime.service.Z2;
import packed.internal.util.ThrowableUtil;

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

    private final BuildEntry<?> buildEntry;

    public BuildEntry<?> entry() {
        if (buildEntry == null) {
            return source.service;
        }
        return buildEntry;
    }

    private Injectable(SourceAssembly source) {
        this.source = requireNonNull(source);
        this.dependencies = source.driver.factory.factory.dependencies;
        this.directMethodHandle = source.driver.fromFactory(source.component);
        this.resolved = new DependencyProvider[dependencies.size()];
        this.detectForCycles = true;// resolved.length > 0;
        if (detectForCycles) {
            source.component.container.im.dependencies().detectCyclesFor.add(this);
        }
        buildEntry = null;
    }

    private Injectable(BuildEntry<?> buildEntry, SourceAssembly source, AtProvides ap) {
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
        System.out.println();
        System.out.println("Trying to generate MethodHandle for " + directMethodHandle);
        // Does not have have dependencies.
        if (resolved.length == 0) {
            buildMethodHandle = MethodHandles.dropArguments(directMethodHandle, 0, Region.class);
            return buildMethodHandle;
        }
        System.out.println("----");
        MethodHandle mh = directMethodHandle;
        System.out.println("Direct " + directMethodHandle);
        for (int i = 0; i < resolved.length; i++) {
            DependencyProvider dp = resolved[i];
            requireNonNull(dp);
            MethodHandle dep = dp.toMethodHandle();
            System.out.println("Dep " + i + " " + dep);
            try {
                dep.invoke(new Region(123));
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mh = MethodHandles.collectArguments(mh, i, dep);
            System.out.println(mh);
            Region reg = new Region(123);
            reg.store[1] = new Z2.NoDep();
            try {
                mh.invoke(reg);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
            // reg.store[1] = null;
            try {
                mh.invoke(reg);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
        System.out.println("----------");

        MethodType mt = MethodType.methodType(mh.type().returnType(), Region.class);
        int[] ar = new int[mh.type().parameterCount()];
        buildMethodHandle = MethodHandles.permuteArguments(mh, mt, ar);

        buildMethodHandle = mh;
        System.out.println(buildMethodHandle);
        System.out.println("----------");

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

    public static Injectable ofDeclaredMember(BuildEntry<?> buildEntry, SourceAssembly source, AtProvides ap) {
        return new Injectable(buildEntry, source, ap);
    }
}
