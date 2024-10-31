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
package app.packed.operation;

import static java.util.Objects.checkIndex;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.function.Function;

import app.packed.bean.scanning.BeanIntrospector.OnVariable;
import app.packed.component.ComponentHandle;
import app.packed.component.ComponentPath;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionContext;
import app.packed.util.Nullable;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.operation.OperationCodeGenerator;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationInstaller;
import internal.app.packed.util.MethodHandleUtil;
import internal.app.packed.util.MethodHandleUtil.LazyResolable;

/**
 * A container handle is a build-time reference to an installed container. They are created by the framework when an
 * extension installs a container on behalf of the user (or an another extension).
 * <p>
 * An operation handle is direct reference to an underlying method, constructor, field, or similar low-level operation.
 * <p>
 * Operation handles are the main way in which the framework supports such as annotations on fields and methods.
 * <p>
 * An operation handle can be constructed by an {@link Extension extension} in any of the following ways:
 * <ul>
 * <li>By calling {@link OperationalMethod#newOperation} to create a new operation that can {@code invoke} the
 * underlying {@link Method}.
 * <li>By calling {@link OperationalField#newGetOperation} to create a new operation that can {@code get} the value of
 * the underlying {@link Field}.
 * <li>By calling {@link OperationalField#newSetOperation} to create a new operation that car {@code set} the value of
 * the underlying {@link Field}.
 * <li>By calling {@link OperationalField#newOperation(java.lang.invoke.VarHandle.AccessMode)} to create a new operation
 * that can {@code access} the underlying {@link Field}.
 * </ul>
 *
 *
 * This class is used to configure a operation.
 *
 * This class contains a number of configuration methods:
 *
 * List them
 *
 *
 *
 * This class currently supports:
 * <ul>
 * <li>Creating method handles that can invoke the underlying operation.</li>
 * <li>Special processing of dependencies.</li>
 * <li>Specialize the mirror that is exposed for the operation.</li>
 * </ul>
 * <p>
 * This class must support:
 * <ul>
 * <li>Customization of the argument side (lanes). For example, take a SchedulingContext as argument X.</li>
 * <li>Name the operation (not implemented yet).</li>
 * </ul>
 * <p>
 *
 * @see OperationalMethod#newOperation()
 * @see OperationalField#newGetOperation()
 * @see OperationalField#newSetOperation()
 * @see OperationalField#newOperation(java.lang.invoke.VarHandle.AccessMode)
 */
//AllInit
/// FooBean.init()
/// TooBean.init1()
/// TooBean.init12()

//Embedded (yes/no)
//Top (yes/no)
//Top does not have a parent
//Top Yes and

