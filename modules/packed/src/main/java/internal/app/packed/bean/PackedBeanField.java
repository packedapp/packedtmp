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

import app.packed.bean.BeanElement.BeanField;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import app.packed.util.AnnotationList;
import internal.app.packed.bean.BeanHookCache.HookOnFieldAnnotation;
import internal.app.packed.binding.PackedVariable;
import internal.app.packed.operation.OperationMemberTarget.OperationFieldTarget;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.util.PackedAnnotationList;

/** Implementation of {@link BeanField}. */
// Previous we had a PackedBeanMember, but there are actually only 2-3 common operations. So don't go that road again.
public final class PackedBeanField implements BeanField, Comparable<PackedBeanField> {

    /** Whether or not operations that read from the field can be created. */
    final boolean allowGet;

    /** Whether or not operations that write to the field can be created. */
    final boolean allowSet;

    /** Annotations on the field. */
    private final PackedAnnotationList annotations;

    /** The extension that can create new operations from the member. */
    private final BeanScannerExtensionRef extension;

    /** The field. */
    public final Field field;

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
    public OperationTemplate.Installer newGetOperation(OperationTemplate template) {
        checkConfigurable();
        MethodHandle mh = extension.scanner.unreflectGetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        template = template.reconfigure(c -> c.returnType(field.getType()));
        return newOperation(template, mh, accessMode);
    }

    private OperationTemplate.Installer newOperation(OperationTemplate template, MethodHandle mh, AccessMode accessMode) {
        PackedOperationTemplate t = (PackedOperationTemplate) template;
        OperationType ft = OperationType.fromField(field, accessMode);

        // We should be able to create the method handle lazily
        return t.newInstaller(extension, mh, new OperationFieldTarget(field, accessMode), ft);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate.Installer newOperation(OperationTemplate template, VarHandle.AccessMode accessMode) {
        checkConfigurable();
        VarHandle varHandle = extension.scanner.unreflectVarHandle(field);
        MethodHandle mh = varHandle.toMethodHandle(accessMode);
        return newOperation(template, mh, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public OperationTemplate.Installer newSetOperation(OperationTemplate template) {
        checkConfigurable();
        MethodHandle mh = extension.scanner.unreflectSetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        return newOperation(template, mh, accessMode);
    }

    void onHook() {
        extension.introspector.activatedByAnnotatedField(hooks, this);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> toKey() {
        return Key.fromField(this);
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return new PackedVariable(annotations, field.getGenericType());
    }
}
