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
import java.util.stream.Collectors;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;

/**
 * An extension loader is responsible for initializing models for extensions.
 */
final class ExtensionModelLoader {

    // Maaske skal vi baade have id, og depth... Eller er depth ligegyldigt???
    // final static Map<String, String> baseExtensions = Map.of();

    private static final WeakHashMap<Class<? extends Extension>, Throwable> ERRORS = new WeakHashMap<>();

    private static final WeakHashMap<Class<? extends Extension>, ExtensionModel> EXTENSIONS = new WeakHashMap<>();

    /** A lock used for making sure that we only load one extension tree at a time. */
    private static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();

    private static int nextExtensionId;

    private final ArrayDeque<Class<? extends Extension>> stack = new ArrayDeque<>();

    private ExtensionModelLoader() {}

    private ExtensionModel load1(Class<? extends Extension> extensionType) {
        // Den eneste grund til at vi gennem en exception er pga
        if (stack.contains(extensionType)) {
            String st = stack.stream().map(e -> e.getCanonicalName()).collect(Collectors.joining(" -> "));
            throw new InternalExtensionException("Cyclic dependencies between extensions encountered: " + st + " -> " + extensionType.getCanonicalName());
        }
        stack.push(extensionType);

        ExtensionModel m;
        try {
            ExtensionModel.Builder builder = new ExtensionModel.Builder(extensionType, this, nextExtensionId++);

            // TODO move this to the builder when it has loaded all its dependencies...
            // And maybe make nextExtension it local to Loader and only update the static one
            // when every extension has been successfully loaded...

            // ALSO nextExtensionID should probably be reset...
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

    static ExtensionModel load(Class<? extends Extension> extensionType) {
        return load0(extensionType, null);
    }

    static ExtensionModel load(ExtensionModel.Builder builder, Class<? extends Extension> dependency, ExtensionModelLoader loader) {
        return load0(dependency, loader);
    }

    // taenker vi godt kan flytte global lock en class valuen...

    private static ExtensionModel load0(Class<? extends Extension> extensionType, @Nullable ExtensionModelLoader loader) {
        GLOBAL_LOCK.lock();
        try {
            // First lets see if we have created the model before
            ExtensionModel m = EXTENSIONS.get(extensionType);
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
