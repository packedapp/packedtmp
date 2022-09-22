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
package app.packed.bean;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.InternalExtensionException;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionModel;

/**
 * @see Extension#newBeanIntrospector
 * @see BeanHandle.Installer#introspectWith(BeanIntrospector)
 */

//BeanAnalyzer, BeanVisitor, BeanInspector, BeanIntrospector, BeanScanner
// BeanHookProcessor?

// Move BeanMethod and friends into BeanScanner as nested classes???

// Syntes godt vi kan smide den paa BeanExtensionPoint
public abstract class BeanIntrospector {

    /**
     * The configuration of this processor. Is initially null but populated via
     * {@link #initialize(ExtensionModel, BeanSetup)}.
     */
    @Nullable
    private Setup setup;

    /** {@return an annotation reader for for the bean.} */
    public final BeanIntrospector$AnnotationReader beanAnnotations() {
        // AnnotationReader.of(beanClass());
        throw new UnsupportedOperationException();
    }

    public final Class<?> beanClass() {
        return setup().bean.beanClass();
    }

    /**
     * @param postFix
     *            the message to include in the final message
     * 
     * @throws BeanDefinitionException
     *             always thrown
     */
    public final void failWith(String postFix) {
        throw new BeanDefinitionException("OOPS " + postFix);
    }

    /**
     * Invoked by a MethodHandle from ExtensionSetup.
     * 
     * @param extension
     *            the extension that owns the scanner
     * @param bean
     *            the bean we are scanning
     * @throws IllegalStateException
     *             if called more than once
     */
    final void initialize(ExtensionModel extension, BeanSetup bean) {
        if (this.setup != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.setup = new Setup(extension, bean);
    }

    public void onClassHook(BeanIntrospector$OnClassHook clazz) {}

    public void onBindingHook(BeanIntrospector$OnBindingHook dependency) {}

    /**
     * A callback method that is called for fields that are annotated with a field hook annotation defined by the extension:
     * 
     * is annotated with an annotation that itself is annotated with {@link BeanIntrospector$OnFieldHook.FieldHook} and
     * where {@link FieldHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a given field and extension. Even if there are multiple matching hook
     * annotations on the same field.
     * 
     * @param field
     *            the bean field
     * @see BeanExtensionPoint.FieldHook
     */
    // onFieldHook(Set<Class<? extends Annotation<>> hooks, BeanField));
    public void onFieldHook(BeanIntrospector$OnFieldHook field) {
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle field annotation(s) " + field.hooks());
    }

    public void onMethodHook(BeanIntrospector$OnMethodHook method) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle method annotation(s) " + method.hooks());
    }

    /**
     * A callback method that is called after any other methods on this class.
     * <p>
     * This method can be used to provide final validation or registration of the bean.
     * <p>
     * If an exception is thrown at any time doing processing of the bean this method will not be called.
     * 
     * @see #onPreIntrospect()
     */
    public void onPostIntrospect() {}

    /**
     * A callback method that is invoked before any calls to {@link #onClass(BeanIntrospector$OnClassHook)},
     * {@link #onFieldHook(BeanIntrospector$OnFieldHook)}, {@link #onMethod(BeanIntrospector$OnMethodHook)} or
     * {@link #onDependency(BeanIntrospector$OnBindingHook)}.
     * <p>
     * This method can be used to setup data structures or perform validation.
     * 
     * @see #onPostIntrospect()
     */
    public void onPreIntrospect() {}

    public final Class<? extends Extension<?>> registrant() {
        // Ideen er at vi kan checke at vi selv er registranten...
        throw new UnsupportedOperationException();
    }

    /**
     * {@return the internal configuration class.}
     * 
     * @throws IllegalStateException
     *             if called from the constructor of the class
     */
    private Setup setup() {
        Setup s = setup;
        if (s == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of " + getClass());
        }
        return s;
    }

    // This is a place holder for now... Will be ditched it in the future
    // BeanVariable bare
    public sealed interface BeanElement permits BeanIntrospector$OnClassHook, BeanIntrospector$OnConstructorHook, BeanIntrospector$OnFieldHook, BeanIntrospector$OnMethodHook, BeanIntrospector$OnBindingHook {

        /**
         * @param postFix
         *            the message to include in the final message
         * 
         * @throws BeanDefinitionException
         *             always thrown
         */
        default void failWith(String postFix) {
            throw new BeanDefinitionException("OOPS " + postFix);
        }
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface ClassHook {

        /** Whether or not the sidecar is allow to get the contents of a field. */
        boolean allowAllAccess() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface FieldHook {

        /** Whether or not the owning extension is allow to get the contents of the field. */
        boolean allowGet() default false;

        /** Whether or not the owning extension is allow to set the contents of the field. */
        boolean allowSet() default false;

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    /**
     *
     * @see BeanIntrospector#onMethodHook(BeanIntrospector$OnMethodHook)
     */
    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface MethodHook {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as {@link BeanIntrospector$OnMethodHook#operationBuilder(ExtensionBeanConfiguration)} and... will fail
         * with {@link UnsupportedOperationException} unless the value of this attribute is {@code true}.
         * 
         * @return whether or not the implementation is allowed to invoke the target method
         * 
         * @see BeanIntrospector$OnMethodHook#operationBuilder(ExtensionBeanConfiguration)
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        boolean allowInvoke() default false; // allowIntercept...

        /** The extension the hook is a part of. */
        Class<? extends Extension<?>> extension();
    }

    /**
     *
     * @see BeanIntrospector.OnBindingHook
     * @see BeanIntrospector#onBindingHook(BeanIntrospector$OnBindingHook)
     */
    @Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE })
    @Retention(RUNTIME)
    @Documented
    public @interface BindingHook {

        /** The extension this hook is a part of. Must be located in the same module as the annotated element. */
        Class<? extends Extension<?>> extension();
    }
    // CheckRealmIsApplication
    // CheckRealmIsExtension

    /** A small utility record to hold the both the extension model and the bean in one field. */
    private record Setup(ExtensionModel extension, BeanSetup bean) {}
}