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
package internal.app.packed.operation.binding;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.function.Supplier;

import app.packed.container.User;
import app.packed.operation.BindingMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The configuration of a single binding in an operation. */

// Vi vil godt gemme noget om 
// Hvorfor er denne binding blevet lavet... Vi saa det var en composite, Vi resolved den som en service, den var manuelt bounded

// CompositeBinding (Always op)
// OpBinding (Always constant)
// @Anno/ Class Hook BindingOp
//// Constant
//// Non
// Service/ExtensionService (Always opish)


// Constant vs Op vs InvocationArgument

public abstract class BindingSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_BINDING_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BindingMirror.class, "initialize",
            void.class, BindingSetup.class);

    public final User boundBy;

    /** The index into {@link OperationSetup#bindings}. */
    public final int index;

    /** Supplies a mirror for the operation */
    public Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation this binding is a part of. */
    public final OperationSetup operation;

    /** Supplies a mirror for the operation */
    public final BindingOrigin target;

    public BindingSetup(OperationSetup operation, int index, User user, BindingOrigin target) {
        this.operation = operation;
        this.index = index;
        this.target = target;
        this.boundBy = user;
    }

    public abstract MethodHandle bindIntoOperation(MethodHandle methodHandle);

    /** {@return a new mirror.} */
    public BindingMirror mirror() {
        BindingMirror mirror = ClassUtil.mirrorHelper(BindingMirror.class, BindingMirror::new, mirrorSupplier);

        // Initialize BindingMirror by calling BindingMirror#initialize(BindingSetup)
        try {
            MH_BINDING_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }
}
