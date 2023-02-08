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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.bean.BeanElement.BeanField;
import app.packed.bindings.Key;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.util.FunctionType;
import app.packed.util.Variable;
import internal.app.packed.bean.hooks.HookOnFieldAnnotation;
import internal.app.packed.operation.OperationMemberTarget.OperationFieldTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;
import internal.app.packed.operation.PackedOperationHandle;
import internal.app.packed.service.KeyHelper;
import internal.app.packed.util.PackedVariable;
import internal.app.packed.util.PackedAnnotationList;

/** Implementation of {@link BeanField}. */
public final class PackedBeanField extends PackedBeanMember<Field> implements BeanField , Comparable<PackedBeanField> {

    /** Whether or not operations that read from the field can be created. */
    final boolean allowGet;

    /** Whether or not operations that write to the field can be created. */
    final boolean allowSet;

    /** Hooks on the field */
    private final PackedAnnotationList hooks;

    private final AnnotatedType annotatedType;

    PackedBeanField(BeanReflector scanner, Field field, PackedAnnotationList annotations, PackedAnnotationList hooks,
            HookOnFieldAnnotation... annotatedFields) {
        super(scanner.computeContributor(annotatedFields[0].extensionType()), field, annotations);
        boolean allowGet = extension.hasFullAccess();
        boolean allowSet = extension.hasFullAccess();
        for (HookOnFieldAnnotation annotatedField : annotatedFields) {
            allowGet |= annotatedField.isGettable();
            allowSet |= annotatedField.isSettable();
        }
        this.allowGet = allowGet;
        this.allowSet = allowSet;
        this.hooks = hooks;
        this.annotatedType = member.getAnnotatedType(); // TODO take as parameter
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(PackedBeanField o) {
        return member.getName().compareTo(o.member.getName());
    }

    /** {@inheritDoc} */
    @Override
    public Field field() {
        return member;
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newGetOperation(OperationTemplate template) {
        checkConfigurable();
        MethodHandle mh = extension.scanner.unreflectGetter(member);
        AccessMode accessMode = Modifier.isVolatile(member.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        return newOperation(template, mh, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(OperationTemplate template, AccessMode accessMode) {
        checkConfigurable();
        VarHandle varHandle = extension.scanner.unreflectVarHandle(member);
        MethodHandle mh = varHandle.toMethodHandle(accessMode);
        return newOperation(template, mh, accessMode);
    }

    private PackedOperationHandle newOperation(OperationTemplate template, MethodHandle mh, AccessMode accessMode) {
        template = template.withReturnType(member.getType());
        OperationSetup operation = new MemberOperationSetup(extension.extension, extension.scanner.bean, FunctionType.ofField(member, accessMode), template,
                new OperationFieldTarget(member, accessMode), mh);
        extension.scanner.unBoundOperations.add(operation);
        extension.scanner.bean.operations.add(operation);
        return operation.toHandle();
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newSetOperation(OperationTemplate template) {
        checkConfigurable();
        MethodHandle mh = extension.scanner.unreflectSetter(member);
        AccessMode accessMode = Modifier.isVolatile(member.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        return newOperation(template, mh, accessMode);
    }

    void onHook() {
        extension.introspector.hookOnAnnotatedField(hooks, this);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        return KeyHelper.convert(member, annotatedType);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return PackedVariable.of(annotatedType);
    }
}
