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
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import app.packed.operation.InvocationType;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.BeanOperationSetup.BeanFieldOperationSetup;
import internal.app.packed.operation.BeanOperationSetup.BeanMethodOperationSetup;
import internal.app.packed.operation.OperationTarget.FieldOperationTarget;
import internal.app.packed.operation.OperationTarget.MethodOperationTarget;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Represents an invokable operation on a bean. */
public abstract sealed class BeanOperationSetup extends OperationSetup permits BeanFieldOperationSetup, BeanMethodOperationSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(BeanOperationSetup)}. */
    private static final MethodHandle MH_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class, "initialize",
            void.class, BeanOperationSetup.class);

    /** The extension that operates the operation. MethodHandles will be generated relative to this. */
    public final ExtensionSetup operator;

    /** The invocation type of the operation. */
    public final InvocationType invocationType;

    /** Whether or not an invoker has been computed */
    boolean isComputed;

    /** Supplies a mirror for the operation */
    Supplier<? extends OperationMirror> mirrorSupplier = OperationMirror::new;

    /** The target of the operation. */
    public final OperationTarget target;

    /** The type of the operation. */
    public final OperationType type;

    public BeanOperationSetup(BeanSetup bean, OperationType type, ExtensionSetup operator, InvocationType invocationType, OperationTarget target) {
        super(bean, type.parameterCount());
        this.type = type;
        this.target = target;
        this.invocationType = requireNonNull(invocationType, "invocationType is null");
        this.operator = operator;
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
            MH_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public static final class BeanMethodOperationSetup extends BeanOperationSetup {

        public final Method method;

        public final MethodHandle methodHandle;

        /**
         * @param bean
         * @param type
         * @param operator
         * @param invocationType
         * @param target
         */
        public BeanMethodOperationSetup(BeanSetup bean, ExtensionSetup operator, OperationType operationType, InvocationType invocationType, Method method,
                MethodHandle methodHandle) {
            super(bean, operationType, operator, invocationType, new MethodOperationTarget(methodHandle, method));
            this.method = method;
            this.methodHandle = methodHandle;
        }
    }

    public static final class BeanFieldOperationSetup extends BeanOperationSetup {

        public final Field field;

        public final AccessMode accessMode;

        public final MethodHandle methodHandle;

        /**
         * @param bean
         * @param type
         * @param operator
         * @param invocationType
         * @param target
         */
        public BeanFieldOperationSetup(BeanSetup bean, ExtensionSetup operator, InvocationType invocationType, Field field, AccessMode accessMode,
                MethodHandle methodHandle) {
            super(bean, OperationType.ofFieldAccess(field, accessMode), operator, invocationType, new FieldOperationTarget(methodHandle, field, accessMode));
            this.field = field;
            this.accessMode = accessMode;
            this.methodHandle = methodHandle;
        }
    }
}
