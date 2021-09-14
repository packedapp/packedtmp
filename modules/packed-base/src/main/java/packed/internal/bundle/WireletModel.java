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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

import app.packed.base.Nullable;
import app.packed.bundle.Wirelet;
import app.packed.extension.InternalExtensionException;
import packed.internal.util.ClassUtil;

/** A model of a {@link Wirelet}. */
public final class WireletModel {

    /** A cache of models for {@link Wirelet} subclasses. */
    private static final ClassValue<WireletModel> MODELS = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected WireletModel computeValue(Class<?> type) {
            Builder b = Builder.consume(ClassUtil.checkProperSubclass(Wirelet.class, type));
            return new WireletModel(b);
        }
    };

    final boolean buildtimeOnly;

    /**
     * Create a new wirelet model.
     * 
     * @param clazz
     *            the wirelet type
     */
    private WireletModel(Builder b) {
        this.buildtimeOnly = b.buildtimeOnly;
    }

    public static Builder bootstrap(Class<?> callerClass) {
        return Builder.m(callerClass);
    }
    
    /**
     * Returns a model for the specified wirelet type.
     * 
     * @param wireletType
     *            the wirelet type to return a model for.
     * @return the model
     */
    public static WireletModel of(Class<? extends Wirelet> wireletType) {
        return MODELS.get(wireletType);
    }

    /** A builder for {@link WireletModel}. */
    public static final class Builder {

        private static final ClassValue<AtomicReference<Builder>> HOLDERS = new ClassValue<>() {

            @SuppressWarnings("unchecked")
            @Override
            protected AtomicReference<Builder> computeValue(Class<?> type) {
                return new AtomicReference<>(new Builder((Class<? extends Wirelet>) type));
            }
        };

        boolean buildtimeOnly;

        private final WeakReference<Thread> thread = new WeakReference<>(Thread.currentThread());

        final Class<? extends Wirelet> type;

        Builder(Class<? extends Wirelet> type) {
            this.type = requireNonNull(type);
        }

        public void buildtimeOnly() {
            buildtimeOnly = true;
        }

        @Nullable
        static Builder consume(Class<? extends Wirelet> extensionType) {
            Lookup l = MethodHandles.lookup();
            l.lookupClass().getModule().addReads(extensionType.getModule());
            try {
                l = MethodHandles.privateLookupIn(extensionType, l);
                l.ensureInitialized(extensionType);
            } catch (IllegalAccessException e) {
                throw new InternalExtensionException("Oops " + extensionType);
            }
            Builder existing = m(extensionType);
            HOLDERS.get(extensionType).set(null);
            return existing;
        }

        private static Builder m(Class<?> callerClass) {
            if (!Wirelet.class.isAssignableFrom(callerClass)) {
                throw new ExceptionInInitializerError("This method can only be called directly from an wirelet, was " + callerClass);
            }
            Builder m = HOLDERS.get(callerClass).get();
            if (m == null) {
                throw new ExceptionInInitializerError("This method must be called within the wirelet's class initializer, wirelet = " + callerClass);
            }
            if (Thread.currentThread() != m.thread.get()) {
                throw new ExceptionInInitializerError("Expected thread " + m.thread + " but was " + Thread.currentThread());
            }
            return m;
        }
    }
}
