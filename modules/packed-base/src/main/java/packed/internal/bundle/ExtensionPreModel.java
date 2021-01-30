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
package packed.internal.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import packed.internal.util.ClassUtil;

/**
 *
 */
// Maaske flytte de statiske metoder til ExtensionModel.builder
public final class ExtensionPreModel   {

    private static final ClassValue<AtomicReference<ExtensionPreModel>> HOLDERS = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected AtomicReference<ExtensionPreModel> computeValue(Class<?> type) {
            return new AtomicReference<>(new ExtensionPreModel((Class<? extends Extension>) type));
        }
    };

    /** A set of extension this extension depends on. */
    private Set<Class<? extends Extension>> dependencies = new HashSet<>();

    private final WeakReference<Thread> thread = new WeakReference<>(Thread.currentThread());

    final Class<? extends Extension> type;

    ExtensionPreModel(Class<? extends Extension> type) {
        this.type = requireNonNull(type);
    }

    public static void addStaticDependency(Class<?> callerClass, Class<? extends Extension> dependencyType) {
        ExtensionPreModel m = m(callerClass);
        m.dependencies.add(dependencyType);
    }

    static Set<Class<? extends Extension>> consume(Class<? extends Extension> extensionType) {
        ClassUtil.initializeClass(extensionType);
                ExtensionPreModel existing = m(extensionType);
        HOLDERS.get(extensionType).set(null);
        if (existing != null) {
            return existing.dependencies;
        }
        return Set.of();
    }

    static ExtensionPreModel m(Class<?> callerClass) {
        if (!Extension.class.isAssignableFrom(callerClass)) {
            throw new InternalExtensionException("This method can only be called directly a class that extends Extension, was " + callerClass);
        }
        ExtensionPreModel m = HOLDERS.get(callerClass).get();
        if (m == null) {
            throw new InternalExtensionException("This method must be called within the extensions class initializer, extension = " + callerClass);
        }
        // Tror vi maa dropper den her traad...
        // Hvis class initializeren fejler et eller andet sted. 
        // Saa har vi stadig en reference til traaden...
        // Som minimum skal vi koere en weak reference
        if (Thread.currentThread() != m.thread.get()) {
            throw new InternalExtensionException("Expected thread " + m.thread + " but was " + Thread.currentThread());
        }
        return m;
    }
}
