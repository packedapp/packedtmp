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
package app.packed.sidecar;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarModel;

/**
 * Packed creates a single instance of a subclass and runs the {@link #configure()} method.
 */
public abstract class MethodSidecar extends Sidecar {

    // Hver sidecar har sit eget context object...
    // Eneste maade at subclasses ikke kan faa fat it
    // Med mindre selvfoelgelig at vi laver den package private..
    // Men kan sagtens se vi faar sidecars udenfor denne pakke.

    /** The builder of this sidecar. Updated by {@link SidecarModel.Builder}. */
    @Nullable
    private MethodSidecarModel.Builder builder;

    protected void bootstrap(BootstrapContext context) {}

    /**
     * Returns this sidecar's builder object.
     * 
     * @return this sidecar's builder object
     */
    private MethodSidecarModel.Builder builder() {
        MethodSidecarModel.Builder c = builder;
        if (c == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    protected void configure() {}

    protected final void disableServiceInjection() {
        // syntes den er enabled by default
    }

    /**
     * 
     * @throws IllegalStateException
     *             if called from outside of {@link #configure()}
     */
    protected final void provideInvoker() {
        builder().provideInvoker();
    }

    public interface BootstrapContext {

        default <T> void attach(Class<T> key, T instance) {
            attach(Key.of(key), instance);
        }

        default <T> void attach(Key<T> key, T instance) {}

        @SuppressWarnings({ "unchecked", "rawtypes" })
        default void attach(Object instance) {
            requireNonNull(instance, "instance is null");
            attach((Class) instance.getClass(), instance);
        }

        /** Disables the sidecar for the particular method. No further processing will be done. */
        void disable();

        default <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return method().getAnnotation(annotationClass);
        }

        default Optional<Key<?>> key() {
            return null;
        }

        Method method();

        default void provideAsService(boolean isConstant) {}

        default void provideAsService(boolean isConstant, Class<?> key) {}

        default void provideAsService(boolean isConstant, Key<?> key) {}
    }
}
