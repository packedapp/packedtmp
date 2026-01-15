/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import app.packed.bean.BeanIntrospector;
import app.packed.component.ComponentHandle;
import app.packed.component.ComponentPath;
import app.packed.component.Sidehandle;
import app.packed.extension.Extension;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.component.PackedComponentState;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationInstaller;
import internal.app.packed.util.accesshelper.AccessHelper;
import internal.app.packed.util.accesshelper.OperationAccessHandler;

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
/// FooBean.init() TooBean.init1() TooBean.init12()

//Embedded (yes/no)
//Top (yes/no)
//Top does not have a parent
//Top Yes and

//TopO
//Non-Top
//Embedded
// interceptor().add(...);
// interceptor().peek(e->IO.println(e));
public non-sealed class OperationHandle<C extends OperationConfiguration> extends ComponentHandle {

    /** The lazy generated operation configuration. */
    private final Supplier<C> configuration = StableValue.supplier(() -> newOperationConfiguration());

    /** The lazy generated operation mirror. */
    private final Supplier<OperationMirror> mirror = StableValue.supplier(() -> newOperationMirror());

    /** The internal operation configuration. */
    final OperationSetup operation;

    /** The state of this handle. */
    private PackedComponentState state = PackedComponentState.CONFIGURABLE;

    /**
     * Creates a new operation handle.
     *
     * @param installer
     *            the installer for the operation
     */
    public OperationHandle(OperationInstaller installer) {
        this.operation = ((PackedOperationInstaller) installer).toSetup();
    }

    /**
     * Calling this method will create a {@link BindingKind#MANUAL manual} binding for the parameter with the specified
     * index.
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
    public final BeanIntrospector.OnVariable bindable(int index) {
        checkIndex(index, operation.type.parameterCount());

        if (operation.bean.scanner == null) {
            throw new UnsupportedOperationException();
        }

        // This method does not throw IllegalStateExtension, but OnBinding may.
        // custom invocationContext must have been set before calling this method

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
        checkNotFinalized();
        operation.bean.container.application.componentTags.addComponentTags(operation, tags);
    }

    /** {@return the user exposed configuration of this operation} */
    public final C configuration() {
        return configuration.get();
    }

    /** {@return the operator of the operation.} */
    public final Class<? extends Extension<?>> installedByExtension() {
        return operation.installedByExtension.extensionType;
    }

    /**
     * Returns the signature of the underlying operation that the invoker must match.
     * <p>
     * This method returns the signature of the underlying operation that the invoker can execute, which describes the
     * parameter types and return type required for invocation. This information can be used to determine if the operation
     * is compatible with a given functional interface or to create compatible method handles.
     * <p>
     * The method handle returned by {@link #invokerAsMethodHandle()} will have this type as its {@link MethodHandle#type()
     * method handle type}.
     *
     * @return the method type signature of the underlying operation
     */
    public final MethodType invokerType() {
        return operation.template.invocationType();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurable() {
        return state == PackedComponentState.CONFIGURABLE;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isOpen() {
        return state != PackedComponentState.FINALIZED;
    }

    /** {@inheritDoc} */
    @Override
    public final OperationMirror mirror() {
        return mirror.get();
    }

    // Ogsaa en template ting taenker jeg? IDK
    /** {@inheritDoc} */
    public final void named(String name) {
        requireNonNull(name, "name is null");
        checkNotFinalized();
        operation.namePrefix = name;
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
     * This method should never be called directly, only through {@link #mirror}.
     * <p>
     * This method will never be called more than once by the framework for each operation.
     */
    protected OperationMirror newOperationMirror() {
        return new OperationMirror(this);
    }

    public final Sidehandle sidehandle() {
        Sidehandle sidehandle = operation.sidehandle;
        if (sidehandle == null) {
            throw new UnsupportedOperationException("Operation has not been attached to a sidehandle");
        }
        return sidehandle;
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
    // Okay we need to clean this us.
    // There is whenTheUseCannotCallTheOperation
    //// This is here where you want to tell the user, that oops, you never configured the X

    // Maybe we need both??? We have it for Extension, onAssemblyClose, onApplicationClose
    // As a okay you cannot do anything else
    protected void onFinalized() {}

    protected void onInstall() {}

    /**
     * The owner on the bean on which the operation is located can no longer configure it.
     */
    protected void onConfigured() {}

    /**
     * Called by the framework to mark that the bean is no longer configurable.
     *
     * @see internal.app.packed.util.handlers.BeanHandlers#invokeBeanHandleDoClose(BeanHandle)
     */
    /* package private */ final void onStateChange(boolean isClose) {
        // Logic in BeanHandle, only calls this method once (with isClose=true)
        // sometimes we run both onConfigured() and onClose();
        if (state == PackedComponentState.CONFIGURABLE) {
            state = PackedComponentState.FINALIZING;
            onConfigured();
        }

        if (isClose) {
            onFinalized();
            state = PackedComponentState.FINALIZED;
        }
    }

    /** {@return the target of this operation} */
    public final OperationTarget target() {
        return operation.target.target();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return operation.toString();
    }

    /** {@return the type of this operation} */
    public final OperationType type() {
        return operation.type;
    }

    static {
        AccessHelper.initHandler(OperationAccessHandler.class, new OperationAccessHandler() {

            @Override
            public OperationSetup getOperationHandleOperation(OperationHandle<?> handle) {
                return handle.operation;
            }

            @Override
            public void invokeOperationHandleDoClose(OperationHandle<?> handle, boolean isClose) {
                handle.onStateChange(isClose);
            }

            @Override
            public void onInstall(OperationHandle<?> handle) {
                handle.onInstall();
            }
        });
    }
}
