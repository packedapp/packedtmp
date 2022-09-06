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

import app.packed.base.Nullable;
import app.packed.bean.BeanExtensionPoint.FieldHook;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionModel;

/**
 * @see Extension#newBeanScanner
 * @see BeanCustomizer.Builder#beanScanner(BeanScanner)
 */

//BeanAnalyzer, BeanVisitor, BeanInspector, BeanIntrospector, BeanScanner
// BeanHookProcessor?

// Move BeanMethod and friends into BeanScanner as nested classes???
public abstract class BeanProcessor {

    /**
     * The configuration of this processor. Is initially null but populated via
     * {@link #initialize(ExtensionModel, BeanSetup)}.
     */
    @Nullable
    private Setup setup;

    /** {@return an annotation reader for for the bean.} */
    public final BeanProcessor$AnnotationReader beanAnnotations() {
        // AnnotationReader.of(beanClass());
        throw new UnsupportedOperationException();
    }
    
    /**
     * @param postFix
     *            the message to include in the final message
     * 
     * @throws BeanDefinitionException
     *             always thrown
     */
    public void failWith(String postFix) {
        throw new BeanDefinitionException("OOPS " + postFix);
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
     * @throws IllegalStateException
     *             if called more than once
     */
    final void initialize(ExtensionModel extension, BeanSetup bean) {
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
     * @see BeanExtensionPoint.FieldHook
     */
    // onAnnotatedField(Set<Class<? extends Annotation<>> hooks, BeanField));
    // IDK Det kommer lidt an paa variable vil jeg mene...
    public void onField(BeanField field) {}

    public void onMethod(BeanMethod method) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        // We probably want to throw an internal extension exception instead
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle bean method");
    }

    /**
     * A callback method that is called before any other methods on this class.
     * <p>
     * This method can be used to setup data structures or perform validation.
     * 
     * @see #onProcessingStop()
     */
    public void onProcessingStart() {}

    /**
     * A callback method that is called after any other methods on this class.
     * <p>
     * This method can be used to provide final validation or registration of the bean.
     * <p>
     * If an exception is thrown at any time doing processing of the bean this method will not be called.
     * 
     * @see #onProcessingStart()
     */
    public void onProcessingStop() {}

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
    public sealed interface BeanElement permits BeanClass, BeanConstructor, BeanField, BeanMethod, BeanDependency {

        default BeanProcessor$AnnotationReader annotations() {
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

    // CheckRealmIsApplication
    // CheckRealmIsExtension

    /** A small utility record to hold the both the extension model and the bean in one field. */
    private record Setup(ExtensionModel extension, BeanSetup bean) {}
}
