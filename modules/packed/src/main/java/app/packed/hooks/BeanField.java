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
package app.packed.hooks;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.hooks.accessors.MethodAccessor;
import app.packed.inject.Factory;
import packed.internal.hooks.usesite.UseSiteFieldHookModel;

public abstract class BeanField {

    /**
     *
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface Hook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowGet() default false;

        /** Whether or not the sidecar is allow to set the contents of a field. */
        boolean allowSet() default false;

        // Maybe it should be mandatory... We don't currently support method hooks that
        // maybe annotation() instead. Sounds better if we, for example, adds
        // nameStartsWith()
        Class<? extends Annotation>[] annotation() default {};

        /** The hook's {@link BeanField} class. */
        Class<? extends BeanField> bootstrap();
    }

    /** The builder used by this bootstrap. Updated by {@link UseSiteFieldHookModel}. */
    private UseSiteFieldHookModel.@Nullable Builder builder;

    Class<?> invoker;

    protected final <T> void attach(Class<T> key, T instance) {
        attach(Key.of(key), instance);
    }

    protected final <T> void attach(Key<T> key, T instance) {}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final void attach(Object instance) {
        requireNonNull(instance, "instance is null");
        attach((Class) instance.getClass(), instance);
    }

    // Taenker vi har lov til at smide reflection exception???
    protected void bootstrap() {}

    /**
     * Returns this sidecar's builder object.
     * 
     * @return this sidecar's builder object
     */
    private UseSiteFieldHookModel.Builder builder() {
        UseSiteFieldHookModel.Builder b = builder;
        if (b == null) {
            throw new IllegalStateException("This method cannot called outside of the #configure() method. Maybe you tried to call #configure() directly");
        }
        return b;
    }

    /**
     * Check not final??
     */
    protected final void checkWritable() {}

    /** Disables any further processing of the field. */
    public final void disable() {
        builder().disable();
    }

    protected final void disableServiceInjection() {
        // syntes den er enabled by default
    }

    public final Field field() {
        return builder().field();
    }

    /**
     * Returns an annotated element from the method that is being bootstrapped.
     * 
     * @see AnnotatedElement#getAnnotation(Class)
     */
    // MS extends AnnotatedElement???? With meta annotations.
    // Call method if you want without them...
    public final <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return builder().field().getAnnotation(annotationClass);
    }

    public final Annotation[] getAnnotations() {
        return builder().field().getAnnotations();
    }

    public final <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
        return builder().field().getAnnotationsByType(annotationClass);
    }

    /**
     * Returns true if an annotation for the specified type is <em>present</em> on the hooked field, else false.
     * 
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return true if an annotation for the specified annotation type is present on the hooked field, else false
     * 
     * @see Field#isAnnotationPresent(Class)
     */
    public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return builder().field().isAnnotationPresent(annotationClass);
    }

    protected final Optional<Key<?>> key() {
        // Maaske extract via Key.of(Field)
        return null;
    }

    /**
     * @param <T>
     *            the type of extension class bootstrap to return
     * @param type
     *            the type of extension class bootstrap to return
     * @return a extension class bootstrap of the specified type
     * @throws IllegalStateException
     *             if this method has previously been called on this instance
     * 
     */
    protected final <T extends BeanClass> T manageBy(Class<T> type) {
        return builder().manageBy(type);
    }

    /**
     * @return a method handle getter
     */
    public final MethodHandle methodHandleGetter() {
        throw new UnsupportedOperationException();
    }

    public final MethodHandle methodHandleSetter() {
        throw new UnsupportedOperationException();
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
     * @throws IllegalStateException
     *             if called from outside of {@link #bootstrap()}
     */
    protected final void provideInvoker() {
        if (invoker != null) {
            throw new IllegalStateException("Cannot provide more than 1 " + MethodAccessor.class.getSimpleName());
        }
        invoker = Object.class;
    }

    /**
     * Sets the field to the specified argument.
     * 
     * @param argument
     *            the argument to set
     * @throws UnsupportedOperationException
     *             if {@link Hook#allowSet()} is false
     * @throws ClassCastException
     *             if the specified argument is not assignable to the field
     */
    // Get if static field...
    protected final void set(Object argument) {
        builder().set(argument);
    }

    // Must have both get and set
    // methodHandleGet
    /**
     * @return the variable
     * @see Lookup#unreflectVarHandle(Field)
     * @see Hook#allowGet()
     * @see Hook#allowSet()
     * @throws UnsupportedOperationException
     *             if the extension field has not both get and set access
     */
    public final VarHandle varHandle() {
        throw new UnsupportedOperationException();
    }

    static Factory<?> findFactory(String name) {
        throw new UnsupportedOperationException();
    }
}