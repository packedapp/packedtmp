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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import app.packed.extension.BeanElement.BeanField;
import app.packed.operation.OperationType;
import app.packed.util.AnnotationList;
import app.packed.util.Key;
import app.packed.util.Variable;
import internal.app.packed.bean.BeanHookCache.HookOnFieldAnnotation;
import internal.app.packed.operation.OperationMemberTarget.OperationFieldTarget;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup;
import internal.app.packed.operation.PackedOperationHandle;
import internal.app.packed.util.PackedAnnotationList;
import internal.app.packed.util.PackedVariable;
import sandbox.extension.operation.OperationHandle;
import sandbox.extension.operation.OperationTemplate;

/** Implementation of {@link BeanField}. */
// Previous we had a PackedBeanMember, but there are actually only 2-3 common operations. So don't go there again.
public final class PackedBeanField implements BeanField , Comparable<PackedBeanField> {

    /** Whether or not operations that read from the field can be created. */
    final boolean allowGet;

    /** Whether or not operations that write to the field can be created. */
    final boolean allowSet;

    /** The extension that can create new operations from the member. */
    private final BeanScannerExtension extension;

    /** The field. */
    public final Field field;

    /** Annotations on the field. */
    private final PackedAnnotationList annotations;

    /** Hooks on the field */
    private final PackedAnnotationList hooks;

    // Field, FieldAnnotations, Type, TypeAnnotations
    PackedBeanField(BeanScanner scanner, Field field, PackedAnnotationList annotations, PackedAnnotationList hookAnnotations, HookOnFieldAnnotation... hooks) {
        this.extension = scanner.computeContributor(hooks[0].extensionType());
        this.field = field;
        this.annotations = annotations;

        boolean allowGet = extension.hasFullAccess();
        boolean allowSet = allowGet;
        for (HookOnFieldAnnotation annotatedField : hooks) {
            allowGet |= annotatedField.isGettable();
            allowSet |= annotatedField.isSettable();
        }
        this.allowGet = allowGet;
        this.allowSet = allowSet;
        this.hooks = hookAnnotations;
    }

    /** {@return a list of annotations on the member.} */
    @Override
    public AnnotationList annotations() {
        return annotations;
    }

    /** Check that we calling from within {@link BeanIntrospector#onField(OnField).} */
    void checkConfigurable() {
        if (!extension.extension.container.assembly.isConfigurable()) {
            throw new IllegalStateException("This method must be called before the assembly is closed");
        }
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(PackedBeanField o) {
        return field.getName().compareTo(o.field.getName());
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
    public Key<?> toKey() {
        return Key.fromField(this);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newGetOperation(OperationTemplate template) {
        checkConfigurable();
        MethodHandle mh = extension.scanner.unreflectGetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        template = template.returnType(field.getType());
        return newOperation(template, mh, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(OperationTemplate template, VarHandle.AccessMode accessMode) {
        checkConfigurable();
        VarHandle varHandle = extension.scanner.unreflectVarHandle(field);
        MethodHandle mh = varHandle.toMethodHandle(accessMode);
        return newOperation(template, mh, accessMode);
    }

    private PackedOperationHandle newOperation(OperationTemplate template, MethodHandle mh, AccessMode accessMode) {
        OperationType ft = OperationType.fromField(field, accessMode);
        OperationSetup operation = new MemberOperationSetup(extension.extension, extension.scanner.bean, ft, template,
                new OperationFieldTarget(field, accessMode), mh);
        extension.scanner.unBoundOperations.add(operation);
        extension.scanner.bean.operations.add(operation);
        return operation.toHandle(extension.scanner);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newSetOperation(OperationTemplate template) {
        checkConfigurable();
        MethodHandle mh = extension.scanner.unreflectSetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        return newOperation(template, mh, accessMode);
    }

    void onHook() {
        extension.introspector.hookOnAnnotatedField(hooks, this);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return PackedVariable.of(field.getGenericType(), field.getAnnotations());
    }
}
