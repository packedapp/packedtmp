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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.application.ApplicationMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.ExtensionBeanSetup;
import internal.app.packed.inject.DependencyNode;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Build-time configuration of an operation. */
public final class OperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_OPERATION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class,
            "initialize", void.class, OperationSetup.class);

    /** The bean that defines the operation. */
    public final BeanSetup bean;

    public DependencyNode depNode;

    /** Supplies a mirror for the operation */
    private final Supplier<? extends OperationMirror> mirrorSupplier;

    /** The target of the operation. */
    public final PackedOperationTarget operationTarget;

    /** The operator of the operation. */
    public final ExtensionBeanSetup operatorBean;
    
    OperationSetup(PackedOperationBuilder builder) {
        this.bean = builder.bean;
        this.operationTarget = builder.target;
        this.operatorBean = builder.operatorBean;
        this.mirrorSupplier = builder.mirrorSupplier;
    }

    /** {@return a new mirror.} */
    public OperationMirror mirror() {
        // Create a new OperationMirror
        OperationMirror mirror = mirrorSupplier.get();
        if (mirror == null) {
            throw new NullPointerException(mirrorSupplier + " returned a null instead of an " + ApplicationMirror.class.getSimpleName() + " instance");
        }

        // Initialize OperationMirror by calling OperationMirror#initialize(OperationSetup)
        try {
            MH_OPERATION_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }
}
