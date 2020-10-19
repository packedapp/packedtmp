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
import java.lang.reflect.Field;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Factory;
import packed.internal.component.source.SourceModelField;
import packed.internal.sidecar.SidecarModel;

/**
 *
 */
public abstract class FieldSidecar {

    // Hver sidecar har sit eget context object...
    // Eneste maade at subclasses ikke kan faa fat it
    // Med mindre selvfoelgelig at vi laver den package private..
    // Men kan sagtens se vi faar sidecars udenfor denne pakke.

    /** The builder of this sidecar. Updated by {@link SidecarModel.Builder}. */
    @Nullable
    private SourceModelField.Builder builder;

    /**
     * Returns this sidecar's builder object.
     * 
     * @return this sidecar's builder object
     */
    private SourceModelField.Builder builder() {
        SourceModelField.Builder c = builder;
        if (c == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        }
        return c;
    }

    protected void configure() {}

    protected final void disableServiceInjection() {
        // syntes den er enabled by default
    }

    protected final <T> void attach(Class<T> key, T instance) {
        attach(Key.of(key), instance);
    }

    protected final <T> void attach(Key<T> key, T instance) {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void attach(Object instance) {
        requireNonNull(instance, "instance is null");
        attach((Class) instance.getClass(), instance);
    }

    /** Disables the sidecar for the particular method. No further processing will be done. */
    protected final void disable() {

    }

    protected final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field().getAnnotation(annotationClass);
    }

    protected final Optional<Key<?>> key() {
        return null;
    }

    protected final Field field() {
        return builder().field();
    }

    /**
     * Sets the field to the specified argument.
     * 
     * @param argument
     *            the argument to set
     * @throws UnsupportedOperationException
     *             if {@link ActivateFieldSidecar#allowSet()} is false
     * @throws ClassCastException
     *             if the specified argument is not assignable to the field
     */
    // Get if static field...
    protected final void set(Object argument) {
        builder().set(argument);
    }

    protected final void provideAsService(boolean isConstant) {
        builder().provideAsService(isConstant);
    }

    protected final void provideAsService(boolean isConstant, Class<?> key) {
        provideAsService(isConstant, Key.of(key));

    }

    protected final void provideAsService(boolean isConstant, Key<?> key) {
        builder().provideAsService(isConstant, key);
    }

    /**
     * 
     */
    protected final void checkWritable() {}

    Class<?> invoker;

    /**
     * 
     * @throws IllegalStateException
     *             if called from outside of {@link #configure()}
     */
    protected final void provideInvoker() {
        if (invoker != null) {
            throw new IllegalStateException("Cannot provide more than 1 " + Invoker.class.getSimpleName());
        }
        invoker = Object.class;
    }

    static Factory<?> findFactory(String name) {
        throw new UnsupportedOperationException();
    }
}
