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
package internal.app.packed.bean.introspection;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanIntrospector$AnnotationReader;
import app.packed.bean.BeanIntrospector$OnFieldHook;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTargetMirror;
import app.packed.operation.OperationType;
import app.packed.operation.Variable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.PackedOperationHandle;
import internal.app.packed.operation.PackedOperationHandle.Wrapper.FieldWrapper;
import internal.app.packed.operation.PackedOperationTarget;

/**
 * Implementation of {@link BeanIntrospector$OnFieldHook}.
 */
public final class PackedBeanField implements PackedOperationTarget , BeanIntrospector$OnFieldHook {

    /** Whether or not the field can be read. */
    final boolean allowGet;

    /** Whether or not the field can be written. */
    final boolean allowSet;

    /** The annotations on the field */
    final Annotation[] annotations;

    /** The bean that declares the member */
    public final BeanSetup bean;

    /** The bean member. */
    protected final Field field;

    final OpenClass openClass;

    /** The extension that will operate any operations. */
    public final ExtensionSetup operator;

    PackedBeanField(BeanSetup bean, BeanIntrospectionHelper scanner, ExtensionSetup operator, Field field, boolean allowGet, boolean allowSet,
            Annotation[] annotations) {
        this.openClass = scanner.oc;
        this.bean = bean;
        this.operator = operator;
        this.field = field;
        this.allowGet = allowGet;
        this.allowSet = allowSet;
        this.annotations = annotations;
    }

    /** {@inheritDoc} */
    @Override
    public BeanIntrospector$AnnotationReader annotations() {
        return new PackedAnnotationReader(annotations);
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
        MethodHandle mh = openClass.unreflectGetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        return new PackedOperationHandle(OperationType.ofFieldAccess(field, accessMode), operator, invocationType, bean, this,
                new FieldWrapper(mh, field, accessMode));
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(ExtensionBeanConfiguration<?> operator, AccessMode accessMode, InvocationType invocationType) {
        MethodHandle mh = openClass.unreflectVarHandle(field).toMethodHandle(accessMode);
        return new PackedOperationHandle(OperationType.ofFieldAccess(field, accessMode), operator, invocationType, bean, this,
                new FieldWrapper(mh, field, accessMode));
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newSetOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType) {
        MethodHandle mh = openClass.unreflectSetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        return new PackedOperationHandle(OperationType.ofFieldAccess(field, accessMode), operator, invocationType, bean, this,
                new FieldWrapper(mh, field, accessMode));
    }

    public VarHandle newVarHandle() {
        return openClass.unreflectVarHandle(field);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return Variable.ofField(field);
    }

    /** A mirror of {@code OperationTargetMirror.OfFieldAccess}. */
    public record BuildTimeFieldTargetMirror(PackedBeanField pbf) implements OperationTargetMirror.OfFieldAccess {

        /** {@inheritDoc} */
        @Override
        public boolean allowGet() {
            return pbf.allowGet;
        }

        /** {@inheritDoc} */
        @Override
        public boolean allowSet() {
            return pbf.allowSet;
        }

        /** {@inheritDoc} */
        @Override
        public Field field() {
            return pbf.field;
        }

        /** {@inheritDoc} */
        @Override
        public AccessMode accessMode() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return null;
        }
    }
}
