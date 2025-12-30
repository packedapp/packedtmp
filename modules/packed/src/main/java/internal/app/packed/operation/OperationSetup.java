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
package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.bean.sidebean.SidebeanAttachment;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.context.Context;
import app.packed.extension.InternalExtensionException;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.ValueBased;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.sidebean.PackedSidebeanAttachment;
import internal.app.packed.bean.sidebean.SidebeanHandle;
import internal.app.packed.binding.BindingProvider.FromEmbeddedOperation;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.context.ContextInfo;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.context.ContextualizedComponentSetup;
import internal.app.packed.context.PackedContextTemplate;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.invoke.OperationCodeGenerator;
import internal.app.packed.lifecycle.lifetime.entrypoint.EntryPointSetup;
import internal.app.packed.namespace.NamespaceSetup;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.service.ServiceProviderSetup;
import internal.app.packed.service.ServiceProviderSetup.NamespaceServiceProviderHandle;
import internal.app.packed.service.ServiceProviderSetup.OperationServiceProviderSetup;
import internal.app.packed.service.util.ServiceMap;
import internal.app.packed.util.accesshelper.OperationAccessHandler;

/** The internal configuration of an operation on a bean. */
public final class OperationSetup implements ContextualizedComponentSetup, ComponentSetup {

    /** The bean this operation belongs to. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

    /** Declared contexts on the operation. Is HashMap because it uses less memory than IHM when empty. */
    public final HashMap<Class<? extends Context<?>>, ContextSetup> contexts = new HashMap<>();

    /** Any operation this operation is embedded into. */
    @Nullable
    public final EmbeddedIntoOperation embeddedInto;

    @Nullable
    public final EntryPointSetup entryPoint;

    /** The operation's handle. */
    @Nullable
    private OperationHandle<?> handle;

    public final OperationCodeGenerator codeHolder;

    /** The operator of the operation. */
    public final ExtensionSetup installedByExtension;

    static final AtomicInteger ID = new AtomicInteger();
    public final int id = ID.incrementAndGet();

    /**
     * The name prefix of the operation.
     * <p>
     * Multiple operations can have the same
     */
    // Could also have a calculated name
    // Record Name(int state, String name)
    // state = 0 = generated, Integer.max = final, anywhere in between the number of
    // operations in the bean when the name was calculated
    // Det hele er vi ikke gider calculate et navn, med mindre det skal bruges
    // Ved method overloading kan vi risikere at 2 operationer med samme navn
    // Hvilket ikke fungere hvis vi vil have component path operationer
    public String namePrefix; // name = operator.simpleName + "Operation"

    /** ServiceProviders bound specifically for the operation. */
    public final ServiceMap<OperationServiceProviderSetup> serviceProviders = new ServiceMap<>();

    /** The target of the operation (field, method, methodhandle, ...) */
    public final PackedOperationTarget target;

    /** The template that was used to create the operation. */
    public final PackedOperationTemplate template;

    /** The type of this operation. */
    public final OperationType type;

    public SidebeanAttachment attachment;

