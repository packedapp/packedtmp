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
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import app.packed.bean.BeanMirror;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTargetMirror;
import internal.app.packed.bean.BeanSetup;

/**
 * The target of an operation.
 */
public sealed abstract class OperationTarget implements OperationTargetMirror {

    public final MethodHandle methodHandle;

    /** Whether or not the first argument to the method handle is the bean instance. */
    public final boolean requiresBeanInstance;

    protected OperationTarget(MethodHandle methodHandle, boolean requiresBeanInstance) {
        this.methodHandle = methodHandle;
        this.requiresBeanInstance = requiresBeanInstance;
    }

    public static final class BeanInstanceAccess extends OperationTarget implements OperationTargetMirror.OfAccessBeanInstance {

        private final BeanSetup bean;

        /**
         * @param methodHandle
         * @param isStatic
         */
        public BeanInstanceAccess(BeanSetup bean, MethodHandle methodHandle) {
            super(methodHandle, false);
            this.bean = bean;
        }

        /** {@inheritDoc} */
        @Override
        public BeanMirror bean() {
            return bean.mirror();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<OperationMirror> origin() {
            return bean().factoryOperation();
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
            super(methodHandle, !Modifier.isStatic(field.getModifiers()));
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

    public static final class ConstructorOperationTarget extends OperationTarget implements OperationTargetMirror.OfConstructorInvoke {

        private final Constructor<?> constructor;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public ConstructorOperationTarget(MethodHandle methodHandle, Constructor<?> constructor) {
            super(methodHandle, false);
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

    public static final class MethodOperationTarget extends OperationTarget implements OperationTargetMirror.OfMethodInvoke {

        private final Method method;

        /**
         * @param methodHandle
         * @param requiresBeanInstance
         */
        public MethodOperationTarget(MethodHandle methodHandle, Method method) {
            super(methodHandle, !Modifier.isStatic(method.getModifiers()));
            this.method = method;
        }

        /** {@return the invokable method.} */
        public Method method() {
            return method;
        }

        public String toString() {
            return method.toString();
        }
    }
}