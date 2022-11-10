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
import app.packed.operation.OperationTargetMirror;
import internal.app.packed.bean.BeanSetup;

/**
 * The target of an operation.
 */
public sealed abstract class OperationTarget implements OperationTargetMirror {

    public final MethodHandle methodHandle;

    protected OperationTarget(MethodHandle methodHandle) {
        this.methodHandle =  requireNonNull(methodHandle);
    }

    /** Whether or not the first argument to the method handle is the bean instance. */
    public boolean requiresBeanInstance() {
        return false;
    }
        
    public static final class LifetimePoolAccessTarget extends OperationTarget implements OperationTargetMirror.OfLifetimePoolAccess {

        public final BeanSetup bean;

        /**
         * @param methodHandle
         * @param isStatic
         */
        public LifetimePoolAccessTarget(BeanSetup bean, MethodHandle methodHandle) {
            super(methodHandle);
            this.bean = bean;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<OperationMirror> origin() {
            return bean.mirror().factoryOperation();
        }
    }

    public static final class FunctionOperationTarget extends OperationTarget implements OperationTargetMirror.OfFunctionCall {

        // Can read it from the method... no 
        private final Class<?> functionalInterface;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public FunctionOperationTarget(MethodHandle methodHandle, Class<?> functionalInterface) {
            super(methodHandle);
            this.functionalInterface=requireNonNull(functionalInterface);
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> functionalInterface() {
            return functionalInterface;
        }
    }

    public static final class MethodHandleOperationTarget extends OperationTarget implements OperationTargetMirror.OfMethodHandleInvoke {

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public MethodHandleOperationTarget(MethodHandle methodHandle) {
            super(methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        public MethodType methodType() {
            throw new UnsupportedOperationException();
        }
    }

    /** An operation target that simply returns a constant. */
    public static final class ConstantOperationTarget extends OperationTarget implements OperationTargetMirror.OfConstant {

        /** The constant. */
        @Nullable
        public final Object constant;

        /** The type of the constant. */
        public final Class<?> constantType;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public ConstantOperationTarget(Class<?> constantType, @Nullable Object constant) {
            super(MethodHandles.constant(constantType, constant));
            this.constantType = constantType;
            this.constant = constant;
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> constantType() {
            return constantType;
        }
    }

    public static final class ConstructorOperationTarget extends OperationTarget implements OperationTargetMirror.OfConstructorInvoke {

        private final Constructor<?> constructor;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public ConstructorOperationTarget(MethodHandle methodHandle, Constructor<?> constructor) {
            super(methodHandle);
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

    public static final class FieldOperationTarget extends OperationTarget implements OperationTargetMirror.OfFieldAccess {

        private final AccessMode accessMode;

        private final Field field;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public FieldOperationTarget(MethodHandle methodHandle, Field field, AccessMode accessMode) {
            super(methodHandle);
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

    public static final class MethodOperationTarget extends OperationTarget implements OperationTargetMirror.OfMethodInvoke {

        private final Method method;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public MethodOperationTarget(MethodHandle methodHandle, Method method) {
            super(methodHandle);
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