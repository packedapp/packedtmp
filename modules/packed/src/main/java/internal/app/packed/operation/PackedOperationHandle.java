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
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.BeanIntrospector$OnBindingHook;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTargetMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.ExtensionBeanSetup;

/** Implementation of {@link OperationHandle}. */
public final class PackedOperationHandle implements OperationHandle {

    /** The bean the operation is a part of. */
    final BeanSetup bean;

    private final InvocationType invocationType;

    /** Whether or not an invoker has been computed */
    boolean isComputed;

    /** Supplies a mirror for the operation */
    Supplier<? extends OperationMirror> mirrorSupplier = OperationMirror::new;

    /** The bean that invokes the operation. */
    final ExtensionBeanSetup operatorBean;

    /** The target of the operation. Typically a bean member, a function or a plain MethodHandle. */
    final PackedOperationTarget target;

    final OperationType type;

    public final Wrapper wrapper;

    public PackedOperationHandle(OperationType type, ExtensionBeanConfiguration<?> operator, InvocationType invocationType, BeanSetup bean,
            PackedOperationTarget target, Wrapper wrapper) {
        this.type = type;
        this.bean = bean;
        this.target = target;
        this.wrapper = wrapper;
        this.invocationType = requireNonNull(invocationType, "invocationType is null");
        requireNonNull(operator, "operator is null");
        this.operatorBean = ExtensionBeanSetup.crack(operator);
    }

    /** {@inheritDoc} */
    @Override
    public List<BeanIntrospector$OnBindingHook> bindings() {
        throw new UnsupportedOperationException();
    }

    public void build() {
        OperationSetup os = new OperationSetup(this);
        bean.addOperation(os);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle buildInvoker() {
        if (isComputed) {
            throw new IllegalStateException("This method can only be called once");
        }
        isComputed = true;
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType invocationType() {
        return invocationType;
    }

    /** {@inheritDoc} */
    public OperationHandle specializeMirror(Supplier<? extends OperationMirror> supplier) {
        if (isComputed) {
            throw new IllegalStateException("Cannot set a mirror after an invoker has been computed");
        }
        this.mirrorSupplier = requireNonNull(supplier, "supplier is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public OperationType type() {
        return type;
    }

    public static non-sealed abstract class Wrapper implements OperationTargetMirror {

        final boolean isStatic;

        final MethodHandle methodHandle;

        Wrapper(MethodHandle methodHandle, boolean isStatic) {
            this.methodHandle = methodHandle;
            this.isStatic = isStatic;
        }

        public static final class FieldWrapper extends Wrapper implements OperationTargetMirror.OfFieldAccess {

            private final AccessMode accessMode;

            private final Field field;

            /**
             * @param methodHandle
             * @param isStatic
             */
            public FieldWrapper(MethodHandle methodHandle, Field field, AccessMode accessMode) {
                super(methodHandle, Modifier.isStatic(field.getModifiers()));
                this.field = field;
                this.accessMode = accessMode;
            }

            /** {@inheritDoc} */
            @Override
            public AccessMode accessMode() {
                return accessMode;
            }

            /** {@inheritDoc} */
            @Override
            public boolean allowGet() {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public boolean allowSet() {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public Field field() {
                return field;
            }

        }

        public static final class MethodWrapper extends Wrapper implements OperationTargetMirror.OfMethodInvoke {

            private final Method method;

            /**
             * @param methodHandle
             * @param isStatic
             */
            public MethodWrapper(MethodHandle methodHandle, Method method) {
                super(methodHandle, Modifier.isStatic(method.getModifiers()));
                this.method = method;
            }

            /** {@return the invokable method.} */
            public Method method() {
                return method;
            }
        }
    }
}
