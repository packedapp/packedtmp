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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanHook.AnnotatedFieldHook;
import app.packed.bean.BeanHook.AnnotatedMethodHook;
import app.packed.bean.BeanHook.AnnotatedVariableHook;
import app.packed.bean.BeanHook.VariableTypeHook;
import app.packed.binding.InvalidKeyException;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.binding.mirror.BindingMirror;
import app.packed.container.Realm;
import app.packed.context.Context;
import app.packed.context.ContextTemplate.InvocationContextArgument;
import app.packed.extension.BaseExtensionPoint;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionDescriptor;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.Nullable;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTarget;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationTemplate.InvocationArgument;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanAnnotationReader;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.IntrospectedOperationalField;
import internal.app.packed.bean.IntrospectedOperationalMethod;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.service.KeyHelper;

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
    private Setup setup;

    /**
     * A callback method that is invoked before any calls to any of the {@code hookOn} methods on this class.
     * <p>
     * This method can be used to setup data structures or perform validation.
     * 
     * @see #beforeHooks()
     */
    public void afterHooks() {}

    /** {@return an annotation reader for the bean class.} */
    public final AnnotationReader beanAnnotations() {
        return new BeanAnnotationReader(beanClass().getAnnotations());
    }

    /** {@return the bean class that is being introspected.} */
    public final Class<?> beanClass() {
        return setup().bean.beanClass;
    }

    /** {@return the extension the bean was installed via.} */
    public final Class<? extends Extension<?>> beanInstalledVia() {
        return setup().bean.installedBy.extensionType;
    }

    /** {@return an annotation reader for the bean class.} */
    public final BeanKind beanKind() {
        return setup().bean.beanKind;
    }

    /** {@return the owner of the bean.} */
    public final Realm beanOwner() {
        return setup().bean.realm.realm();
    }

    /** {@return an annotation reader for the bean class.} */
    public final BeanSourceKind beanSourceKind() {
        return setup().bean.sourceKind;
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

    /**
     * @param postFix
     *            the message to include in the final message
     * 
     * @throws InvalidBeanDefinitionException
     *             always thrown
     */
    public final void failWith(String postFix) {
        throw new InvalidBeanDefinitionException("OOPS " + postFix);
    }

    // Replace set with something like AnnotatedHookSet
    public void hookOnAnnotatedClass(Set<Class<? extends Annotation>> hooks, OperationalClass on) {}

    /**
     * A callback method that is called for fields that are annotated with a field hook annotation defined by the extension:
     * 
     * is annotated with an annotation that itself is annotated with {@link AnnotatedFieldHook} and where
     * {@link AnnotatedFieldHook#extension()} matches the type of this extension.
     * <p>
     * This method is never invoked more than once for a given field and extension. Even if there are multiple matching hook
     * annotations on the same field.
     * 
     * @param of
     *            an operational field
     * @see AnnotatedFieldHook
     */
    // onFieldHook(Set<Class<? extends Annotation<>> hooks, BeanField));
    public void hookOnAnnotatedField(Set<Class<? extends Annotation>> hooks, OperationalField of) {
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle field annotation(s) " + hooks);
    }

    /**
     * @param on
     * 
     * @see AnnotatedMethodHook
     */
    public void hookOnAnnotatedMethod(Set<Class<? extends Annotation>> hooks, OperationalMethod on) {
        // Test if getClass()==BeanScanner forgot to implement
        // Not we want to return generic bean scanner from newBeanScanner
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle method annotation(s) " + hooks);
    }

    /**
     * @param variable
     *            a binding
     * 
     * @see AnnotatedVariableHook
     */
    public void hookOnAnnotatedVariable(Annotation hook, BindableVariable v) {
        // could test if getClass is beanIntrospector, in which case they probably forgot to override extension.newIntrospector
        // Otherwise they forgot to implement binding hook
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle parameter hook annotation(s) " + hook);
    }

    /**
     * @param v
     * 
     * @see VariableTypeHook
     */
    public void hookOnVariableType(Class<?> hook, BindableBaseVariable v) {
        throw new InternalExtensionException(setup().extension.fullName() + " failed to handle parameter hook annotation(s) " + hook);
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
    final void initialize(ExtensionSetup operator, BeanSetup bean) {
        if (this.setup != null) {
            throw new IllegalStateException("This scanner has already been initialized.");
        }
        this.setup = new Setup(operator.model, bean);
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
    private Setup setup() {
        Setup s = setup;
        if (s == null) {
            throw new IllegalStateException("This method cannot be called from the constructor of " + getClass());
        }
        return s;
    }

    /**
     * An annotation reader can be used to process annotations on bean elements.
     * 
     * @see AnnotatedElement
     */
    // Maybe BeanAnnotationReader? Don't think we will use it elsewhere?
    // AnnotatedBeanElement?
    public sealed interface AnnotationReader permits BeanAnnotationReader {

        /** {@return whether or not there are any annotations to read.} */
        boolean hasAnnotations();

        default <T extends Annotation> void ifPresent(Class<T> annotationClass, Consumer<T> consumer) {
            T t = readRequired(annotationClass);
            consumer.accept(t);
        }

        boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);

        // Det er taenk
        Annotation[] readAnyOf(Class<?>... annotationTypes);

        /**
         * Returns a annotation of the specified type or throws {@link InvalidBeanDefinitionException} if the annotation is not
         * present
         * 
         * @param <T>
         *            the type of the annotation to query for and return if present
         * @param annotationClass
         *            the Class object corresponding to the annotation type
         * @return the annotation for the specified annotation type if present
         * 
         * @throws InvalidBeanDefinitionException
         *             if the specified annotation is not present or the annotation is a repeatable annotation and there are not
         *             exactly 1 occurrences of it
         * 
         * @see AnnotatedElement#getAnnotation(Class)
         */
        //// foo bean was expected method to dddoooo to be annotated with
        <T extends Annotation> T readRequired(Class<T> annotationClass);

        // Q) Skal vi bruge den udefra beans???
        // A) Nej vil ikke mene vi beskaeftiger os med andre ting hvor vi laeser det.
        // Altsaa hvad med @Composite??? Det er jo ikke en bean, det bliver noedt til at vaere fake metoder...
        // Paa hver bean som bruger den...
        // Vi exponere den jo ikke, saa kan jo ogsaa bare bruge den...

        // I think the only we reason we call it BeanAnnotationReader is because
        // if we called AnnotationReader is should really be located in a utility package
    }

    /**
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
     */
    public interface BindableBaseVariable extends BindableVariable {

        /**
         * Binds to {@link Nullable}, {@link Optional}, Default value.
         * 
         * <p>
         * For raw er det automatisk en fejl
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
        default void bindToOptional(Op<Optional<?>> op) {}

        default void bindToOptional(Op<Optional<?>> op, Runnable missing) {}

        void checkNotRequired();

        void checkRequired();

        boolean hasDefaults();

        boolean isLazy();

        boolean isNullable();

        boolean isOptional();

        boolean isProvider();

        boolean isRequired();

        Variable originalVariable();

        default boolean tryBindNone() {
            return false;
        }
    }

    /**
     *
     */
    // Hoved problemet er wrappers og denne gradvise peeling
    // Hvordan det praecis skal foregaa er lidt ukendt
    public interface BindableVariable {

        AnnotationReader annotations();

        default Map<Class<? extends Context<?>>, List<Class<?>>> availableContexts() {
            return Map.of();
        }

        // Hmm, vi vil jo ogsaa gerne have contexts med...
        // Map<Context.class, List<>>
        default List<Class<?>> availableInvocationArguments() {
            return List.of();
        }

        /**
         * 
         * @throws UnsupportedOperationException
         *             if the underlying variable is not a {@link Record}
         */
        default void bindCompositeRecord() {} // bindComposite?

        /**
         * Binds the specified value to the parameter.
         * <p>
         * Vi tager Nullable med saa vi bruge raw.
         * <p>
         * Tror vi smider et eller andet hvis vi er normal og man angiver null. Kan kun bruges for raw
         * 
         * @param value
         *            the value to bind
         * @throws ClassCastException
         *             if the type of the value does not match the underlying parameter
         * @throws IllegalStateException
         *             if a binding has already been created for the underlying parameter.
         */
        void bindConstant(@Nullable Object value);

        /**
         * 
         * 
         * @param op
         */
        // provideConverted(new Op2<@InvocationArgument(index = 0, context = HttpRequestContext.class) RequestImpl, String)

        // provide(new Op2<@InvocationArgument(index = 0, context = HttpRequestContext.class) RequestImpl, String)
        void bindTo(Op<?> op);

        /**
         * @param argumentIndex
         *            the index of the argument
         * 
         * @throws IndexOutOfBoundsException
         * @throws IllegalArgumentException
         *             if the invocation argument is not of kind {@link OperationTemplate.ArgumentKind#ARGUMENT}
         * @throws UnsupportedOperationException
         *             if the {@link #invokedBy()} is not identical to the binding extension
         * @throws ClassCastException
         * 
         * @see OperationTemplate
         * @see InvocationArgument
         */
        default void bindToInvocationArgument(int argumentIndex) {
            throw new UnsupportedOperationException();
        }

        /**
         * @param argumentIndex
         * @param context
         * 
         * @see InvocationContextArgument
         */
        default void bindToInvocationContextArgument(Class<? extends Context<?>> context, int argumentIndex) {
            throw new UnsupportedOperationException();
        }

        // bindLazy-> Per Binding? PerOperation? PerBean, ?PerBeanInstance ?PerContainer ? PerContainerInstance ?
        // PerApplicationInstance

        // Kan only do this if is invoking extension!!

        default void checkAssignableTo(Class<?> clazz, Class<?>... additionalClazzes) {

        }

        /**
         * @param postFix
         *            the message to include in the final message
         * 
         * @throws InvalidBeanDefinitionException
         *             always thrown
         */
        default void failWith(String postFix) {
            throw new InvalidBeanDefinitionException("OOPS " + postFix);
        }

        /** {@return the extension that is responsible for invoking the underlying operation.} */
        Class<? extends Extension<?>> invokedBy();

        default boolean isAssignable(Class<?> clazz, Class<?>... additionalClazzes) {
            return false;
        }

        /** {@return the raw type of the variable.} */
        default Class<?> rawType() {
            return variable().getRawType();
        }

        // we are actually specializing the binding and not the variable.
        // But don't really want to create a BindingHandle... just for this method
        BindableVariable specializeMirror(Supplier<? extends BindingMirror> supplier);

        Variable variable();

        /**
         * @return
         * 
         * @throws InvalidKeyException
         *             if a valid key could not be read
         */
        // readAsKey, parseKey?
        default Key<?> variableToKey() {
            throw new UnsupportedOperationException();
        }

        default BindableBaseVariable wrapAsBaseBindable() {
            // peel ->

            // peel, unwrap
            // Ville vaere fedt hvis alle metoderne havde samme prefix
            throw new UnsupportedOperationException();
        }
    }

    // CheckRealmIsApplication
    // CheckRealmIsExtension
    /**
     *
     * <p>
     * Members from the {@code java.lang.Object} class are never returned.
     */
    public interface OperationalClass {

        void forEachConstructor(Consumer<? super OperationalConstructor> m);

        void forEachMethod(Consumer<? super OperationalMethod> m);

        boolean hasFullAccess();

        // Hvad med Invokeable thingies??? FX vi tager ExtensionContext for invokables
        // Masske har vi BeanClass.Builder() istedet for???

        // Cute men vi gider ikke supportere det
//       static BeanClass of(MethodHandles.Lookup caller, Class<?> clazz) {
//           throw new UnsupportedOperationException();
//       }

        // Fields first, include subclasses, ... blabla
        // Maybe on top of full access have boolean custom processing on ClassHook
        void setProcessingStrategy(Object strategy);
    }

    /**
     * This class represents a {@link Constructor} on a bean.
     * <p>
     * Unlike field and methods hooks. There are no way to define hooks on constructors. Instead they must be defined on a
     * bean driver or a bean class. Which determines how constructors are processed.
     */
    // Do we need a BeanExecutable??? Not sure we have a use case
    // Or maybe we just have BeanMethod (Problem with constructor() though)
    public interface OperationalConstructor {

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
        int getModifiers();

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
    public sealed interface OperationalField permits IntrospectedOperationalField {

        /** {@return an annotation reader for the field.} */
        AnnotationReader annotations();

        // newBindableOperation?
        default BindableVariable bindable() {
            // get access is checked when we create the bindinable
            throw new UnsupportedOperationException();
        }

        /**
         * @param postFix
         *            the message to include in the final message
         * 
         * @throws InvalidBeanDefinitionException
         *             always thrown
         */
        default void failWith(String postFix) {
            throw new InvalidBeanDefinitionException("Field " + field() + ": " + postFix);
        }

        /** {@return the underlying field.} */
        Field field();

        /**
         * Attempts to convert field to a {@link Key} or fails by throwing {@link InvalidBeanDefinitionException} if the field
         * does not represent a proper key.
         * <p>
         * This method will not attempt to peel away injection wrapper types such as {@link Optional} before constructing the
         * key. As a binding hook is typically used in cases where this would be needed.
         * 
         * @return a key representing the field
         * 
         * @throws InvalidBeanDefinitionException
         *             if the field does not represent a proper key
         */
        default Key<?> fieldToKey() {
            return KeyHelper.convertField(field());
        }

        /**
         * {@return the modifiers of the field.}
         * 
         * @see Field#getModifiers()
         */
        int modifiers();

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
         * {@return the underlying field represented as a {@code Variable}.}
         * 
         * @see Variable#ofField(Field)
         */
        Variable variable();
    }

    /**
     * This class represents a {@link Method} from which an {@link OperationHandle operation} can be created.
     */
    public sealed interface OperationalMethod permits IntrospectedOperationalMethod {

        /** {@return an annotation reader for the method.} */
        AnnotationReader annotations();

        /**
         * @param postFix
         *            the message to include in the final message
         * 
         * @throws InvalidBeanDefinitionException
         *             always thrown
         */
        default void failWith(String postFix) {
            throw new InvalidBeanDefinitionException("OOPS " + postFix);
        }

        /**
         * @return
         * @see AnnotatedMethodHook#allowInvoke()
         */
        boolean hasInvokeAccess();

        /** {@return the underlying method.} */
        Method method();

        /**
         * Attempts to convert the annotated return type of the method to a {@link Key}, or fails by throwing
         * {@link InvalidBeanDefinitionException} if the annotated return type does not represent a valid key.
         * <p>
         * This method will not attempt to peel away injection wrapper types such as {@link Optional} before constructing the
         * key.
         * 
         * @return a key representing the return type of the method
         * 
         * @throws InvalidKeyException
         *             if the return type of the method does not represent a proper key
         */
        Key<?> methodToKey();

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

        /** {@return the default type of operation that will be created.} */
        OperationType operationType();
    }

    /** A small utility record to hold the both the extension model and the bean in one field. */
    // Replace with Introspector???
    private record Setup(ExtensionDescriptor extension, BeanSetup bean) {}
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
