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
package packed.internal.component.wirelet;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.container.InternalExtensionException;

/**
 *
 */
// Maaske flytte de statiske metoder til ExtensionModel.builder
public final class WireletPreModel {

    private static final ClassValue<AtomicReference<WireletPreModel>> HOLDERS = new ClassValue<>() {

        @SuppressWarnings("unchecked")
        @Override
        protected AtomicReference<WireletPreModel> computeValue(Class<?> type) {
            return new AtomicReference<>(new WireletPreModel((Class<? extends Wirelet>) type));
        }
    };

    /** A set of extension this extension depends on. */
    @Nullable
    Class<? extends Wirelet> stackBy;

    private final WeakReference<Thread> thread = new WeakReference<>(Thread.currentThread());

    final Class<? extends Wirelet> type;

    boolean buildtimeOnly;

    WireletPreModel(Class<? extends Wirelet> type) {
        this.type = requireNonNull(type);
    }

    public static void buildtimeOnly(Class<?> callerClass) {
        WireletPreModel m = m(callerClass);
        m.buildtimeOnly = true;
    }

    public static void stackBy(Class<?> callerClass, Class<? extends Wirelet> wireletType) {
        WireletPreModel m = m(callerClass);
        if (m.stackBy != null) {
            throw new ExceptionInInitializerError("This method can only be invoked once for a single wirelet");
        }
        m.stackBy = requireNonNull(wireletType, "wireletType is null");
    }

    @Nullable
    static WireletPreModel consume(Class<? extends Wirelet> extensionType) {
        try {
            MethodHandles.lookup().ensureInitialized(extensionType);
        } catch (IllegalAccessException e) {
            throw new InternalExtensionException("Oops");
        }
        WireletPreModel existing = m(extensionType);
        HOLDERS.get(extensionType).set(null);
        return existing;
    }

    private static WireletPreModel m(Class<?> callerClass) {
        if (!Wirelet.class.isAssignableFrom(callerClass)) {
            throw new ExceptionInInitializerError("This method can only be called directly from an wirelet, was " + callerClass);
        }
        WireletPreModel m = HOLDERS.get(callerClass).get();
        if (m == null) {
            throw new ExceptionInInitializerError("This method must be called within the wirelet's class initializer, wirelet = " + callerClass);
        }
        if (Thread.currentThread() != m.thread.get()) {
            throw new ExceptionInInitializerError("Expected thread " + m.thread + " but was " + Thread.currentThread());
        }
        return m;
    }
}
