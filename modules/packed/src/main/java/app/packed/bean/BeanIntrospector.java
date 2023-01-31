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
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import app.packed.bindings.BindableVariable;
import app.packed.bindings.BindableWrappedVariable;
import app.packed.bindings.Key;
import app.packed.bindings.Variable;
import app.packed.container.Realm;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.AnnotationList;
import app.packed.framework.Nullable;
import app.packed.operation.DelegatingOperationHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.OperationalExtension;
import internal.app.packed.bean.PackedAnnotationList;
import internal.app.packed.bean.PackedOperationalConstructor;
import internal.app.packed.bean.PackedOperationalField;
import internal.app.packed.bean.PackedOperationalMethod;
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
    private OperationalExtension setup;

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
        return bean().realm.realm();
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

    public void hookOnAnnotatedClass(Annotation hook, OperationalClass clazz) {}

    // Replace set with something like AnnotatedHookSet
    /**
     *
     * The default implementation calls {@link #hookOnAnnotatedClass(Annotation, OperationalClass)}
     *
     * @param hooks
     *            the annotation(s) that hook
     * @param an
     */
    public void hookOnAnnotatedClass(AnnotationList hooks, OperationalClass clazz) {
        for (Annotation a : hooks) {
            hookOnAnnotatedClass(a, clazz);
        }
    }

    public void hookOnAnnotatedField(Annotation hook, OperationalField of) {
        throw new InternalExtensionException(extension().fullName() + " failed to handle field annotation(s) " + hook);
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
    public void hookOnAnnotatedField(AnnotationList hooks, OperationalField of) {
        for (Annotation a : hooks) {
            hookOnAnnotatedField(a, of);
        }
    }

    public void hookOnAnnotatedMethod(Annotation hook, OperationalMethod on) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        throw new InternalExtensionException(extension().fullName() + " failed to handle method annotation(s) " + hook);
    }

    /**
     * @param on
     *
     * @see AnnotatedMethodHook
     */
    public void hookOnAnnotatedMethod(AnnotationList hooks, OperationalMethod on) {
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
    public void hookOnProvidedAnnotatedVariable(Annotation hook, BindableVariable var) {
        throw new InternalExtensionException(extension().fullName() + " failed to handle parameter hook annotation(s) " + hook);
    }

    /**
     * @param v
     *
     * @see BindingTypeHook
     */
    public void hookOnProvidedVariableType(Class<?> hook, BindableWrappedVariable v) {
        throw new InternalExtensionException(extension().fullName() + " failed to handle type hook " + StringFormatter.format(hook));
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
    final void initialize(OperationalExtension ce) {
        if (this.setup != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.setup = ce;
    }

    /** {@return whether or not this introspector is the installing introspector.} */
    public boolean isInstallingIntrospector() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@return the internal configuration class.}
     *
     * @throws IllegalStateException
     *             if called from the constructor of the class
     */
    private OperationalExtension setup() {
        OperationalExtension s = setup;
        if (s == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of " + getClass());
        }
        return s;
    }

    // CheckRealmIsApplication
    // CheckRealmIsExtension
    /**
     *
     * <p>
     * Members from the {@code java.lang.Object} class are never returned.
     */
    public interface OperationalClass {

        void forEachConstructor(Consumer<? super OperationalConstructor> action);

        void forEachField(Consumer<? super OperationalField> action);

        void forEachMethod(Consumer<? super OperationalMethod> action);

        boolean hasFullAccess();

        // Fields first, include subclasses, ... blabla
        // Maybe on top of full access have boolean custom processing on ClassHook
        void setProcessingStrategy(Object strategy);

        // Hvad med Invokeable thingies??? FX vi tager ExtensionContext for invokables
        // Masske har vi BeanClass.Builder() istedet for???

        // Cute men vi gider ikke supportere det
//       static BeanClass of(MethodHandles.Lookup caller, Class<?> clazz) {
//           throw new UnsupportedOperationException();
//       }

        Key<?> toKey();
    }

    /**
     * This class represents a {@link Constructor} on a bean.
     * <p>
     * Unlike field and methods hooks. There are no way to define hooks on constructors. Instead they must be defined on a
     * bean driver or a bean class. Which determines how constructors are processed.
     */
    // Do we need a BeanExecutable??? Not sure we have a use case
    // Or maybe we just have BeanMethod (Problem with constructor() though)
    public sealed interface OperationalConstructor permits PackedOperationalConstructor {

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();

        /**
         * Returns the modifiers of the constructor.
         *
         * @return the modifiers of the constructor
         * @see Constructor#getModifiers()
         * @apiNote the method is named getModifiers instead of modifiers to be consistent with
         *          {@link Constructor#getModifiers()}
         */
        int modifiers();

        OperationHandle newOperation(OperationTemplate template);

        /** {@return a factory type for this method.} */
        OperationType operationType();
    }

    /**
     * This class represents a {@link Field} on a bean.
     *
     * @see BaseExtensionPoint.AnnotatedFieldHook
     * @see BeanIntrospector#hookOnAnnotatedField(BeanProcessor$BeanField)
     *
     * @apiNote There are currently no support for obtaining a {@link VarHandle} for a field.
     */
    public sealed interface OperationalField permits PackedOperationalField {

        /** {@return an annotation reader for the field.} */
        AnnotationList annotations();

        /**
         * @param postFix
         *            the message to include in the final message
         *
         * @throws BeanInstallationException
         *             always thrown
         */
        void failWith(String postFix);

        /** {@return the underlying field.} */
        Field field();

        /**
         * {@return the modifiers of the field.}
         *
         * @see Field#getModifiers()
         */
        int modifiers();

        default BindableVariable newInjectOperation() {
            throw new UnsupportedOperationException();
        }

        /**
         * Creates a new operation that can read the field.
         * <p>
         * If an {@link OperationMirror} is created for the operation. It will report {@link OperationTarget.OfField} as its
         * {@link OperationMirror#target()}.
         *
         * @param template
         *            a template for the operation
         * @return an operation handle
         * @see Lookup#unreflectGetter(Field)
         */
        OperationHandle newGetOperation(OperationTemplate template);

        /**
         * Creates a new operation that can read or/and write a field as specified by the provided access mode.
         * <p>
         * If an {@link OperationMirror} is created for this operation. It will report {@link OperationTarget.OfField} as its
         * {@link OperationMirror#target()}.
         *
         * @param template
         *            a template for the operation
         * @param accessMode
         *            the access mode of the operation
         *
         * @return an operation handle
         *
         * @see VarHandle#toMethodHandle(java.lang.invoke.VarHandle.AccessMode)
         *
         * @apiNote There are currently no way to create more than one MethodHandle per operation (for example one for reading
         *          and one for writing a field). You must create an operation per access mode instead. It is also currently not
         *          possible to get a VarHandle for the field
         */
        OperationHandle newOperation(OperationTemplate template, VarHandle.AccessMode accessMode);

        /**
         * Creates a new operation that can write to a field.
         * <p>
         * If an {@link OperationMirror} is created for this operation. It will report {@link OperationTarget.OfField} as its
         * {@link OperationMirror#target()}.
         *
         * @param template
         *            a template for the operation
         * @return an operation handle
         *
         * @see Lookup#unreflectSetter(Field)
         */
        OperationHandle newSetOperation(OperationTemplate template);

        /**
         * Attempts to convert field to a {@link Key} or fails by throwing {@link KeyExceptio} if the field does not represent a
         * proper key.
         * <p>
         * This method will use the exact type of the field. And not attempt to peel away injection wrapper types such as
         * {@link Optional} before constructing the key. As a binding hook is typically used in cases where this would be
         * needed.
         *
         * @return a key representing the field
         *
         * @throws KeyException
         *             if the field does not represent a valid key
         */
        Key<?> toKey();

        /**
         * {@return the underlying field represented as a {@code Variable}.}
         *
         * @see Variable#ofField(Field)
         */
        Variable variable();
    }

    /**
     * This class represents a {@link Method} from which an {@link OperationHandle operation} can be created.
     */
    public sealed interface OperationalMethod permits PackedOperationalMethod {

        /** {@return an annotation reader for the method.} */
        AnnotationList annotations();

        /**
         * @param postFix
         *            the message to include in the final message
         *
         * @throws BeanInstallationException
         *             always thrown
         */
        void failWith(String postFix);

        /**
         * @return
         * @see AnnotatedMethodHook#allowInvoke()
         */
        boolean hasInvokeAccess();

        /** {@return the underlying method.} */
        Method method();

        /**
         * {@return the modifiers of the underlying method.}
         *
         * @see Method#getModifiers()
         */
        int modifiers();

        /**
         * Creates a new operation that can invoke the underlying method.
         *
         * @param template
         *            a template for the operation
         * @return an operation handle
         *
         * @throws InaccessibleBeanMemberException
         *             if the framework does not have access to invoke the method
         * @throws InternalExtensionException
         *             if the extension does not have access to invoke the method
         *
         * @see OperationTarget.OfMethodHandle
         * @see Lookup#unreflect(Method)
         * @see BeanMethodHook#allowInvoke()
         * @see BeanClassHook#allowFullPrivilegeAccess()
         */
        OperationHandle newOperation(OperationTemplate template);

        DelegatingOperationHandle newDelegatingOperation();

        /** {@return the default type of operation that will be created.} */
        OperationType operationType();

        /**
         * Attempts to convert the annotated return type of the method to a {@link Key}, or fails by throwing
         * {@link BeanInstallationException} if the annotated return type does not represent a valid key.
         * <p>
         * This method will not attempt to peel away injection wrapper types such as {@link Optional} before constructing the
         * key.
         *
         * @return a key representing the return type of the method
         *
         * @throws InvalidKeyException
         *             if the return type of the method does not represent a proper key
         */
        Key<?> toKey();
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
