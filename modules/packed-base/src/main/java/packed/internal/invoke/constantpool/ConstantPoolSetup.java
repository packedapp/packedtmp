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
package packed.internal.invoke.constantpool;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;

import packed.internal.application.ApplicationLaunchContext;
import packed.internal.component.ClassSourceSetup;
import packed.internal.component.PackedApplicationRuntime;
import packed.internal.component.PackedComponent;
import packed.internal.inject.dependency.DependancyConsumer;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class ConstantPoolSetup {

    /**
     * Components that contains constants that should be stored in a region. Is only written by {@link ClassSourceSetup}.
     */
    private final ArrayList<ClassSourceSetup> constants = new ArrayList<>();

    public final ArrayList<MethodHandle> initializers = new ArrayList<>();

    /** The size of the pool. */
    private int size;

    // List of services that must be instantiated and stored in the region
    // They are ordered in the order they should be initialized
    // For now written by DependencyCycleDetector via BFS
    public final ArrayList<DependancyConsumer> regionStores = new ArrayList<>();

    public void addSourceClass(ClassSourceSetup s) {
        constants.add(s);
    }

    public ConstantPool newPool(ApplicationLaunchContext pic, PackedComponent root) {
        ConstantPool pool = new ConstantPool(size);

        // Not sure we want to create the guest here, we do it for now though
        if (root.modifiers().hasRuntime()) {
            pool.store(0, new PackedApplicationRuntime(pic));
        }

        // We start by storing all constant instances in the region array
        for (ClassSourceSetup sa : constants) {
            sa.writeConstantPool(pool);
        }

        // All constants that must be instantiated and stored
        // Order here is very important. As for every constant.
        // Its dependencies are guaranteed to have been already stored
        for (DependancyConsumer injectable : regionStores) {
            MethodHandle mh = injectable.buildMethodHandle();

            Object instance;
            try {
                instance = mh.invoke(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }

            if (instance == null) {
                throw new NullPointerException(injectable + " returned null");
            }

            int index = injectable.regionIndex();
            pool.store(index, instance);
        }

        // Initialize
        for (MethodHandle mh : initializers) {
            try {
                mh.invoke(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
        return pool;
    }

    public int reserve() {
        return size++;
    }
}
