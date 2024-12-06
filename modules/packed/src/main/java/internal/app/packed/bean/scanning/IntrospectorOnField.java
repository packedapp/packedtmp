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
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.scanning.BeanTriggerModel.OnAnnotatedFieldCache;
import internal.app.packed.binding.PackedVariable;
import internal.app.packed.operation.OperationMemberTarget.OperationFieldTarget;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.operation.PackedOperationTemplate.ReturnKind;
import internal.app.packed.util.PackedAnnotationList;

/** Implementation of {@link BeanIntrospector.OnField}. */
// Previous we had a PackedBeanMember, but there are actually only 2-3 common operations. So don't go that road again.
public final class IntrospectorOnField extends IntrospectorOn implements BeanIntrospector.OnField, Comparable<IntrospectorOnField> {

    /** Whether or not operations that read from the field can be created. */
    private final boolean allowGet;

    /** Whether or not operations that write to the field can be created. */
    private final boolean allowSet;

    /** Annotations on the field. */
    private final PackedAnnotationList annotations;

    /** The field. */
    private final Field field;

    /** The bean introspector that was triggered. */
    private final BeanIntrospectorSetup introspector;

    /** Triggering annotations on the field (for the given introspector). */
    private final PackedAnnotationList triggeringAnnotations;

    // Field, FieldAnnotations, Type, TypeAnnotations
    private IntrospectorOnField(BeanIntrospectorSetup introspector, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations,
            OnAnnotatedFieldCache... annotatedFields) {
        this.introspector = introspector;
        this.field = field;
        this.annotations = annotations;

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

    /** {@return a list of annotations on the member.} */
    @Override
    public AnnotationList annotations() {
        return annotations;
    }

    /** {@inheritDoc} */
    @Override
    public BeanSetup bean() {
        return introspector.scanner.bean;
    }

    /** Check that we calling from within {@link BeanIntrospector#onField(OnField).} */
    void checkConfigurable() {
        if (!introspector.extension().isConfigurable()) {
            throw new IllegalStateException("This method must be called before the extension is closed");
        }
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(IntrospectorOnField o) {
        return field.getName().compareTo(o.field.getName());
    }

    /** {@inheritDoc} */
    @Override
    public void failWith(String message) {
        throw new BeanInstallationException(message);
    }

    /** {@inheritDoc} */
    @Override
    public Field field() {
        return field;
    }

    /** {@return the modifiers of the member.} */
    @Override
    public int modifiers() {
        return field.getModifiers();
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
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;

        if (t.returnKind == ReturnKind.DYNAMIC) {
            t = t.withReturnType(field.getType());
        }

//        template = template.reconfigure(c -> c.returnType(field.getType()));
        MethodHandle directMH = introspector.scanner.unreflectGetter(field);
        return newOperation(t, directMH, accessMode);
    }

    private OperationInstaller newOperation(OperationTemplate template, MethodHandle mh, AccessMode accessMode) {
        PackedOperationTemplate t = (PackedOperationTemplate) template;
        OperationType ft = OperationType.fromField(field, accessMode);

        // We should be able to create the method handle lazily
        return t.newInstaller(introspector, mh, new OperationFieldTarget(field, accessMode), ft);
    }

    /** {@inheritDoc} */
    @Override
    public OperationInstaller newOperation(OperationTemplate template, VarHandle.AccessMode accessMode) {
        requireNonNull(template, "template is null");
        checkConfigurable();

        VarHandle varHandle = introspector.scanner.unreflectVarHandle(field);
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
        MethodHandle mh = introspector.scanner.unreflectSetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;

        return newOperation(template, mh, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        return Key.fromField(field);
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationList triggeringAnnotations() {
        return triggeringAnnotations;
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return new PackedVariable(annotations, field.getGenericType());
    }

    static void process(BeanIntrospectorSetup introspector, Field field, PackedAnnotationList annotations, PackedAnnotationList triggeringAnnotations,
            OnAnnotatedFieldCache... annotatedFields) {
        IntrospectorOnField f = new IntrospectorOnField(introspector, field, annotations, triggeringAnnotations, annotatedFields);
        introspector.instance.onAnnotatedField(triggeringAnnotations, f);
    }
}
