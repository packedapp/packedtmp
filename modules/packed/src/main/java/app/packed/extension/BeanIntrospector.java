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
package app.packed.extension;

import java.lang.annotation.Annotation;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocalAccessor;
import app.packed.bean.BeanSourceKind;
import app.packed.container.Operative;
import app.packed.extension.BeanElement.BeanClass;
import app.packed.extension.BeanElement.BeanField;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.AnnotationList;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanScannerExtensionRef;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.util.PackedAnnotationList;
import internal.app.packed.util.StringFormatter;

/**
 * Bean introspectors are the primary way for extensions to interacts the
 *
 * This class contains a number of overridable callback methods, all of them starting with {@code on}. Make list
 *
 * @see Extension#newBeanIntrospector()
 */

// Operations24C3
/// OnX
//// Man kan lave ny operationer
//// Operationen er configurable indtil onX returnere, man kalder customBinding(int index), eller
//// Kalder OH.resolveParameters
//// - Bindings kan ikke overskrives
public non-sealed abstract class BeanIntrospector implements BeanLocalAccessor {

    /**
     * The configuration of this introspector. Is initially null but populated via
     * {@link #initialize(ExtensionDescriptor, BeanSetup)}.
     */
    @Nullable
    private BeanScannerExtensionRef setup;

    /**
     * A callback method that is invoked before any calls to any of the {@code hookOn} methods on this class.
     * <p>
     * This method can be used to setup data structures or perform validation.
     *
     * @see #beforeHooks()
     */
    public void afterHooks() {}

    BeanSetup bean() {
        return setup().scanner.bean;
    }

    /** {@return an annotation reader for the bean class.} */
    public final AnnotationList beanAnnotations() {
        return new PackedAnnotationList(beanClass().getAnnotations());
    }

    /** {@return the owner of the bean.} */
    public final Operative beanAuthor() {
        return bean().author();
    }

    /** {@return the bean class that is being introspected.} */
    public final Class<?> beanClass() {
        return bean().beanClass;
    }

    /** {@return the extension the bean was installed via.} */
    public final Class<? extends Extension<?>> beanInstalledVia() {
        return bean().installedBy.extensionType;
    }

    /** {@return an annotation reader for the bean class.} */
    public final BeanKind beanKind() {
        return bean().beanKind;
    }

    /** {@return the extension the bean was installed via.} */
    public final LifetimeKind beanLifetimeKind() {
        return bean().lifetime.lifetimeKind();
    }

    /** {@return the bean source kind.} */
    public final BeanSourceKind beanSourceKind() {
        return bean().beanSourceKind;
    }

    /**
     * A callback method that is called after any other callback methods on this class.
     * <p>
     * This method can be used to provide final validation or registration of the bean.
     * <p>
     * If an exception is thrown at any time doing processing of the bean this method will not be called.
     *
     * @see #afterHooks()
     */
    public void beforeHooks() {}

//    public boolean hasAttachment(Class<?> attachmentType) {
//        requireNonNull(attachmentType);
//        Map<Class<?>, Object> a = setup().bean.attachments;
//        return a != null && a.containsKey(attachmentType);
//    }

    private ExtensionDescriptor extension() {
        return setup().extension.model;
    }

    /**
     * @param postFix
     *            the message to include in the final message
     *
     * @throws BeanInstallationException
     *             always thrown
     */
    public final void failWith(String postFix) {
        throw new BeanInstallationException("OOPS " + postFix);
    }

    public void hookOnAnnotatedClass(Annotation annotated, BeanClass clazz) {}

    // Replace set with something like AnnotatedHookSet
    /**
     *
     * The default implementation calls {@link #hookOnAnnotatedClass(Annotation, OperationalClass)}
     *
     * @param hooks
     *            the annotation(s) that hook
     * @param an
     */
    public void hookOnAnnotatedClass(AnnotationList annotations, BeanClass clazz) {
        for (Annotation a : annotations) {
            hookOnAnnotatedClass(a, clazz);
        }
    }

    /**
     * This method is called by the similar named method {@link #hookOnAnnotatedField(AnnotationList, BeanField)} for every
     * annotation.
     *
     * @param annotation
     *            the annotation
     * @param field
     *            an operational field
     */
    public void hookOnAnnotatedField(Annotation annotation, BeanField field) {
        throw new BeanInstallationException(extension().fullName() + " does not know how to handle " + annotation.annotationType() + " on " + field);
    }

    /**
     * A callback method that is called by the framework when a bean is being installed for fields that are annotated with
     * one or more field hook annotation belonging to the extension:
     * <p>
     * The default implementation simply calls {@link #hookOnAnnotatedField(Annotation, BeanField)} for each separately
     * applied annotation. And in most
     * <p>
     * This method should be overridden only if there are annotations that can be applied multiple times to the same field.
     * Or i
     *
     * annotation. And
     *
     * is annotated with an annotation that itself is annotated with {@link AnnotatedFieldHook} and where
     * {@link AnnotatedFieldHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a given field and extension. Even if there are multiple matching hook
     * annotations on the same field.
     *
     * @param hooks
     *            a non-empty collection of hooks
     * @param field
     *            an operational field
     * @see AnnotatedFieldHook
     */
    // Combinations of Field Annotations, Variable Annotations & VariableType
    // Failures? or order of importancez
    public void hookOnAnnotatedField(AnnotationList hooks, BeanField field) {
        for (Annotation a : hooks) {
            hookOnAnnotatedField(a, field);
        }
    }

    // can we attach information to the method???
    // fx @Lock(sdfsdfsdf) uden @Query???
    public void hookOnAnnotatedMethod(Annotation hook, BeanMethod method) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        throw new InternalExtensionException(extension().fullName() + " failed to handle method annotation(s) " + hook);
    }

    /**
     * @param on
     *
     * @see AnnotatedMethodHook
     */
    public void hookOnAnnotatedMethod(AnnotationList hooks, BeanMethod method) {
        for (Annotation a : hooks) {
            hookOnAnnotatedMethod(a, method);
        }
    }

    /**
     * <p>
     * A variable can never be annotated with more than 1 operational
     *
     * @param variable
     *            a binding
     *
     * @see AnnotatedBindingHook
     */
    public void hookOnAnnotatedVariable(Annotation hook, BindableVariable binder) {
        throw new BeanInstallationException(extension().fullName() + " failed to handle parameter hook annotation(s) " + hook);
    }

    /**
     * @param v
     *
     * @see BindingTypeHook
     */
    public void hookOnVariableType(Class<?> hook, UnwrappedBindableVariable binder) {
        throw new BeanInstallationException(extension().fullName() + " cannot handle type hook " + StringFormatter.format(hook));
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
    final void initialize(BeanScannerExtensionRef ce) {
        if (this.setup != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.setup = ce;
    }

    /** {@return whether or not the bean is in same lifetime as the application.} */
    public final boolean isInApplicationLifetime() {
        BeanSetup b = bean();
        return b.lifetime == b.container.application.container.lifetime;
    }

    /** {@return whether or not the bean is in same lifetime as its container.} */
    public final boolean isInContainerLifetime() {
        return bean().lifetime instanceof ContainerLifetimeSetup;
    }

    /** {@return whether or not this introspector is the installing introspector.} */
    public final boolean isInstallingIntrospector() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return the internal configuration class.}
     *
     * @throws IllegalStateException
     *             if called from the constructor of the class
     */
    private BeanScannerExtensionRef setup() {
        BeanScannerExtensionRef s = setup;
        if (s == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of " + getClass());
        }
        return s;
    }
}

interface ZandboxBI {

    // IDK vi bliver stadig noedt til at analysere den...
    // Factory hint instead? Og allow en Method... saa det ikke kun er
    // constructors
    @interface ConstructorHint {
        // Ideen er lidt at man bruger den for extension beans

        Class<?>[] value();
    }
}

// Proevede at lave en lidt slags eventdriver hook thingy med
// pattern switches
// Men det fungere ikke super godt. Isaer fordi vi ikke kan
// switch on class literals
//public void onHook(HookElement element) {
//
//}
//
