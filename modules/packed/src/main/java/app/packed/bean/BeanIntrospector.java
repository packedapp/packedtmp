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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

import app.packed.bean.BeanBuildLocal.Accessor;
import app.packed.bean.BeanElement.BeanClass;
import app.packed.bean.BeanElement.BeanField;
import app.packed.bean.BeanElement.BeanMethod;
import app.packed.binding.BindableVariable;
import app.packed.binding.Key;
import app.packed.binding.UnwrappedBindableVariable;
import app.packed.build.BuildActor;
import app.packed.context.Context;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.InternalExtensionException;
import app.packed.lifecycle.LifecycleKind;
import app.packed.util.AnnotationList;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanScannerParticipant;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.util.PackedAnnotationList;

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
public non-sealed abstract class BeanIntrospector implements Accessor {

    /**
     * The configuration of this introspector. Is initially null but populated via
     * {@link #initialize(ExtensionDescriptor, BeanSetup)}.
     */
    @Nullable
    private BeanScannerParticipant extension;

    BeanSetup bean() {
        return requireNonNull(setup().scanner.bean);
    }

    /** {@return an annotation reader for the bean class.} */
    public final AnnotationList beanAnnotations() {
        return new PackedAnnotationList(beanClass().getAnnotations());
    }

    /** {@return the owner of the bean.} */
    public final BuildActor beanAuthor() {
        return bean().owner();
    }

    /** {@return the bean class that is being introspected.} */
    public final Class<?> beanClass() {
        return bean().beanClass;
    }

    public final <H extends BeanHandle<?>> Optional<H> beanHandle(Class<H> handleKind) {
        if (isBeanInstallingExtension()) {
            return Optional.of(handleKind.cast(bean().handle()));
        }
        return Optional.empty();
    }

    /** {@return the extension the bean was installed via.} */
    public final Class<? extends Extension<?>> beanInstallingExtension() {
        return bean().installedBy.extensionType;
    }

    /** {@return an annotation reader for the bean class.} */
    public final BeanKind beanKind() {
        return bean().beanKind;
    }

    /** {@return the extension the bean was installed via.} */
    public final LifecycleKind beanLifetimeKind() {
        return bean().lifetime.lifetimeKind();
    }

    /** {@return the bean source kind.} */
    public final BeanSourceKind beanSourceKind() {
        return bean().beanSourceKind;
    }

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
    final void initialize(BeanScannerParticipant ce) {
        if (this.extension != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.extension = ce;
    }

    /**
     * {@return whether or not the extension that implements this introspector is also the extension that is installing the
     * bean.}
     */
    public final boolean isBeanInstallingExtension() {
        return extension.extension == bean().installedBy;
    }

    /** {@return whether or not the bean is in same lifetime as the application.} */
    public final boolean isInApplicationLifetime() {
        BeanSetup b = bean();
        return b.lifetime == b.container.application.container().lifetime;
    }

    /** {@return whether or not the bean is in same lifetime as its container.} */
    public final boolean isInContainerLifetime() {
        return bean().lifetime instanceof ContainerLifetimeSetup;
    }

    public void onAnnotatedClass(Annotation annotation, BeanClass clazz) {
        throw new InternalExtensionException(
                extension().fullName() + " failed to handle class annotation " + annotation.annotationType() + " on " + beanClass());
    }

    /**
     * The default implementation calls {@link #hookOnAnnotatedClass(Annotation, OperationalClass)}
     *
     * @param annotations
     *            the annotations that used {@link app.packed.bean.BeanTrigger.AnnotatedClassBeanTrigger}
     * @param clazz
     *            an object that can be used to
     */
    public void onAnnotatedClass(AnnotationList annotations, BeanClass clazz) {
        for (Annotation a : annotations) {
            onAnnotatedClass(a, clazz);
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
    public void onAnnotatedField(Annotation annotation, BeanField field) {
        throw new InternalExtensionException(extension().fullName() + " failed to handle field annotation " + annotation.annotationType() + " on " + field);
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
     *            a non-empty list of field activating annotations
     * @param field
     *            an operational field
     * @see app.packed.extension.ExtensionMetaHook.AnnotatedBeanFieldHook
     */
    // Combinations of Field Annotations, Variable Annotations & VariableType
    // Failures? or order of importancez
    // What about meta data annotations? Maybe this is mostly applicable to binding class hooks
    public void onAnnotatedField(AnnotationList hooks, BeanField field) {
        for (Annotation a : hooks) {
            onAnnotatedField(a, field);
        }
    }

    // can we attach information to the method???
    // fx @Lock(sdfsdfsdf) uden @Query???
    public void onAnnotatedMethod(Annotation hook, BeanMethod method) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        throw new InternalExtensionException(extension().fullName() + " failed to handle method annotation(s) " + hook);
    }

    /**
     * @param on
     *
     * @see AnnotatedMethodHook
     */
    public void onAnnotatedMethod(AnnotationList hooks, BeanMethod method) {
        for (Annotation a : hooks) {
            onAnnotatedMethod(a, method);
        }
    }

    /**
     * <p>
     * This method is never called directly by the framework. Instead the framework will always call
     * {@link #triggeredByAnnotatedMethod(AnnotationList, BeanMethod)} which in turn per default will call this method with
     * each of the relevant annotations. If you need to process multiple annotations at the same time override
     * {@link #triggeredByAnnotatedMethod(AnnotationList, BeanMethod)}. If you are fine with processing annotations
     * separately override this method.
     *
     * <p>
     * A variable can never be annotated with more than 1 operational
     *
     * @param variable
     *            a binding
     *
     * @see AnnotatedBindingHook
     */
    public void onAnnotatedVariable(Annotation hook, BindableVariable binder) {
        throw new BeanInstallationException(extension().fullName() + " failed to handle parameter hook annotation(s) " + hook);
    }

    /**
     * @param v
     *
     * @see app.packed.context.ContextualServiceProvider
     * @see app.packed.context.InheritableContextualServiceProvider
     */
    public void onContextualServiceProvision(Key<?> key, Class<?> baseClass, Set<Class<? extends Context<?>>> contexts, UnwrappedBindableVariable binder) {
        throw new BeanInstallationException(extension().fullName() + " cannot handle type hook " + key);
    }

    public void onContextualServiceProvision2(Key<?> key, Set<Class<? extends Context<?>>> contexts, Optional<Class<?>> baseClass,
            UnwrappedBindableVariable binder) {
        throw new BeanInstallationException(extension().fullName() + " cannot handle type hook " + key);
    }

    // BaseClass = ApplicationMirror, for Key<MyApplicationMirror>
    // For dynamic classes I think it is just the raw class
    public void onInheritableContextualServiceProvision2(Key<?> key, Class<?> baseClass, Set<Class<? extends Context<?>>> contexts,
            UnwrappedBindableVariable binder) {
        throw new BeanInstallationException(extension().fullName() + " cannot handle type hook " + key);
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
    public void onStart() {}

    /**
     * A callback method that is invoked before any calls to any of the {@code hookOn} methods on this class.
     * <p>
     * This method can be used to setup data structures or perform validation.
     *
     * @see #beforeHooks()
     */
    public void onStop() {}

    /**
     * {@return the internal configuration class.}
     *
     * @throws IllegalStateException
     *             if called from the constructor of the class
     */
    private BeanScannerParticipant setup() {
        BeanScannerParticipant s = extension;
        if (s == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of " + getClass());
        }
        return s;
    }
}
