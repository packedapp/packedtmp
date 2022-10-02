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
package internal.app.packed.bean;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanIntrospector.AnnotationReader;
import app.packed.bean.BeanIntrospector.OnFieldHook;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTargetMirror;
import app.packed.operation.OperationType;
import app.packed.operation.Variable;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.OperationTarget;
import internal.app.packed.operation.PackedOperationHandle;

/**
 * Implementation of {@link OnFieldHook}.
 */
public final class IntrospectorOnField implements OnFieldHook {

    /** Whether or not the field can be read. */
    final boolean allowGet;

    /** Whether or not the field can be written. */
    final boolean allowSet;

    /** The annotations on the field */
    final Annotation[] annotations;

    /** The bean member. */
    protected final Field field;

    /** The extension that will operate any operations. */
    public final ExtensionSetup operator;

    public final Introspector introspector;
    IntrospectorOnField(Introspector introspector, ExtensionSetup operator, Field field, boolean allowGet, boolean allowSet,
            Annotation[] annotations) {
        this.introspector = introspector;
        this.operator = operator;
        this.field = field;
        this.allowGet = allowGet;
        this.allowSet = allowSet;
        this.annotations = annotations;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationReader annotations() {
        return new IntrospectorAnnotationReader(annotations);
    }

    /** {@inheritDoc} */
    @Override
    public Field field() {
        return field;
    }

    /** {@inheritDoc} */
    public int modifiers() {
        return field.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newGetOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType) {
        MethodHandle mh = introspector.oc.unreflectGetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        OperationSetup os = new OperationSetup(introspector.bean, OperationType.ofFieldAccess(field, accessMode), operator, invocationType,
                new FieldOperationTarget(mh, field, accessMode));
        introspector.bean.addOperation(os);
        return new PackedOperationHandle(os);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(ExtensionBeanConfiguration<?> operator, AccessMode accessMode, InvocationType invocationType) {
        MethodHandle mh = introspector.oc.unreflectVarHandle(field).toMethodHandle(accessMode);
        OperationSetup os = new OperationSetup(introspector.bean, OperationType.ofFieldAccess(field, accessMode), operator, invocationType,
                new FieldOperationTarget(mh, field, accessMode));
        introspector.bean.addOperation(os);

        return new PackedOperationHandle(os);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newSetOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType) {
        MethodHandle mh = introspector.oc.unreflectSetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        OperationSetup os = new OperationSetup(introspector.bean, OperationType.ofFieldAccess(field, accessMode), operator, invocationType,
                new FieldOperationTarget(mh, field, accessMode));
        introspector.bean.addOperation(os);

        return new PackedOperationHandle(os);
    }

    public VarHandle newVarHandle() {
        return introspector.oc.unreflectVarHandle(field);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return Variable.ofField(field);
    }


    public static final class FieldOperationTarget extends OperationTarget implements OperationTargetMirror.OfFieldAccess {

        private final AccessMode accessMode;

        private final Field field;

        /**
         * @param methodHandle
         * @param isStatic
         */
        public FieldOperationTarget(MethodHandle methodHandle, Field field, AccessMode accessMode) {
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

}