    /**
     * Create a new operation.
     *
     * @param installer
     *            the operation's installer
     */
    private OperationSetup(PackedOperationInstaller installer) {
        this.installedByExtension = requireNonNull(installer.operator);
        this.target = requireNonNull(installer.operationTarget);
        this.bean = requireNonNull(installer.bean);
        this.type = installer.operationType;
        this.embeddedInto = installer.embeddedInto;
        this.bindings = new BindingSetup[type.parameterCount()];
        this.template = installer.template;
        this.namePrefix = installer.namePrefix; // temporarty
        if (template.newLifetime()) {
            this.entryPoint = new EntryPointSetup(this, bean.lifetime);
        } else {
            this.entryPoint = null;
        }
        for (PackedContextTemplate ct : template.contexts) {
            contexts.put(ct.contextClass(), new ContextSetup(ct, this));
        }
        // check is built
        this.codeHolder = new OperationCodeGenerator(this, null);
        if (installer.attachToSidebean != null) {
            SidebeanHandle<?> handle2 = (SidebeanHandle<?>) installer.attachToSidebean.handle();
            this.attachment = handle2.attachTo(new PackedSidebeanAttachment.OfOperation(installer.attachToSidebean, this));
        } else {
            this.attachment = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return ComponentKind.OPERATION.pathNew(bean.componentPath(), name());
    }

    public Set<BeanSetup> dependsOn() {
        HashSet<BeanSetup> result = new HashSet<>();
        // TODO hmm, skal inkludere extensions??? nope
        forEachBinding(b -> {
            if (b instanceof ServiceBindingSetup s) {
                ServiceProviderSetup provider = s.resolvedProvider;
                if (provider instanceof NamespaceServiceProviderHandle p) {
                    result.add(p.operation().bean);
                }
            }
        });
        return result;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public ContextSetup findContext(Class<? extends Context<?>> contextClass) {
        Class<? extends Context<?>> cl = ContextInfo.normalize(contextClass);
        ContextSetup cs = contexts.get(cl);
        if (cs != null) {
            return cs;
        }
        return bean.findContext(cl);
    }

    // readOnly. Will not work if for example, resolving a binding
    public void forEachBinding(Consumer<? super BindingSetup> binding) {
        for (BindingSetup bs : bindings) {
            requireNonNull(bs);
            if (bs.provider() != null && bs.provider() instanceof FromEmbeddedOperation nested) {
                nested.operation().forEachBinding(binding);
            }
            binding.accept(bs);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void forEachContext(Consumer<? super ContextSetup> action) {
        contexts.values().forEach(action);
        bean.forEachContext(action);
    }

    /** {@return the operation's handle} */
    @Override
    public OperationHandle<?> handle() {
        OperationHandle<?> h = handle;
        if (h != null) {
            return h;
        }
        throw new IllegalStateException();
    }

    /** {@return the operation's mirror.} */
    @Override
    public OperationMirror mirror() {
        return handle().mirror();
    }

    /** {@return the name of the operation} */
    public String name() {
        return bean.operations.operationNames().get(this);
    }

    public String namePrefix() {
        return namePrefix;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return componentPath().toString();
    }

    /**
     * Extracts an operation setup from an operation handle.
     *
     * @param handle
     *            the handle to extract from
     * @return the operation setup
     */
    public static OperationSetup crack(OperationHandle<?> handle) {
        requireNonNull(handle);
        return OperationAccessHandler.instance().getOperationHandleOperation(handle);
    }

    /**
     * Create a new operation using the specified installer and handle factory.
     *
     * @param installer
     *            the installer for the operation
     * @param handleFactory
     *            a function responsible for creating the operation's handle
     * @return the new operation
     * @throws InternalExtensionException
     *             if the handleFactory returns null
     */
    static OperationSetup newOperation(PackedOperationInstaller installer, Function<? super OperationInstaller, OperationHandle<?>> handleFactory) {
        // Cannot reuse an installer
        installer.checkNotUsed();

        // Create the new operation, and set it on the installer. This must be done in order to create the handle in next step.
        OperationSetup operation = installer.install(new OperationSetup(installer));

        // Add the operation to the bean
        installer.bean.operations.add(operation);

        // Add the operation to any requested namespace
        if (installer.addToNamespace != null) {
            NamespaceSetup ns = NamespaceSetup.crack(installer.addToNamespace);
            ns.operations.add(operation);
        }

        // Create the operation's handle.
        OperationHandle<?> handle = operation.handle = handleFactory.apply(installer);
        if (handle == null) {
            throw new InternalExtensionException(installer.operator.extensionType, handleFactory + " returned null, when creating a new OperationHandle");
        }

        OperationAccessHandler.instance().onInstall(handle);

        return operation;
    }

    /** The parent of a nested operation. */
    @ValueBased
    public record EmbeddedIntoOperation(OperationSetup operation, int bindingIndex) {}
}
