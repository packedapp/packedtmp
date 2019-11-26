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
package packed.internal.container;

import java.util.ArrayDeque;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;

/**
 *
 */
final class ExtensionModelLoader {

    private static final WeakHashMap<Class<? extends Extension>, ExtensionModel<?>> MODELS = new WeakHashMap<>();

    private static final WeakHashMap<Class<? extends Extension>, Throwable> ERRORS = new WeakHashMap<>();

    private static final ReentrantLock lock = new ReentrantLock();

    @SuppressWarnings("unchecked")
    static <E extends Extension> ExtensionModel<E> load(Class<E> extensionType, Runtime runtime) {
        lock.lock();
        try {
            ExtensionModel<?> m = MODELS.get(extensionType);
            if (m != null) {
                return (ExtensionModel<E>) m;
            }
            Throwable t = ERRORS.get(extensionType);
            if (t != null) {
                throw new InternalExtensionException("Extension " + extensionType + " failed to be configured previously", t);
            }

            if (runtime == null) {
                runtime = new Runtime();
            }
            return load0(extensionType, runtime);
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Extension> ExtensionModel<E> load0(Class<E> extensionType, Runtime runtime) {
        if (runtime.stack.contains(extensionType)) {
            throw new RuntimeException("Cyclic error");
        }
        runtime.stack.push(extensionType);

        ExtensionModel<E> m;
        try {
            m = (ExtensionModel<E>) new ExtensionModel.Builder(extensionType, runtime).build();
        } catch (Throwable t) {
            ERRORS.put(extensionType, t);
            throw t;
        } finally {
            runtime.stack.pop();
        }
        MODELS.put(extensionType, m);
        return m;
    }

    // Laeser et trae ind af gangen
    // Checker det.
    // Opdatere det med alt hvad vi har laest ind.

    static class Runtime {
        final ArrayDeque<Class<? extends Extension>> stack = new ArrayDeque<>();

    }
}
