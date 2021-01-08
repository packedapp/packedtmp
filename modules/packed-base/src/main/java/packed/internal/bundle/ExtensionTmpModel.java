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
public final class ExtensionTmpModel   {

    private static final ClassValue<AtomicReference<ExtensionTmpModel>> HOLDERS = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected AtomicReference<ExtensionTmpModel> computeValue(Class<?> type) {
            return new AtomicReference<>(new ExtensionTmpModel((Class<? extends Extension>) type));
        }
    };

    /** A set of extension this extension depends on. */
    private Set<Class<? extends Extension>> dependencies = new HashSet<>();

    private final Thread thread = Thread.currentThread();

    final Class<? extends Extension> type;

    ExtensionTmpModel(Class<? extends Extension> type) {
        this.type = requireNonNull(type);
    }


    public static void addStaticDependency(Class<?> callerClass, Class<? extends Extension> dependencyType) {
        ExtensionTmpModel m = m(callerClass);
        m.dependencies.add(dependencyType);
    }

    static Set<Class<? extends Extension>> consume(Class<? extends Extension> extensionType) {
        ClassUtil.initializeClass(extensionType);
                ExtensionTmpModel existing = m(extensionType);
        HOLDERS.get(extensionType).set(null);
        if (existing != null) {
            return existing.dependencies;
        }
        return Set.of();
    }

    static ExtensionTmpModel m(Class<?> callerClass) {
        if (!Extension.class.isAssignableFrom(callerClass)) {
            throw new InternalExtensionException("This method can only be called directly from an extension, was " + callerClass);
        }
        ExtensionTmpModel m = HOLDERS.get(callerClass).get();
        if (m == null) {
            throw new InternalExtensionException("This method must be called within the extensions class initializer, extension = " + callerClass);
        }
        if (Thread.currentThread() != m.thread) {
            throw new InternalExtensionException("Expected thread " + m.thread + " but was " + Thread.currentThread());
        }
        return m;
    }
}