//Top
//Non-Top
//Embedded
// interceptor().add(...);
// interceptor().peek(e->System.out.println(e));
public non-sealed class OperationHandle<C extends OperationConfiguration> extends ComponentHandle implements InvokerFactory {

    /** The lazy generated operation configuration. */
    private C configuration;

    /** The generated method handle. */
    @Nullable
    private MethodHandle generatedMethodHandle;

    @Nullable
    private LazyResolable lmh;

    /** The lazy generated operation mirror. */
    private OperationMirror mirror;

    /** The internal operation configuration. */
    final OperationSetup operation;

    /**
     * Creates a new operation handle.
     *
     * @param installer
     *            the installer for the operation
     */
    public OperationHandle(OperationInstaller installer) {
        this.operation = ((PackedOperationInstaller) installer).toSetup();
    }

    protected MethodHandle adaptMethodHandle(MethodHandle mh) {
        return mh;
    }

    /**
     * This will create a {@link BindingKind#MANUAL manual} binding for the parameter with the specified index.
     * <p>
     * The {@link BindableVariable} must be bound at some point before the assembly closes. Otherwise a BuildException is
     * thrown.
     * <p>
     * This operation is no longer configurable when this method returns.
     * <p>
     * The will report a {@link BindingKind#MANUAL} as binding classifier
     *
     * @param parameterIndex
     *            the index of the parameter to bind
     * @return a bindable variable
     * @throws IndexOutOfBoundsException
     *             if the parameter index is out of bounds
     * @throws UnsupportedOperationException
     *             if type of operation does not support manually binding. For example, a lifecycle operation. Eller er det
     *             i virkeligheden mere at ejeren af operationen er anderledes end den der kalder. Problemet er fx.
     *             BEan.lifetimeOperations som returnere OperationHandle. Men de er der kun fordi man skal kalde generateX.
     *             Maaske har vi en slags immutable version af operation handle
     * @throws IllegalStateException
     *             this method must be called before the runtime starts resolving parameters. It is best to call this
     *             immediately after having created the operation. The actual binding can be done at a laver point\
     * @see BindingKind#MANUAL
     */
    // Tror vi force laver (reserves) en binding her.
    // Det er jo kun meningen at man skal binden den hvis man kalder denne metode.
    // parameter virker kun som navn hvis man ikke "reservere" binding.
    // Men binder med det samme
    // reserve
    // bindManually(1).bindInvocationArgument(1);
    // Tror vi force laver (reserves) en binding her.
    // Det er jo kun meningen at man skal binden den hvis man kalder denne metode.
    // Saa maaske skal vi have en Mode i IBB

    // overrideParameter?
    // bindParameter
    // bindManually
    // bind(index).toConstant("Foo");
    // Maybe take an consumer to make sure it is "executed"
    public final OnVariable bindManually(int index) {

        // This method does not throw IllegalStateExtension, but OnBinding may.
        // custom invocationContext must have been set before calling this method
        checkIndex(index, operation.type.parameterCount());
        if (operation.bean.scanner == null) {
            throw new UnsupportedOperationException();
        }
        // TODO we need to check that s is still active
        return new IntrospectorOnVariable(operation.bean.scanner, operation, index, operation.installedByExtension, operation.type.parameter(index));
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return operation.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public final void componentTag(String... tags) {
        checkHandleIsConfigurable();
        operation.bean.container.application.componentTags.addComponentTags(operation, tags);
    }

    /** { @return the user exposed configuration of the operation} */
    public final C configuration() {
        C c = configuration;
        if (c == null) {
            c = configuration = newOperationConfiguration();
        }
        return c;
    }

    /**
     * Called by the framework to mark the bean as no longer be configurable.
     *
     * @see internal.app.packed.util.handlers.BeanHandlers#invokeBeanHandleDoClose(BeanHandle)
     */
    final void doClose() {
        onOperationClose();
        // isConfigurable = false;
    }

    /** {@return the operator of the operation.} */
    public final Class<? extends Extension<?>> installerByExtension() {
        return operation.installedByExtension.extensionType;
    }

    // For those that are "afraid" of method handles. You can specify a SAM interface (Or abstract class with an empty
    // constructor)
    // I actually think this is a lot prettier, you can see the signature
    // Maybe a class value in the template.
    // This will codegen though
    // constructor arguments are for abstract classes only
    @Override
    public final <T> T invokerAs(Class<T> handleClass, Object... constructorArguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final MethodHandle invokerAsMethodHandle() {
        // If we have already created the final method handle return this
        MethodHandle result = generatedMethodHandle;
        if (result != null) {
            return result;
        }

        // If we already created a lazy method handle return this
        LazyResolable l = lmh;
        if (l != null) {
            return l.handle();
        }

        // Check that we are still configurable
        checkHandleIsConfigurable();

        if (operation.bean.container.application.isAssembling()) {
            // Still assembling, we need to create a lazy method handle
            l = lmh = MethodHandleUtil.lazyF(operation.template.methodType, () -> generatedMethodHandle = newMethodHandle());

            // If eager codegen .addToSomeQueue, in case we are static java£
            result = l.handle();
        } else {
            // No longer assembling, lets create the method handle directly
            result = newMethodHandle();
            assert (result.type() == operation.template.methodType);
        }
        return result;
    }

    // Must be a SAM type

    /**
     * {@return the invocation type of this operation.}
     * <p>
     * Method handles generated via {@link #generateMethodHandle()} will always the returned value as their
     * {@link MethodHandle#type() method handle type}.
     *
     * @see OperationTemplate.Descriptor#invocationType()
     */
    @Override
    public final MethodType invokerType() {
        return operation.template.invocationType();
    }

    @Override
    public final VarHandle invokerAsVarHandle() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurationConfigurable() {
        return operation.installedByExtension.isConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isHandleConfigurable() {
        // Hah der er forskel paa handle og configuration
        // Fordi configuration kan tilgaas af brugeren.
        // Det er jo faktisk det samme som for en bean...
        return operation.installedByExtension.isConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public final OperationMirror mirror() {
        OperationMirror m = mirror;
        if (m == null) {
            m = mirror = newOperationMirror();
        }
        return m;
    }

    // Ogsaa en template ting taenker jeg? IDK
    /** {@inheritDoc} */

    public final void named(String name) {
        requireNonNull(name, "name is null");
        checkHandleIsConfigurable();
        operation.namePrefix = name;
    }

    /**
     * Generates a method handle that can be used to invoke the underlying operation. *
     * <p>
     * This method should never be called directly, only through {@link #configuration}
     * <p>
     * This method will be never called more than once for a single operation.
     *
     * <p>
     * This method cannot be called earlier than the code generating phase of the application.
     * <p>
     * The {@link MethodType method type} of the returned method handle must be {@code invocationType()}.
     *
     * @return the generated method handle
     *
     * @throws IllegalStateException
     *             if called before the code generating phase of the application.
     */
    protected MethodHandle newMethodHandle() {
        // Maybe have a check here instead, and specifically mention generateMethodHandle when calling
        // Check only called once
        MethodHandle mh = generatedMethodHandle;
        if (mh == null) {
            mh = generatedMethodHandle = OperationCodeGenerator.newMethodHandle(operation);
        }
        // What if we subclassing fucks it op?
        // It will fuck up the lazy generation

        // I think we maybe have this protected.
        // And methodHandle() will just create a lazy method handle if we assembling.
        assert (mh.type() == operation.template.methodType);
        return mh;
    }

    /**
     * {@return creates a new configuration for the operation}
     * <p>
     * This method should never be called directly, only through {@link #configuration}
     * <p>
     * This method will be never called more than once for a single operation.
     */
    @SuppressWarnings("unchecked")
    protected C newOperationConfiguration() {
        return (C) new OperationConfiguration(this);
    }

    /**
     * {@return creates a new mirror for the operation}
     * <p>
     * This method should never be called directly, only through, {@link #mirror}
     * <p>
     * This method will never be called more than once for a single operation.
     */
    protected OperationMirror newOperationMirror() {
        return new OperationMirror(this);
    }

    /**
     * Called by the framework when the operation is marked as no longer configurable.
     * <p>
     * This handle will be {@link #isConfigurable()} while calling this method, but marked as non configurable immediately
     * after.
     * <p>
     * For beans owned by the user, this method will be called when the owning assembly is closed. For beans owned by an
     * application, this method will be called when the application closes.
     * <p>
     * The framework may call this method for all beans owned by the same authority in any order within the same
     * assembly/application (Ideen er bare at smide dem i en liste)
     */
    protected void onOperationClose() {}

    /** {@return the target of this operation.} */
    public final OperationTarget target() {
        return operation.target.target();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return operation.toString();
    }

    /** {@return the type of this operation.} */
    public final OperationType type() {
        return operation.type;
    }
}

interface Zandbox {

    // raekkefoelgen kender man jo ikke
    // Foer vi har sorteret
    void addChild(OperationHandle<?> h);

    default VarHandle generateVarHandle() {
        throw new UnsupportedOperationException();
    }
}
// Hvad goer vi med annoteringer paa Field/Update???
// Putter paa baade Variable og ReturnType???? Det vil jeg mene

interface ZandboxOH {

    // Must not have a classifier
    //// Will have a MH injected at runtime...
    // public void generateInto(InstanceBeanConfiguration<?> bean) {
    // generateInto(bean, Key.of(MethodHandle.class));
    // }
    //
    // public void generateInto(InstanceBeanConfiguration<?> bean, Key<MethodHandle> key) {
    // // En anden mulighed er at det er bundet i InvocationSite...
    // throw new UnsupportedOperationException();
    // }
    //
    //// Must not have a classifier
    //// Will have a MH injected at runtime...
    //
    // public <T> T generateIntoWithAutoClassifier(InstanceBeanConfiguration<?> bean, Class<T> classifierType) {
    // return generateIntoWithAutoClassifier(bean, Key.of(MethodHandle.class), classifierType);
    // }
    //
    // public <T> T generateIntoWithAutoClassifier(InstanceBeanConfiguration<?> bean, Key<MethodHandle> key, Class<T>
    // classifierType) {
    // throw new UnsupportedOperationException();
    // }
    //
    // public void generateIntoWithClassifier(InstanceBeanConfiguration<?> bean, Key<MethodHandle> key, Object classifier) {
    // // InvocationType must have a classifier with an assignable type
    // }
    //
    // public void generateIntoWithClassifier(InstanceBeanConfiguration<?> bean, Object classifier) {
    // generateIntoWithClassifier(bean, Key.of(MethodHandle.class), classifier);
    // }
    //
//     public void injectMethodHandleArrayInto(InstanceBeanConfiguration<?> bean, Object classifier) {
//         throw new UnsupportedOperationException();
//     }
    //
//     public int injectMethodHandleArrayInto(InstanceBeanConfiguration<?> bean) {
//         requireNonNull(bean, "bean is null");
    //
//         // Hvorfor maa man egentlig ikke det her???
//         // Vi kan jo altid bare lave vores egen og injecte den...
//         if (bean.owner().isApplication() || bean.owner().extension() != operation.operator.extensionType) {
//             throw new IllegalArgumentException("Can only specify a bean that has extension " + operation.operator.extensionType.getSimpleName() + " as owner");
//         }
//         return operation.bean.container.application.codegenHelper.addArray(bean, this);
//     }
    //
//     public <T> void injectMethodHandleMapInto(InstanceBeanConfiguration<?> bean, Class<T> keyType, T key) {
//         // check is type
//         throw new UnsupportedOperationException();
//     }

    default boolean hasBindingsBeenResolved() {
        return false;
    }

    // Hmm, kan jo ikke bare tage en tilfaeldig...
    default void invokeFromAncestor(Extension<?> extension) {}

    default void invokeFromAncestor(ExtensionContext context) {}

    // Can be used to optimize invocation...
    // Very advanced though
    // Ideen er nok at vi har den model hvad der er til raadighed...
    // Hvilke argumenter skal du egentlig bruge...
    boolean isInvocationArgumentUsed(int index);

    default <F, T> OperationHandle<?> mapReturn(Class<F> fromType, Class<T> toType, Function<F, T> function) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

    // Hvad hvis vi vil injecte ting??? Return is always the first parameter I would think
    // Additional parameters will be like any other bindings
    // Will it create an additional operation? I would think so if it needs injection
    default OperationHandle<?> mapReturn(MethodHandle mh) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

    // non void return matching invocation type
    default OperationHandle<?> mapReturn(Op<?> op) {
        // Vi kan fx sige String -> StringReturnWrapper
        throw new UnsupportedOperationException();
    }

    // I think this needs to be first operation...
    // Once we start calling onBuild() which schedules it for the extension its over
    default void operatedBy(Object extensionHandle) {
        // delegateTo, transferTo
        // Maybe the method is on ExtensionPoint.UseSite
        // checkConfigurable();
        // Do we create a new handle, and invalidate this handle?
    }

    default void relativise(Extension<?> extension) {

    }

    // I don't know if have a forceMirrorBaseType??? Basic idea being that if delegate the operation
    // to someone they cannot specialize with a mirror that is not a subtype of the specified type
    // For example, LifetimeOperationMirror.
    // However, the best thing we can do is a runtime exception. As the supplier is lazy
    void requireMirrorSubClassOf(Class<? extends OperationMirror> mclass);

    // /**
//  * If this operation is created from a variable (typically a field), returns its accessMode. Otherwise empty.
//  *
//  * @return
//  */
// default Optional<AccessMode> accessMode() {
//     return Optional.empty();
// }
//    default <T> T computeInvoker(TypeToken<T> invokerType) {
//
//        /// computeInvoker(new TypeToken<Function<Boo, Sddd>);
//        throw new UnsupportedOperationException();
//    }

    default void resolveParameter() {
        // we need the introspected bean...

        // Hmm, I don't like it

        // If we don't call this ourselves. It will be called immediately after
        // onMethod, onField, ect
    }

    ZandboxOH resultAssignableToOrFail(Class<?> clz); // ignores any return value

    // dependencies skal vaere her, fordi de er mutable. Ved ikke om vi skal have 2 klasser.
    // Eller vi bare kan genbruge BeanDependency

    // Resolves all parameters that have not already been resolved via bindParameter
    // Idea was to act on all those that werent resolved

    // boolean isBound(int parameterIndex)

    ZandboxOH resultVoid(); // returnIgnore?

    ZandboxOH resultVoidOrFail(); // fails if non-void with BeanDeclarationException

    // spawn NewBean/NewContainer/NewApplication...
    default void spawnNewBean() {
        // I'm not sure this is needed.
        // It is always only configured on the bean

        // Can't see we will ever need to combi it

        // A new bean will be created. I think we need to configure something when making the bean as well
        // Maybe we need a bean option and call this method for every operation.
        // I don't know can we have methods that can do both
    }

    // If immutable... IDK
    // Fx fra mirrors. Har composite operations ogsaa templates???
    OperationTemplate template();
}
