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
package packed.internal.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.container.InternalExtensionException;
import app.packed.operation.OperationBuilder;
import app.packed.operation.mirror.OperationMirror;
import packed.internal.base.PackedVariable;
import packed.internal.bean.BeanSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.inject.DependencyNode;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** Build-time configuration of an operation. */
public class OperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_OPERATION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class,
            "initialize", void.class, OperationSetup.class);

    /** The bean the operation is a part of. */
    public final BeanSetup bean;

    public DependencyNode depNode;

    /** Supplies a mirror for the operation */
    private Supplier<? extends OperationMirror> mirrorSupplier = OperationMirror::new;

    /** The operation's target. */
    public final PackedOperationTarget operationTarget;

    /** The operator of the operation. */
    public final ExtensionSetup operator;

    public OperationSetup(BeanSetup bean, PackedOperationTarget target, ExtensionSetup operator) {
        this.bean = requireNonNull(bean);
        this.operationTarget = requireNonNull(target);
        this.operator = requireNonNull(operator);
        
        bean.addOperation(this); // add operation
    }

    /** {@return a mirror for the operation.} */
    public OperationMirror mirror() {
        OperationMirror mirror = mirrorSupplier.get();
        if (mirror == null) {
            throw new InternalExtensionException(operator.extensionType + " supplied a null operation mirror");
        }

        // Initialize OperationMirror by calling OperationMirror#initialize(OperationSetup)
        try {
            MH_OPERATION_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public OperationBuilder specializeMirror(Supplier<? extends OperationMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        this.mirrorSupplier = supplier;
        return (OperationBuilder) this;
    }

    public PackedVariable variable(int index) {
        throw new UnsupportedOperationException();
    }
}
