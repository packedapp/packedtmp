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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.binding.BindingSetup;
import internal.app.packed.operation.binding.NestedBindingSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Represents an operation on a bean. */
public final class OperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class, "initialize",
            void.class, OperationSetup.class);

    /** A MethodHandle for creating a new handle {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_NEW_OPERATION_HANDLE = LookupUtil.lookupConstructorPrivate(MethodHandles.lookup(), OperationHandle.class,
            OperationSetup.class);

    /** An empty array of {@code BindingSetup}. */
    private static final BindingSetup[] NO_BINDINGS = new BindingSetup[0];

    /** The bean this operation belongs to. */
    public final BeanSetup bean;

    /** Bindings for this operation. */
    public final BindingSetup[] bindings;

    /** By who and how this operation is invoked */
    public final InvocationSite invocationSite;

    /** Whether or not an invoker has been computed */
    public boolean isComputed;

    /** Supplies a mirror for the operation */
    public Supplier<? extends OperationMirror> mirrorSupplier;

    /** Any nested binding this operation is a part of. */
    @Nullable
    public final NestedBindingSetup nestedBinding;

    /** The target of the operation. */
    public final OperationTarget target;

    /** The type of the operation. */
    public final OperationType type;

    public OperationSetup(BeanSetup bean, OperationType type, ExtensionSetup operator, InvocationSite invocationSite, OperationTarget operationTarget,
            @Nullable NestedBindingSetup nestedBinding) {
        this.bean = requireNonNull(bean);
        this.type = requireNonNull(type);
        this.invocationSite = requireNonNull(invocationSite);
        this.target = requireNonNull(operationTarget);
        this.nestedBinding = nestedBinding;
        this.bindings = type.parameterCount() == 0 ? NO_BINDINGS : new BindingSetup[type.parameterCount()];
    }

    public MethodHandle buildInvoker() {
        // Hav en version der tager en ExtensionBeanConfiguration eller bring back ExtensionContext

        if (isComputed) {
            throw new IllegalStateException("This method can only be called once");
        }

        isComputed = true;
        // application.checkIsComputable
        throw new UnsupportedOperationException();
    }

    public OperationHandle toHandle() {
        try {
            return (OperationHandle) MH_NEW_OPERATION_HANDLE.invokeExact(this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /**
     * 
     */
    public void codegen() {

    }

    // readOnly. Will not work if for example, resolving a binding
    public void forEachBinding(Consumer<? super BindingSetup> binding) {
        for (BindingSetup bs : bindings) {
            if (bs instanceof NestedBindingSetup nested) {
                nested.operation.forEachBinding(binding);
            }
            binding.accept(bs);
        }
    }

    /** {@return a new mirror.} */
    public OperationMirror mirror() {
        OperationMirror mirror = ClassUtil.mirrorHelper(OperationMirror.class, OperationMirror::new, mirrorSupplier);

        // Initialize OperationMirror by calling OperationMirror#initialize(OperationSetup)
        try {
            MH_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }
}
