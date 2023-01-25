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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.OperationalField;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.binding.Variable;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanHookModel.AnnotatedField;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.FieldOperationSetup;

/** Responsible for scanning fields on a bean. */
public final class PackedOperationalField extends PackedOperationalMember<Field> implements OperationalField {

    /** Whether or not operations that read from the field can be created. */
    final boolean allowGet;

    /** Whether or not operations that write to the field can be created. */
    final boolean allowSet;

    PackedOperationalField(BeanScanner scanner, Class<? extends Extension<?>> extensionType, Field field, boolean allowGet, boolean allowSet,
            Annotation[] annotations, AnnotatedField... annotatedFields) {
        super(scanner.computeContributor(extensionType), field, annotations);
        this.allowGet = allowGet || ce.hasFullAccess();
        this.allowSet = allowSet || ce.hasFullAccess();
    }

    PackedOperationalField(BeanScanner scanner, Field field, Annotation[] annotations, AnnotatedField... annotatedFields) {
        super(scanner.computeContributor(annotatedFields[0].extensionType()), field, annotations);
        boolean allowGet = ce.hasFullAccess();
        boolean allowSet = ce.hasFullAccess();
        for (AnnotatedField annotatedField : annotatedFields) {
            allowGet |= annotatedField.isGettable();
            allowSet |= annotatedField.isSettable();
        }
        this.allowGet = allowGet;
        this.allowSet = allowSet;
    }

    PackedOperationalField(ContributingExtension scanner, Field field, Annotation[] annotations, AnnotatedField... annotatedFields) {
        super(scanner, field, annotations);
        boolean allowGet = ce.hasFullAccess();
        boolean allowSet = ce.hasFullAccess();
        for (AnnotatedField annotatedField : annotatedFields) {
            allowGet |= annotatedField.isGettable();
            allowSet |= annotatedField.isSettable();
        }
        this.allowGet = allowGet;
        this.allowSet = allowSet;
    }



    /** {@inheritDoc} */
    @Override
    public Field field() {
        return member;
    }

    /** Callback into an extension's {@link BeanIntrospector#hookOnAnnotatedField(OperationalField)} method. */
    void matchy() {
        ce.introspector().hookOnAnnotatedField(PackedAnnotationCollection.of(), this);
        ce.scanner.resolveOperations(); // resolve bindings for any operation(s) that have been created
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newGetOperation(OperationTemplate template) {
        checkConfigurable();

        Lookup lookup = ce.scanner.oc.lookup(member);

        MethodHandle methodHandle;
        try {
            methodHandle = lookup.unreflectGetter(member);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }

        AccessMode accessMode = Modifier.isVolatile(member.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        return newOperation(template, methodHandle, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(OperationTemplate template, AccessMode accessMode) {
        checkConfigurable();
        Lookup lookup = ce.scanner.oc.lookup(member);

        VarHandle varHandle;
        try {
            varHandle = lookup.unreflectVarHandle(member);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a VarHandle", e);
        }

        MethodHandle mh = varHandle.toMethodHandle(accessMode);
        return newOperation(template, mh, accessMode);
    }

    private OperationHandle newOperation(OperationTemplate template, MethodHandle mh, AccessMode accessMode) {
        template = template.withReturnType(member.getType());
        OperationSetup operation = new FieldOperationSetup(ce.extension(), ce.scanner.bean, OperationType.ofFieldAccess(member, accessMode), template, mh, member,
                accessMode);

        ce.scanner.unBoundOperations.add(operation);
        ce.scanner.bean.operations.add(operation);
        return operation.toHandle();
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newSetOperation(OperationTemplate template) {
        checkConfigurable();
        Lookup lookup = ce.scanner.oc.lookup(member);

        // Create a method handle by unreflecting the field.
        // Will fail if the framework does not have access to the member
        MethodHandle methodHandle;
        try {
            methodHandle = lookup.unreflectSetter(member);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }

        AccessMode accessMode = Modifier.isVolatile(member.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        return newOperation(template, methodHandle, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return Variable.ofField(member);
    }
}
