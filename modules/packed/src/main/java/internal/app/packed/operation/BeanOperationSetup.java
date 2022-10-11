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
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import app.packed.operation.InvocationType;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.BeanOperationSetup.BeanFieldAccessSetup;
import internal.app.packed.operation.BeanOperationSetup.BeanInstanceAccessSetup;
import internal.app.packed.operation.BeanOperationSetup.BeanMethodInvokeSetup;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** Represents an operation on a bean. */
public abstract sealed class BeanOperationSetup extends OperationSetup permits BeanFieldAccessSetup, BeanMethodInvokeSetup, BeanInstanceAccessSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(BeanOperationSetup)}. */
    private static final MethodHandle MH_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OperationMirror.class, "initialize",
            void.class, BeanOperationSetup.class);

    /** The invocation type of the operation. */
    public final InvocationType invocationType;

    /** Whether or not an invoker has been computed */
    boolean isComputed;

    /** Supplies a mirror for the operation */
    Supplier<? extends OperationMirror> mirrorSupplier = OperationMirror::new;

    /** The extension that operates the operation. MethodHandles will be generated relative to this. */
    public final ExtensionSetup operator;

    /** The type of the operation. */
    public final OperationType type;

    public abstract MethodHandle methodHandle();

    public abstract boolean isStatic();

    public BeanOperationSetup(BeanSetup bean, OperationType type, ExtensionSetup operator, InvocationType invocationType) {
        super(bean, type);
        this.type = type;
        this.invocationType = invocationType;
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

    public static final class BeanInstanceAccessSetup extends BeanOperationSetup {

        /**
         * @param bean
         * @param type
         * @param operator
         * @param invocationType
         */
        public BeanInstanceAccessSetup(BeanSetup bean) {
            super(bean, OperationType.of(bean.beanClass()), null, null);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle methodHandle() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStatic() {
            return false;
        }

    }

    /** Represents a field access on a bean */
    public static final class BeanFieldAccessSetup extends BeanOperationSetup {

        /** The access mode. */
        public final AccessMode accessMode;

        /** The field that is accessed. */
        public final Field field;

        /** A direct method handle for the field and accessMode. */
        public final MethodHandle methodHandle;

        /**
         * @param bean
         *            the bean where the field is located
         * @param operator
         *            the extension where the operating bean that will access the field is located
         * @param invocationType
         *            the invocation type that the operating bean will use
         * @param field
         *            the field
         * @param accessMode
         *            the access mode
         * @param methodHandle
         *            a method handle for accessing the field
         */
        public BeanFieldAccessSetup(BeanSetup bean, ExtensionSetup operator, InvocationType invocationType, Field field, AccessMode accessMode,
                MethodHandle methodHandle) {
            super(bean, OperationType.ofFieldAccess(field, accessMode), operator, invocationType);
            this.field = field;
            this.accessMode = accessMode;
            this.methodHandle = methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle methodHandle() {
            return methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStatic() {
            return Modifier.isStatic(field.getModifiers());
        }
    }

    public static final class BeanMethodInvokeSetup extends BeanOperationSetup {

        public final Method method;

        public final MethodHandle methodHandle;

        /**
         * @param bean
         * @param type
         * @param operator
         * @param invocationType
         * @param target
         */
        public BeanMethodInvokeSetup(BeanSetup bean, ExtensionSetup operator, OperationType operationType, InvocationType invocationType, Method method,
                MethodHandle methodHandle) {
            super(bean, operationType, operator, invocationType);
            this.method = method;
            this.methodHandle = methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle methodHandle() {
            return methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public boolean isStatic() {
            return Modifier.isStatic(method.getModifiers());
        }
    }
}
