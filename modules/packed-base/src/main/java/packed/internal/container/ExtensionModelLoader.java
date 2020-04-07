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
 * An extension loader is responsible for initializing models for extensions.
 */
final class ExtensionModelLoader {

    private static final WeakHashMap<Class<? extends Extension>, Throwable> ERRORS = new WeakHashMap<>();

    private static final WeakHashMap<Class<? extends Extension>, ExtensionSidecarModel> EXTENSIONS = new WeakHashMap<>();

    /** A lock used for making sure that we only load one extension tree at a time. */
    private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

    private final ArrayDeque<Class<? extends Extension>> stack = new ArrayDeque<>();

    private ExtensionModelLoader() {}

    private ExtensionSidecarModel load1(Class<? extends Extension> extensionType) {
        if (stack.contains(extensionType)) {
            throw new RuntimeException("Cyclic error");
        }
        stack.push(extensionType);

        ExtensionSidecarModel m;
        ExtensionSidecarModel.Builder builder = new ExtensionSidecarModel.Builder(extensionType, this);
        try {
            m = builder.build();
        } catch (Throwable t) {
            ERRORS.put(extensionType, t);
            throw t;
        } finally {
            stack.pop();
        }

        // All dependencies have been successfully validated before we add the actual extension
        // and any of its pipelines to permanent storage
        EXTENSIONS.put(extensionType, m);

        return m;
    }

    static ExtensionSidecarModel load(Class<? extends Extension> extensionType) {
        return load0(extensionType, null);
    }

    static ExtensionSidecarModel load(Class<? extends Extension> extensionType, ExtensionModelLoader runtime) {
        return load0(extensionType, runtime);
    }

    static ExtensionSidecarModel load0(Class<? extends Extension> extensionType, ExtensionModelLoader loader) {
        GLOBAL_LOCK.lock();
        try {
            // First lets see if we have created the model before
            ExtensionSidecarModel m = EXTENSIONS.get(extensionType);
            if (m != null) {
                return m;
            }

            // Lets then see if we have tried to create the model before, but failed
            Throwable t = ERRORS.get(extensionType);
            if (t != null) {
                throw new InternalExtensionException("Extension " + extensionType + " failed to be configured previously", t);
            }

            // Create a new loader if we are not already part of one
            if (loader == null) {
                loader = new ExtensionModelLoader();
            }
            return loader.load1(extensionType);
        } finally {
            GLOBAL_LOCK.unlock();
        }
    }
}
