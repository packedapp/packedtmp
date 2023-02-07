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

import java.lang.annotation.Annotation;

import app.packed.bean.BeanElement.BeanClass;
import app.packed.bean.BeanElement.BeanField;
import app.packed.bean.BeanElement.BeanMethod;
import app.packed.bindings.BeanVariable;
import app.packed.bindings.BeanWrappedVariable;
import app.packed.container.Realm;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.AnnotationList;
import app.packed.framework.Nullable;
import internal.app.packed.bean.BeanScannerExtension;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedAnnotationList;
import internal.app.packed.util.StringFormatter;

/**
 *
 * This class contains a number of overridable callback methods, all of them starting with {@code on}. Make list
 *
 * @see Extension#newBeanIntrospector
 */

// Operations
/// OnX
//// Man kan lave ny operationer
//// Operationen er configurable indtil onX returnere, man kalder customBinding(int index), eller
//// Kalder OH.resolveParameters
//// - Bindings kan ikke overskrives

public abstract class BeanIntrospector {

    /**
     * The configuration of this introspector. Is initially null but populated via
     * {@link #initialize(ExtensionDescriptor, BeanSetup)}.
     */
    @Nullable
    private BeanScannerExtension setup;

    /**
     * A callback method that is invoked before any calls to any of the {@code hookOn} methods on this class.
     * <p>
     * This method can be used to setup data structures or perform validation.
     *
     * @see #beforeHooks()
     */
    public void afterHooks() {}

//    @SuppressWarnings("unchecked")
//    public <A> Optional<A> attachment(Class<A> attachmentType) {
//        requireNonNull(attachmentType);
//        Map<Class<?>, Object> a = setup().bean.attachments;
//        return a == null ? Optional.empty() : Optional.ofNullable((A) a.get(attachmentType));
//    }

    private BeanSetup bean() {
        return setup().scanner.bean;
    }

    /** {@return an annotation reader for the bean class.} */
    public final AnnotationList beanAnnotations() {
        return new PackedAnnotationList(beanClass().getAnnotations());
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

    /** {@return the owner of the bean.} */
    public final Realm beanOwner() {
        return bean().owner();
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

    public void hookOnAnnotatedField(Annotation annotation, BeanField of) {
        throw new BeanInstallationException(extension().fullName() + " does not know how to handle " + annotation.annotationType() + " on " + of);
    }

    /**
     * A callback method that is called for fields that are annotated with a field hook annotation defined by the extension:
     *
     * is annotated with an annotation that itself is annotated with {@link AnnotatedFieldHook} and where
     * {@link AnnotatedFieldHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a given field and extension. Even if there are multiple matching hook
     * annotations on the same field.
     *
     * @param hooks
     *            a non-empty collection of hooks
     * @param of
     *            an operational field
     * @see AnnotatedFieldHook
     */
    // onFieldHook(Set<Class<? extends Annotation<>> hooks, BeanField));
    public void hookOnAnnotatedField(AnnotationList hooks, BeanField of) {
        for (Annotation a : hooks) {
            hookOnAnnotatedField(a, of);
        }
    }

    public void hookOnAnnotatedMethod(Annotation hook, BeanMethod on) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        throw new InternalExtensionException(extension().fullName() + " failed to handle method annotation(s) " + hook);
    }

    /**
     * @param on
     *
     * @see AnnotatedMethodHook
     */
    public void hookOnAnnotatedMethod(AnnotationList hooks, BeanMethod on) {
        for (Annotation a : hooks) {
            hookOnAnnotatedMethod(a, on);
        }
    }

    /**
     * @param variable
     *            a binding
     *
     * @see AnnotatedBindingHook
     */
    public void hookOnProvidedAnnotatedVariable(Annotation hook, BeanVariable var) {
        throw new BeanInstallationException(extension().fullName() + " failed to handle parameter hook annotation(s) " + hook);
    }

    /**
     * @param v
     *
     * @see BindingTypeHook
     */
    public void hookOnProvidedVariableType(Class<?> hook, BeanWrappedVariable v) {
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
    final void initialize(BeanScannerExtension ce) {
        if (this.setup != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.setup = ce;
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
    private BeanScannerExtension setup() {
        BeanScannerExtension s = setup;
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
