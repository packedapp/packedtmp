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
import java.util.function.Supplier;

import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTargetMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.ExtensionBeanSetup;
import internal.app.packed.bean.IntrospectorOnField.FieldOperationTarget;
import internal.app.packed.bean.IntrospectorOnMethod.MethodOperationTarget;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Build-time configuration of an operation. */
public final class OperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_OPERATION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class,
            "initialize", void.class, OperationSetup.class);

    /** The bean that defines the operation. */
    public final BeanSetup bean;

    /** Any binding for the operation. {@code null} represents not yet bound */
    public final BindingSetup[] bindings;

    /** The invocation type of the operation. */
    public final InvocationType invocationType;

    /** Whether or not an invoker has been computed */
    boolean isComputed;

    /** Supplies a mirror for the operation */
    Supplier<? extends OperationMirror> mirrorSupplier = OperationMirror::new;

    /** The extension that operates the operation. */
    public final ExtensionBeanSetup operatorBean;

    /** The target of the operation. */
    public final OperationTarget target;

    /** The type of the operation. */
    public final OperationType type;

    public OperationSetup(BeanSetup bean, OperationType type, ExtensionBeanConfiguration<?> operator, InvocationType invocationType, OperationTarget target) {
        this.bean = bean;
        this.type = type;
        this.target = target;
        this.invocationType = requireNonNull(invocationType, "invocationType is null");
        requireNonNull(operator, "operator is null");
        this.operatorBean = ExtensionBeanSetup.crack(operator);
        this.bindings = new BindingSetup[type.parameterCount()];
    }

    /** {@return a new mirror.} */
    public OperationMirror mirror() {
        // Create a new OperationMirror
        OperationMirror mirror = mirrorSupplier.get();
        if (mirror == null) {
            throw new NullPointerException(mirrorSupplier + " returned a null instead of an " + OperationMirror.class.getSimpleName() + " instance");
        }

        // Initialize OperationMirror by calling OperationMirror#initialize(OperationSetup)
        try {
            MH_OPERATION_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public static sealed abstract class OperationTarget implements OperationTargetMirror permits FieldOperationTarget, MethodOperationTarget {

        final boolean isStatic;

        final MethodHandle methodHandle;

        protected OperationTarget(MethodHandle methodHandle, boolean isStatic) {
            this.methodHandle = methodHandle;
            this.isStatic = isStatic;
        }
    }
}
