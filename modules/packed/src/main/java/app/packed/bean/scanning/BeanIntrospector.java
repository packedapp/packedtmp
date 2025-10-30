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
package app.packed.bean.scanning;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanLifetime;
import app.packed.bean.BeanLocal.Accessor;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.sandbox.AttachmentConfiguration;
import app.packed.binding.BindingMirror;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.component.ComponentRealm;
import app.packed.context.Context;
import app.packed.extension.BaseExtensionPoint;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.InternalExtensionException;
import app.packed.operation.Op;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import app.packed.util.AnnotationList;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanIntrospectorSetup;
import internal.app.packed.bean.scanning.IntrospectorOnConstructor;
import internal.app.packed.bean.scanning.IntrospectorOnField;
import internal.app.packed.bean.scanning.IntrospectorOnMethod;
import internal.app.packed.bean.scanning.IntrospectorOnContextService;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.bean.scanning.IntrospectorOnVariableUnwrapped;
import internal.app.packed.extension.PackedExtensionHandle;
import internal.app.packed.lifecycle.lifetime.ContainerLifetimeSetup;
import internal.app.packed.util.accesshelper.AccessHelper;
import internal.app.packed.util.accesshelper.BeanScanningAccessHandler;

/**
 * A bean introspector is the primary way for extensions are the primary way for extensions to interacts the
 *
 * This class contains a number of overridable callback methods, all of them starting with {@code on}. TODO Make list
 * <p>
 * A bean introspector implementation must have a no-argument constructor. And must be located in the same module as the
 * extension itself. The implementation must be open for the frameworkxw
 */
