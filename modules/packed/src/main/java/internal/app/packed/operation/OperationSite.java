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
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import app.packed.framework.Nullable;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationSiteMirror;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;

/**
 * The target of an operation.
 */
public sealed abstract class OperationSite implements OperationSiteMirror {

    /** The bean the operation is part of. */
    public final BeanSetup bean;

    /** A method handle for the operation. May be lazily created in the future. */
    public final MethodHandle methodHandle;

    /** The type of operation site. */
    public final OperationType type;

    protected OperationSite(BeanSetup bean, OperationType operationType, MethodHandle methodHandle) {
        this.bean = requireNonNull(bean);
        this.type = requireNonNull(operationType);
        this.methodHandle = requireNonNull(methodHandle);
    }

    /** Whether or not the first argument to the method handle must be the bean instance. */
    public boolean requiresBeanInstance() {
        return false;
    }

    /** {@inheritDoc} */
    public final OperationType type() {
        return type;
    }

    /** An operation target that simply returns a constant. */
    public static final class ConstantOperationSite extends OperationSite implements OperationSiteMirror.OfConstant {

        /** The constant. */
        @Nullable
        public final Object constant;

        /** The type of the constant. */
        public final Class<?> constantType;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public ConstantOperationSite(BeanSetup bean, OperationType operationType, Class<?> constantType, @Nullable Object constant) {
            super(bean, operationType, MethodHandles.constant(constantType, constant));
            this.constantType = constantType;
            this.constant = constant;
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> constantType() {
            return constantType;
        }
    }

    public static final class ConstructorOperationSite extends OperationSite implements OperationSiteMirror.OfConstructorInvoke {

        private final Constructor<?> constructor;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public ConstructorOperationSite(BeanSetup bean, MethodHandle methodHandle, Constructor<?> constructor) {
            super(bean, OperationType.ofExecutable(constructor), methodHandle);
            this.constructor = constructor;
        }

        /** {@return the invokable method.} */
        public Constructor<?> constructor() {
            return constructor;
        }

        public String toString() {
            return constructor.toString();
        }
    }

    public static final class FieldOperationSite extends OperationSite implements OperationSiteMirror.OfFieldAccess {

        private final AccessMode accessMode;

        private final Field field;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public FieldOperationSite(BeanSetup bean, OperationType operationType, MethodHandle methodHandle, Field field, AccessMode accessMode) {
            super(bean, operationType, methodHandle);
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

        public boolean requiresBeanInstance() {
            return !Modifier.isStatic(field.getModifiers());
        }
    }

    public static final class FunctionOperationSite extends OperationSite implements OperationSiteMirror.OfFunctionCall {

        // Can read it from the method... no
        private final Class<?> functionalInterface;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public FunctionOperationSite(BeanSetup bean, OperationType operationType, MethodHandle methodHandle, Class<?> functionalInterface) {
            super(bean, operationType, methodHandle);
            this.functionalInterface = requireNonNull(functionalInterface);
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> functionalInterface() {
            return functionalInterface;
        }
    }

    public static final class LifetimePoolAccessSite extends OperationSite implements OperationSiteMirror.OfLifetimePoolAccess {


        /**
         * @param methodHandle
         * @param isStatic
         */
        public LifetimePoolAccessSite(BeanSetup bean, OperationType operationType, MethodHandle methodHandle) {
            super(bean, operationType, methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        public Optional<OperationMirror> origin() {
            return bean.mirror().factoryOperation();
        }
    }

    public static final class MethodHandleOperationSite extends OperationSite implements OperationSiteMirror.OfMethodHandleInvoke {

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public MethodHandleOperationSite(BeanSetup bean, OperationType operationType, MethodHandle methodHandle) {
            super(bean, operationType, methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        public MethodType methodType() {
            throw new UnsupportedOperationException();
        }
    }

    /** An operation site representing the invocation of a method. */
    public static final class MethodOperationSite extends OperationSite implements OperationSiteMirror.OfMethodInvoke {

        /** The actual method. */
        private final Method method;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public MethodOperationSite(BeanSetup bean, MethodHandle methodHandle, Method method) {
            super(bean, OperationType.ofExecutable(method), methodHandle);
            this.method = method;
        }

        /** {@return the invokable method.} */
        public Method method() {
            return method;
        }

        public boolean requiresBeanInstance() {
            return !Modifier.isStatic(method.getModifiers());
        }

        public String toString() {
            return method.toString();
        }
    }
}