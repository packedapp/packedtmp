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
import app.packed.bean.BeanField.FieldHook;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.InternalExtensionException;
import packed.internal.bean.BeanSetup;
import packed.internal.container.ExtensionSetup;

/**
 *
 *
 * @see Extension#newBeanScanner
 * @see BeanHandler.Builder#beanScanner(BeanScanner)
 */
// Move BeanMethod and friends into BeanScanner as nested classes???
public abstract class BeanScanner {

    /**
     * The configuration of this scanner. Is initially null but populated via
     * {@link #initialize(ExtensionSetup, BeanSetup)}.
     */
    @Nullable
    private Setup setup;

    // BeanAnnotationReader???

    /** {@return an annotation reader for for the bean.} */
    public final BeanAnnotationReader beanAnnotationReader() {
        // AnnotationReader.of(beanClass());
        throw new UnsupportedOperationException();
    }

    public final Class<?> beanClass() {
        return setup().bean.beanClass();
    }

    /** {@return the kind of bean being scanned.} */
    public final BeanKind beanKind() {
        return setup().bean.beanKind();
    }

    /**
     * Invoked by a MethodHandle from ExtensionSetup.
     * 
     * @param extension
     *            the extension that owns the scanner
     * @param bean
     *            the bean we are scanning
     */
    final void initialize(ExtensionSetup extension, BeanSetup bean) {
        if (this.setup != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.setup = new Setup(extension, bean);
    }

    public void onClass(BeanClass clazz) {}

    public void onDependency(BeanDependency dependency) {}

    /**
     * A callback method that is called for fields that are annotated with a field hook annotation defined by the extension:
     * 
     * is annotated with an annotation that itself is annotated with {@link BeanField.FieldHook} and where
     * {@link FieldHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a given field and extension. Even if there are multiple matching hook
     * annotations on the same field.
     * 
     * @param field
     *            the bean field
     * @see BeanField.FieldHook
     */
    // onAnnotatedField(Set<Class<? extends Annotation<>> hooks, BeanField));
    // IDK
    // Det kommer lidt an paa variable vil jeg mene...
    public void onField(BeanField field) {}

    public void onMethod(BeanMethod method) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        // We probably want to throw an internal extension exception instead
        throw new InternalExtensionException(setup().extension.model.fullName() + " failed to handle bean method");
    }

    /**
     * A callback method that is called before any other methods on the scanner.
     * <p>
     * This method can be used to setup data structures or perform validation.
     * 
     * @see #onScanEnd()
     */
    public void onScanBegin() {}

    /**
     * A callback method that is called after any other methods on the scanner.
     * <p>
     * This method can be used to provide final validation or registration of the bean.
     * <p>
     * If an exception is thrown at any time doing processing of the bean this method will not be called.
     * 
     * @see #onScanBegin()
     */
    public void onScanEnd() {}

    /**
     * {@return all the extensions that are being mirrored.}
     * 
     * @throws InternalExtensionException
     *             if called from the constructor of the mirror
     */
    private final Setup setup() {
        Setup b = setup;
        if (b == null) {
            throw new InternalExtensionException("This method cannot be called from the constructor of this class.");
        }
        return b;
    }

    // This is a place holder for now... Will be ditched it in the future
    // BeanVariable bare
    public sealed interface BeanElement permits BeanClass, BeanConstructor, BeanField, BeanMethod, BeanDependency {

        default BeanAnnotationReader annotations() {
            throw new UnsupportedOperationException();
        }

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
    public @interface MethodHook {

        /**
         * Whether or not the implementation is allowed to invoke the target method. The default value is {@code false}.
         * <p>
         * Methods such as {@link BeanMethod#operationBuilder(ExtensionBeanConfiguration)} and... will fail with
         * {@link UnsupportedOperationException} unless the value of this attribute is {@code true}.
         * 
         * @return whether or not the implementation is allowed to invoke the target method
         * 
         * @see BeanMethod#operationBuilder(ExtensionBeanConfiguration)
         */
        // maybe just invokable = true, idk og saa Field.gettable and settable
        boolean allowInvoke() default false; // allowIntercept...

        /** The hook's {@link BeanField} class. */
        Class<? extends Extension<?>> extension();
    }
    // CheckRealmIsApplication
    // CheckRealmIsExtension

    /** A small utility record to hold the both the extension and the bean in one field. */
    private record Setup(ExtensionSetup extension, BeanSetup bean) {}
}