public non-sealed abstract class BeanIntrospector<E extends Extension<E>> implements Accessor {

    /**
     * The internal configuration of this introspector. Is initially null but populated via
     * {@link #initialize(BeanIntrospectorSetup)}.
     */
    @Nullable
    private BeanIntrospectorSetup introspector;

    // Hvornaar skal den laves? Hvis vi kun har en
    public final <T> AttachmentConfiguration<T> attach(Op<T> op) {
        throw new UnsupportedOperationException();
    }

    public final <T> AttachmentConfiguration<T> attachIfAbsent(Op<T> op) {
        throw new UnsupportedOperationException();
    }

    /** {@return the base extension point} */
    public final BaseExtensionPoint base() {
        return new PackedExtensionHandle<>(introspector().extension()).use(BaseExtensionPoint.class);
    }

    /** {@return the bean (for internal use)} */
    private BeanSetup bean() {
        return requireNonNull(introspector().scanner.bean);
    }

    /**
     * {@return an annotation list containing all annotations on the bean}
     * <p>
     * This may differ do the actual class that defines the bean, in case it has been transformed in some way, for examle,
     * by a {@link app.packed.bean.BeanBuildHook} before installing.
     */
    public final AnnotationList beanAnnotations() {
        return bean().bean.annotations();
    }

    /** {@return the bean class that is being introspected} */
    public final Class<?> beanClass() {
        return bean().bean.beanClass;
    }

    /**
     * @param <H>
     * @param beanHandleType
     *            the type of the bean handle
     * @return the bean handle if
     * @throws ClassCastException
     *             if the bean handle is of another type then the specified
     */
    public final <H extends BeanHandle<?>> Optional<H> beanHandle(Class<H> beanHandleType) {
        if (isInstallingExtension()) {
            return Optional.of(beanHandleType.cast(bean().handle()));
        }
        return Optional.empty();
    }

    /** {@return the extension the bean was installed via.} */
    public final Class<? extends Extension<?>> beanInstallingExtension() {
        return bean().installedBy.extensionType;
    }

    /** {@return an annotation reader for the bean class.} */
    public final BeanLifetime beanKind() {
        return bean().beanKind;
    }

    /** {@return the owner of the bean.} */
    public final ComponentRealm beanOwner() {
        return bean().owner();
    }

    /** {@return the bean source kind.} */
    public final BeanSourceKind beanSourceKind() {
        return bean().bean.beanSourceKind;
    }

    /**
     * {@return the extension instance}
     */
    @SuppressWarnings("unchecked")
    public final E extension() {
        return (E) introspector().extension().instance();
    }

    private ExtensionDescriptor extensionDescriptor() {
        return ExtensionDescriptor.of(introspector().extensionClass);
    }

    /**
     * {@return a handle for the extension}
     */
    public final ExtensionHandle<E> extensionHandle() {
        return new PackedExtensionHandle<E>(introspector().extension());
    }

    /**
     * Invoked by a MethodHandle from ExtensionSetup.
     *
     * @param introspector
     *            the bean introspector setup
     * @throws IllegalStateException
     *             if called more than once
     */
    final void initialize(BeanIntrospectorSetup introspector) {
        if (this.introspector != null) {
            throw new IllegalStateException("This introspector has already been initialized.");
        }
        this.introspector = introspector;
    }

    /**
     * {@return the internal configuration class.}
     *
     * @throws IllegalStateException
     *             if called from the constructor of the class
     */
    private BeanIntrospectorSetup introspector() {
        BeanIntrospectorSetup s = introspector;
        if (s == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of " + getClass());
        }
        return s;
    }

    /**
     * {@return whether or not the extension that implements this introspector is also the extension that is installing the
     * bean.}
     */
    public final boolean isInstallingExtension() {
        return introspector().extensionClass == bean().installedBy.extensionType;
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

    public void onAnnotatedClass(Annotation annotation, BeanIntrospector.OnClass onClass) {
        throw new InternalExtensionException(
                extensionDescriptor().fullName() + " failed to handle class annotation " + annotation.annotationType() + " on " + beanClass());
    }

    /**
     * The default implementation calls {@link #hookOnAnnotatedClass(Annotation, OperationalClass)}
     *
     * @param annotations
     *            the annotations that used {@link app.packed.bean.BeanTrigger.}
     * @param clazz
     *            an object that can be used to
     */
    public void onAnnotatedClass(AnnotationList annotations, BeanIntrospector.OnClass onClass) {
        for (Annotation annotation : annotations) {
            onAnnotatedClass(annotation, onClass);
        }
    }

    /**
     * This method is called by the similar named method {@link #hookOnAnnotatedField(AnnotationList, BeanField)} for every
     * annotation.
     *
     * @param triggeringAnnotation
     *            the triggering annotation
     * @param field
     *            an operational field
     * @see BeanTrigger.OnAnnotatedField
     */
    public void onAnnotatedField(Annotation triggeringAnnotation, OnField onField) {
        throw new InternalExtensionException(
                extensionDescriptor().fullName() + " failed to handle triggering field annotation " + triggeringAnnotation.annotationType() + " on " + onField);
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
     * @param triggeringAnnotations
     *            a non-empty list of field triggering annotations
     * @param field
     *            the field that was triggered
     * @see BeanTrigger.OnAnnotatedField
     */
    // Combinations of Field Annotations, Variable Annotations & VariableType
    // Failures? or order of importancez
    // What about meta data annotations? Maybe this is mostly applicable to binding class hooks
    public void onAnnotatedField(AnnotationList triggeringAnnotations, OnField ofField) {
        for (Annotation annotation : triggeringAnnotations) {
            onAnnotatedField(annotation, ofField);
        }
    }

    // can we attach information to the method??? // fx @Lock(sdfsdfsdf) uden @Query???
    public void onAnnotatedMethod(Annotation annotation, BeanIntrospector.OnMethod onMethod) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        throw new InternalExtensionException(extensionDescriptor().fullName() + " failed to handle method annotation(s) " + annotation);
    }

    /**
     * @param method
     *            the method
     * @param annotations
     *            a list of annotations that triggered
     *
     * @see BeanTrigger.OnAnnotatedMethod
     */
    public void onAnnotatedMethod(AnnotationList annotations, BeanIntrospector.OnMethod onMethod) {
        for (Annotation annotation : annotations) {
            onAnnotatedMethod(annotation, onMethod);
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
    public void onAnnotatedVariable(Annotation annotation, OnVariable onVariable) {
        throw new BeanInstallationException(extensionDescriptor().fullName() + " failed to handle parameter hook annotation(s) " + annotation);
    }

    /**
     * @param v
     *
     * @see app.packed.context.ContextualServiceProvider
     * @see app.packed.context.InheritableContextualServiceProvider
     * @see BeanTrigger.AutoInject
     * @see BeanTrigger.AutoInjectInheritable
     */
    public void onExtensionService(Key<?> key, OnContextService service) {
        throw new BeanInstallationException(extensionDescriptor().fullName() + " cannot handle type hook " + key);
    }

    /**
     * A callback method that is invoked before any calls to any of the {@code hookOn} methods on this class.
     * <p>
     * This method can be used to setup data structures or perform validation.
     *
     * @see #onStop()
     */
    public void onStart() {}

    /**
     * A callback method that is called after any other callback methods on this class.
     * <p>
     * This method can be used to provide final validation or registration of the bean.
     * <p>
     * If an exception is thrown at any time doing processing of the bean this method will not be called.
     *
     * @see #onStart()
     */
    public void onStop() {}

    /**
     * Registers a action to run doing the code generation phase of the application.
     * <p>
     * If the application has no code generation phase. For example, if building a {@link BuildGoal#MIRROR}. The specified
     * action will not be executed.
     *
     * @param action
     *            the action to run
     * @throws IllegalStateException
     *             if the underlying bean is no longer configurable
     * @see BuildGoal#isCodeGenerating()
     */
    // juse use extensionHandle?? Dont know usecases, for now we just use for debugging
    public final void runOnCodegen(Runnable action) {
        extensionHandle().runOnCodegen(action);
    }

    /**
     *
     * <p>
     * Members from {@code java.lang.Object} are never returned.
     */
    public interface OnClass {

        /**
         * {@return a list of annotations on the class.}
         *
         * @see Class#getAnnotations()
         **/
        AnnotationList annotations();

        /**
         * @param postFix
         *            the message to include in the final message
         *
         * @throws BeanInstallationException
         *             always thrown
         */
        default void failWith(String message) {
            throw new BeanInstallationException(message);
        }

        void forEachConstructor(Consumer<? super OnConstructor> action);

        void forEachField(Consumer<? super OnField> action);

        void forEachMethod(Consumer<? super OnMethod> action);

        boolean hasFullAccess();

        // Fields first, include subclasses, ... blabla
        // Maybe on top of full access have boolean custom processing on ClassHook
        void setProcessingStrategy(Object strategy);

        // void includeJavaBaseMembers();

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

        // Hvad med Invokeable thingies??? FX vi tager ExtensionContext for invokables
        // Masske har vi BeanClass.Builder() istedet for???

        // Cute men vi gider ikke supportere det
        // static BeanClass of(MethodHandles.Lookup caller, Class<?> clazz) {
        // throw new UnsupportedOperationException();
        // }
    }

    /**
     * This class represents a {@link Constructor} on a bean.
     * <p>
     * Unlike field and methods hooks. There are no way to define hooks on constructors. Instead they must be defined on a
     * bean driver or a bean class. Which determines how constructors are processed.
     */
    public sealed interface OnConstructor permits IntrospectorOnConstructor {

        /**
         * {@return a list of annotations on the constructor}
         *
         * @see Constructor#getAnnotations()
         **/
        AnnotationList annotations();

        /** {@return the underlying constructor.} */
        Constructor<?> constructor();

        /**
         * @param postFix
         *            the message to include in the final message
         *
         * @throws BeanInstallationException
         *             always thrown
         */
        default void failWith(String message) {
            throw new BeanInstallationException(message);
        }

        /**
         * {@return the modifiers of the constructor}
         *
         * @see Constructor#getModifiers()
         */
        int modifiers();

        // LifetimeTemplate??? Also available for on Method????
        OperationInstaller newOperation(OperationTemplate template);

        /** {@return a factory type for this method.} */
        OperationType operationType();

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
    }

    public sealed interface OnContextService permits IntrospectorOnContextService {

        Class<?> baseClass();

        OnVariableUnwrapped binder();

        Set<Class<? extends Context<?>>> contexts();

        Key<?> key();

        // matchUnqualified
        default boolean matchNoQualifiers(Class<?> clazz) {
            if (key().rawType() == clazz) {
                if (key().isQualified()) {
                    throw new BeanInstallationException("oops");
                }
                return true;
            }
            return false;
        }

    }

    /**
     * This class represents a {@link Field} on a bean.
     * <p>
     * This class supports 3 different ways to create an operation for the underlying field.
     *
     * @see BeanIntrospector#onAnnotatedField(BeanField, java.lang.annotation.Annotation)
     * @see BeanIntrospector#onAnnotatedField(BeanField, AnnotationList)
     *
     * @apiNote There are currently no support for obtaining a {@link VarHandle} for a field.
     */
    public sealed interface OnField permits IntrospectorOnField {

        /**
         * {@return a list of annotations on the field}
         *
         * @see Field#getAnnotations()
         **/
        AnnotationList annotations();

        /**
         * Throws a BeanInstallationException with the details of the bean and field and specified message.
         *
         * @param message
         *            the message to include in the final message
         * @throws BeanInstallationException
         *             always thrown
         */
        void failWith(String message);

        /** {@return the underlying field.} */
        Field field();

        /**
         * {@return the modifiers of the field}
         *
         * @see Field#getModifiers()
         */
        int modifiers();

        /**
         * Creates a new operation that can read the underlying field.
         * <p>
         * If an {@link OperationMirror} is created for the operation. It will report {@link OperationTarget.OfField} as its
         * {@link OperationMirror#target()}.
         *
         * @param template
         *            a template for the operation
         * @return an operation handle
         * @see Lookup#unreflectGetter(Field)
         */
        OperationInstaller newGetOperation(OperationTemplate template);

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
         *          and one for writing a field). You must create an operation per access mode instead. Also, there is currently
         *          no way to obtain a VarHandle for the underlying field
         */
        OperationInstaller newOperation(OperationTemplate template, VarHandle.AccessMode accessMode);

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
        OperationInstaller newSetOperation(OperationTemplate template);

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
         * {@return a list of triggering annotations on the field}
         **/
        AnnotationList triggeringAnnotations();

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
    public sealed interface OnMethod permits IntrospectorOnMethod {

        /**
         * {@return a list of annotations on the method}
         *
         * @see Method#getAnnotations()
         **/
        AnnotationList annotations();

        /**
         * @param postFix
         *            the message to include in the final message
         *
         * @throws BeanInstallationException
         *             always thrown
         */
        default void failWith(String message) {
            throw new BeanInstallationException(message);
        }

        /**
         * {@return whether or not this bean method has invoke access to the underlying method}
         *
         * @see AnnotatedMethodHook#allowInvoke()
         */
        boolean hasInvokeAccess();

        /** {@return the underlying method (if the method is not synthetic).} */
        // Prob not in version 1. What if we just removed an annotation????
        // Probably need these in OperationTarget as well:(
        Optional<Method> method();

        /**
         * {@return the modifiers of the method}
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
        OperationInstaller newOperation(OperationTemplate template);

        /** {@return the default type of operation that will be created.} */
        OperationType operationType();

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
         * @throws InvalidKeyException
         *             if the return type of the method does not represent a valid key
         */
        Key<?> toKey();
    }

    /**
     * Represents a variable that can be bound at build time by an extension.
     * <p>
     *
     */
// Something about being embedded
// For example, deep down, we cannot resolve something. And we need to
// throw an exception. But we need to include the original method that could not
// be resolved.
// Or VariableBinder

    public sealed interface OnVariable permits IntrospectorOnVariable, OnVariableUnwrapped {
        /**
         * By default binding to static fields are not permitted. Any call to one of the bind methods of this interface will
         * result in an exception being thrown unless this method has been called first.
         * <p>
         * Calling this method has no effect if the underlying variable is not a field.
         *
         * @return this bindable variable
         * @throws IllegalStateException
         *             if the variable has already has been bound
         */
        OnVariable allowStaticFieldBinding();

        /** {@return a list of annotations on the element.} */
        AnnotationList annotations();

        /**
         * <p>
         * This method exist only for informational purposes.
         *
         * @return
         */
        default Set<Class<? extends Context<?>>> availableContexts() {
            return Set.of();
        }

        /**
         * NOTE: Invocation arguments are only available variables returned from Ope
         *
         * @
         */
        List<Class<?>> availableInvocationArguments();

        // Hmm, vi vil jo ogsaa gerne have contexts med...
        // Map<Context.class, List<>>

        /**
         * Binds the underlying variable to a constant that is computed exactly once. Typically, doing the application's code
         * generating phase.
         * <p>
         * If the application's {@link BuildGoal#isCodeGenerating() code generating} phase is never executed, for example, if
         * building an {@link app.packed.application.ApplicationMirror}. The specified supplier will never be called.
         * <p>
         * If the specified supplier returns a value that is not assignable to the underlying variable. The runtime will throw a
         * {@link CodegenException} when called.
         * <p>
         * The specified supplier is never invoked more than once.
         *
         * @param supplier
         *            the supplier of the constant
         * @throws IllegalStateException
         *             if the variable has already been bound.
         * @see #bindConstant(Object)
         */
        OnVariable bindComputedConstant(Supplier<@Nullable ?> supplier);

        /**
         * Binds the specified context to the underlying variable.
         * <p>
         * If you need to perform any kind of transformations on a particular context you can use {@link #bindOp(Op)} instead.
         * Taking the context as the sole argument and returning the result of the transformation.
         *
         * @param fromContext
         *
         * @throws ClassCastException
         *             if the type of the context is not assignable to the underlying variable
         * @throws app.packed.context.ContextNotAvailableException
         *             if the context is not available
         * @see #availableContexts()
         */
        OnVariable bindContext(Class<? extends Context<?>> context);

        /**
         * Binds the specified constant value to the underlying variable.
         *
         * @param value
         *            the value to bind
         * @throws IllegalArgumentException
         *             if {@code null} is specified and null is not a valid value for the variable
         * @throws ClassCastException
         *             if the type of the value is not assignable to the underlying parameter
         * @throws IllegalStateException
         *             if the variable has already been bound.
         * @see #bindGeneratedConstant(Supplier)
         */
        OnVariable bindInstance(@Nullable Object value);

        /**
         * @param index
         *            the index of the invocation argument in
         * @return
         *
         * @throws UnsupportedOperationException
         *             if called from a bindable variable that was not created from
         *             {@link sandbox.extension.operation.OperationHandle#manuallyBindable(int)}
         */
        // We need this, for example, for @OnEvent. Where the first argument is the event
        // Men hmm, hvad fx med ExtensionContext
        OnVariable bindInvocationArgument(int index);

        /**
         * Binds an operation whose return value will be used as the variable argument. The specified operation will be invoked
         * every time the the variable is requested.
         * <p>
         * There are no direct support for lazy computation or caching of computed values.
         *
         * @param op
         *            the operation to bind
         * @throws ClassCastException
         *             if the return type of the op is not assignable to the variable
         */
        OnVariable bindOp(Op<?> op);

        /**
         * Checks that the underlying variable is {@link Class#isAssignableFrom(Class) assignable} to one of the specified
         * classes.
         *
         * @param classes
         *            the classes to check
         * @return the first class in the specified array that the variable is assignable to
         * @see Class#isAssignableFrom(Class)
         */
        // maybe replace it with notAssignableTo
        default Class<?> checkAssignableTo(Class<?>... classes) {
            if (classes.length == 0) {
                throw new IllegalArgumentException("Cannot specify an empty array");
            }
            Class<?> rawType = variable().rawType();
            for (Class<?> c : classes) {
                if (c.isAssignableFrom(rawType)) {
                    return c;
                }
            }
            List<String> cls = List.of(classes).stream().map(c -> c.getSimpleName()).toList();
            throw new BeanInstallationException(variable() + ", Must be assignable to one of " + cls);
        }

        /**
         * @param postFix
         *            the message to include in the final message
         *
         * @throws BeanInstallationException
         *             always thrown
         */
        default void failWith(String message) {
            throw new BeanInstallationException(message);
        }

        /**
         * {@return the extension that is responsible for invoking the underlying non-nested operation}
         */
        Class<? extends Extension<?>> invokedBy();

        /** {@return whether or not the underlying variable has been bound.} */
        boolean isBound();

        /**
         * Specializes the binding mirror.
         * <p>
         * Notice: We are actually specializing the binding and not the variable.
         *
         * @param supplier
         *            the supplier used to create the binding mirror
         * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
         *          must be returned
         * @return this bindable variable
         */
        OnVariable mirror(Supplier<? extends BindingMirror> supplier);

        /** {@return the raw type of the variable.} */
        default Class<?> rawType() {
            return variable().rawType();
        }

        /**
         * Parses the variable as a key.
         *
         * @return a key representing the variable
         *
         * @throws InvalidKeyException
         *             if the variable does not represent a valid key
         */
        Key<?> toKey();

        default OnVariableUnwrapped unwrap() {
            // peel ->

            // peel, unwrap
            // Ville vaere fedt hvis alle metoderne havde samme prefix
            throw new UnsupportedOperationException();
        }

        /** {@return the variable that can be bound.} */
        Variable variable();

        // The target of a binding, Put on variable instead???
        // Syntes faktisk ikke det giver mening at have den her, men ikke paa variable

        // What about Variable in an OP?? And what if the Op wraps a field???
        // Target (Object?) + TargetKind (Enum)
        /**
         * The target of a bindable variable.
         */
        enum Target {
            /** The variable represents a field. */
            FIELD, PARAMETER, TYPE_PARAMETER; // What if MethodHandle???
        }
    }

    /**
     * A bindable variable whose type has been unwrapped from various "container objects" such as {@link Optional},
     * {@link app.packed.operation.Provider}
     *
     * <p>
     * The following container objects and annotations will be processed by default:
     *
     * Opts into
     *
     * Optional
     *
     * Provider
     *
     * Lazy
     *
     * Nullable
     *
     * Validate (Er jo i virkeligheden peek??
     *
     * Default
     *
     * <p>
     * By default there are a number of combinations that will fail:
     *
     * Optional, Nullable, default annotation combinations makes no sense
     */
    // BeanInjectableVariablw
    public sealed interface OnVariableUnwrapped extends OnVariable permits IntrospectorOnVariableUnwrapped {

        @Override
        OnVariableUnwrapped allowStaticFieldBinding();

        /**
         * Binds the variable to the {@code empty value} if not {@link #isRequired()}. Otherwise fails with
         * {@link RuntimeException} (ValueRequiredException) if a binding is required
         * <p>
         * What exactly none means depends on the underlying variable:
         *
         * Optional Class: Binds to {@link Optional#empty()}, {@link java.util.OptionalLong#empty()},
         * {@link java.util.OptionalDouble#empty()}, {@link java.util.OptionalInt#empty()} respectively none {@link Nullable},
         * {@link Optional}, Default value.
         *
         * Default Value: The default value as specified by the annotation
         *
         * Nullable: {@code null}
         *
         * <p>
         *
         * @throws UnsupportedOperationException
         *             if the underlying field does not support
         */
        void bindNone();

        // Problemet er vi gerne vil smide en god fejlmeddelse
        // Det kan man vel ogsaa...
        //// isOptional()->bindOptionallTo()
        //// else provide()
        // Will automatically handle, @Nullable, and Default

//        default void bindOpWithNoneAsNull(Op<?> op) {
//            bindOpWithNoneToken(op, null);
//        }

        // bindNoneableOp
        // bindNoneableOpWithToken
        // bindNoneableOpWithOptional

        // Wrapper er klassen som op returnere. Skal matche med Op
        // isEmpty (boolean, Op.returnValue) -> ifTrue -> none, otherwise uses valueExtractor
        // ValueExtractor(T, Op.returnValue)
        default void bindNoneableOp(Op<?> op, MethodHandle isEmptyTest, MethodHandle valueExtractor) {}

        // Op must have Optional, OptionalLong, OptionalInt or OptionalDouble as result
        default void bindNoneableOpWithOptional(Op<?> op) {}

        // Replace with Supplier<Throwable>? skal have denne version for all empty binders
        default void bindNoneableOpWithOptional(Op<?> op, Supplier<Throwable> throwing) {}

        /**
         * Uses identity
         * <p>
         * This method is typically called with {@code null} as the token.
         *
         * @param op
         * @param noneToken
         */
        default void bindNoneableOpWithToken(Op<?> op, @Nullable Object noneToken) {}

        /**
         * {@return whether or not the variable can be bound to none}
         * <p>
         * A variable can be bound to none if it is either: <tt>
         * <li>Is nullable</li>
         * <li>Is wrapped in an optional container (Optional, OptionalLong, OptionalDouble, or OptionalInt)
         * <li>Is annotated with default annotation such as Default</li>
         * </tt>
         */
        boolean canBindNone();

        /**
         * Checks that the unwrapped variable it not nullable, is not wrapped in an optional container, and is not annotated
         * with a default annotation.
         *
         * @throws app.packed.bean.BeanInstallationException
         *             if the unwrapped variable is not required
         */
        void checkNoneable();

        void checkNotNoneable();

        boolean hasDefaults();

        boolean isLazy();

        boolean isNullable();

        // isOptionalContainer
        boolean isOptional();

        boolean isProvider();

        default boolean isWrapped() {
            return false;
        }

        // BindableVariable source()???
        Variable originalVariable();

        // Usecase???
        default boolean tryBindNone() {
            if (!canBindNone()) {
                bindNone();
                return true;
            }
            return false;
        }

        default List<Annotation> wrapperAnnotations() {
            return List.of();
        }

        /** {@return a list of all wrapper classes in applied order} */
        default List<Class<?>> wrapperClasses() {
            return List.of();
        }

        /**
        *
        */
        public enum WrapperKind {
            LAZY, OPTIONAL, PROVIDER;
        }
    }

    interface Sanfbox {

        // bindLazy-> Per Binding? PerOperation? PerBean, ?PerBeanInstance ?PerContainer ? PerContainerInstance ?
        // PerApplicationInstance. Can only do this if is invoking extension!!
        void bindCachableOp(Object cacheType, Op<?> op);

        // Ideen er den bliver instantiteret hver gang (Er det en bean saa???)

        // Det er lidt som en OP

        // Tror vi skriver meget klart, at man kun scanner efter inject...
        // Alle andre annoteringer bliver ignoreret
        // Altsaa man kan jo bare finde constructeren selv, og saa kalde
        // Constructor c= Foo.class.getConstroctur(int.class, String.class);
        // Op.ofConstructor(MethodHandles.lookup(), c);
        void bindComposite(Class<?> compositeClass); // bindComposite?

        /**
         *
         * @throws UnsupportedOperationException
         *             if the underlying variable is not a {@link Record}
         */
        // En composite
        // Er det en bruger record? Hvem kan laese den...
        void bindCompositeRecord(); // bindComposite?
        // Har saa mange metoder i forvejen

        OnVariable bindInvocationArgument(int index, MethodHandle transformer);

        default boolean isAssignableTo(Class<?>... classes) {
            if (classes.length == 0) {
                throw new IllegalArgumentException("Cannot specify an empty array of classes");
            }
            Class<?> rawType = Class.class; // variable().getRawType();
            for (Class<?> c : classes) {
                if (c.isAssignableFrom(rawType)) {
                    return true;
                }
            }
            return false;
        }

    }

    static {
        AccessHelper.initHandler(BeanScanningAccessHandler.class, new BeanScanningAccessHandler() {
            @Override
            public BeanSetup invokeBeanIntrospectorBean(BeanIntrospector<?> introspector) {
                return introspector.bean();
            }

            @Override
            public void invokeBeanIntrospectorInitialize(BeanIntrospector<?> introspector, BeanIntrospectorSetup ref) {
                introspector.initialize(ref);
            }
        });
    }
}
