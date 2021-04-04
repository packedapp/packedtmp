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

import java.util.ArrayList;

import packed.internal.application.ApplicationLaunchContext;
import packed.internal.component.ClassSourceSetup;
import packed.internal.component.PackedApplicationRuntime;
import packed.internal.inject.dependency.DependancyConsumer;

/**
 *
 */
// Long term.. Could we rewrite all the indexes for images. In this way we could store all constants in another array that we would just reference
public final class ConstantPoolSetup {

    /** All constants that should be stored in the constant pool. */
    private final ArrayList<ClassSourceSetup> constants = new ArrayList<>();

    private final ArrayList<DependancyConsumer> ordered = new ArrayList<>();

    /** The size of the pool. */
    private int size;

    public void addConstant(ClassSourceSetup s) {
        constants.add(s);
    }

    public void addOrdered(DependancyConsumer c) {
        ordered.add(c);
    }

    public ConstantPool newPool(ApplicationLaunchContext pic) {
        ConstantPool pool = new ConstantPool(size);

        // Not sure we want to create the guest here, we do it for now though
        if (pic.component.modifiers().hasRuntime()) {
            pool.store(0, new PackedApplicationRuntime(pic));
        }

        // We start by storing all constants in the pool
        // TODO it is likely that we
        for (ClassSourceSetup sa : constants) {
            sa.writeConstantPool(pool);
        }

        // All constants that must be instantiated and stored
        // Order here is very important. As for every constant.
        // Its dependencies are guaranteed to have been already stored
        for (DependancyConsumer injectable : ordered) {
            injectable.writeConstantPool(pool);
        }
        return pool;
    }

    public int reserve() {
        return size++;
    }
}
