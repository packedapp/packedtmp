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
package packed.internal.lifetime;

import java.util.ArrayList;

import packed.internal.application.ApplicationLaunchContext;

/**
 *
 */
// Long term.. Could we rewrite all the indexes for images. In this way we could store all constants in another array that we would just reference
public final class LifetimePoolSetup {

    /** All constants that should be stored in the constant pool. */
    private final ArrayList<LifetimePoolWriteable> entries = new ArrayList<>();

    public final ArrayList<Runnable> postProcessing = new ArrayList<>();

    /** The size of the pool. */
    private int size;

    public void addConstant(LifetimePoolWriteable s) {
        entries.add(s);
    }

    public void addOrdered(LifetimePoolWriteable c) {
        // new Exception().printStackTrace();
        // We just keep both these 2 method that does the same for now
        entries.add(c);
    }

    public LifetimePool newPool(ApplicationLaunchContext launchContext) {
        LifetimePool pool = new LifetimePool(size);

        launchContext.writeToPool(pool);

        for (LifetimePoolWriteable e : entries) {
            e.writeToPool(pool);
        }
        return pool;
    }

    /**
     * Reserves room for a single object.
     * 
     * @return the index to store the object in at runtime
     */
    public int reserveObject() {
        return size++;
    }
}
