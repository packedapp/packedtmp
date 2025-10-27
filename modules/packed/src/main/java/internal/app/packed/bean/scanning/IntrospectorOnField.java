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
package internal.app.packed.bean.scanning;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.scanning.BeanIntrospector;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import app.packed.util.AnnotationList;
import internal.app.packed.bean.scanning.BeanTriggerModel.OnAnnotatedFieldCache;
import internal.app.packed.binding.PackedVariable;
import internal.app.packed.operation.OperationMemberTarget.OperationFieldTarget;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.operation.PackedOperationTemplate.ReturnKind;
import internal.app.packed.util.PackedAnnotationList;

/** Implementation of {@link BeanIntrospector.OnField}. */
public final class IntrospectorOnField extends IntrospectorOnMember<Field> implements BeanIntrospector.OnField, Comparable<IntrospectorOnField> {

    /** Whether or not operations that read from the field can be created. */
    private final boolean allowGet;

    /** Whether or not operations that write to the field can be created. */
    private final boolean allowSet;

    /** Triggering annotations on the field (for the given introspector). */
    private final PackedAnnotationList triggeringAnnotations;

    // Field, FieldAnnotations, Type, TypeAnnotations
    private IntrospectorOnField(BeanIntrospectorSetup introspector, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations,
            OnAnnotatedFieldCache... annotatedFields) {
        super(introspector, field, annotations);

        boolean allowGet = introspector.hasFullAccess();
        boolean allowSet = allowGet;
        for (OnAnnotatedFieldCache annotatedField : annotatedFields) {
            allowGet |= annotatedField.isGettable();
            allowSet |= annotatedField.isSettable();
        }
        this.allowGet = allowGet;
        this.allowSet = allowSet;

        this.triggeringAnnotations = triggeringAnnotations;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(IntrospectorOnField o) {
        return member.getName().compareTo(o.member.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void failWith(String message) {
        throw new BeanInstallationException(message);
    }

    /** {@inheritDoc} */
    @Override
    public Field field() {
        return member;
    }

    /** {@inheritDoc} */
    @Override
    public OperationInstaller newGetOperation(OperationTemplate template) {
        PackedOperationTemplate t = (PackedOperationTemplate) requireNonNull(template, "template is null");
        checkConfigurable();
        if (!allowGet) {
            throw new IllegalStateException("" + triggeringAnnotations);
        }
        // Get A direct method handle to a getter for the field
        AccessMode accessMode = Modifier.isVolatile(modifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;

        if (t.returnKind == ReturnKind.DYNAMIC) {
            t = t.withReturnType(member.getType());
        }

//        template = template.reconfigure(c -> c.returnType(field.getType()));
        MethodHandle directMH = introspector.scanner.unreflectGetter(member);
        return newOperation(t, directMH, accessMode);
    }

    private OperationInstaller newOperation(OperationTemplate template, MethodHandle directMH, AccessMode accessMode) {
        PackedOperationTemplate t = (PackedOperationTemplate) template;
        OperationType ft = OperationType.fromField(member, accessMode);

        // We should be able to create the method handle lazily
        return t.newInstaller(introspector, directMH, new OperationFieldTarget(member, accessMode), ft);
    }

    /** {@inheritDoc} */
    @Override
    public OperationInstaller newOperation(OperationTemplate template, VarHandle.AccessMode accessMode) {
        requireNonNull(template, "template is null");
        checkConfigurable();

        VarHandle varHandle = introspector.scanner.unreflectVarHandle(member);
        MethodHandle mh = varHandle.toMethodHandle(accessMode);

        return newOperation(template, mh, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public OperationInstaller newSetOperation(OperationTemplate template) {
        requireNonNull(template, "template is null");
        checkConfigurable();
        if (!allowSet) {
            throw new IllegalStateException();
        }
        MethodHandle mh = introspector.scanner.unreflectSetter(member);
        AccessMode accessMode = Modifier.isVolatile(member.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;

        return newOperation(template, mh, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        return Key.fromField(member);
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationList triggeringAnnotations() {
        return triggeringAnnotations;
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return new PackedVariable(annotations, member.getGenericType());
    }

    static void process(BeanIntrospectorSetup introspector, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations,
            OnAnnotatedFieldCache... annotatedFields) {
        IntrospectorOnField f = new IntrospectorOnField(introspector, field, annotations, triggeringAnnotations, annotatedFields);
        introspector.introspector.onAnnotatedField(triggeringAnnotations, f);
    }
}
