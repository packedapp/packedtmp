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
import java.util.IdentityHashMap;

import app.packed.bean.BeanDefinitionException;
import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.bean.BeanExtensionPoint.FieldHook;
import app.packed.bean.BeanIntrospector.AnnotationReader;
import app.packed.bean.BeanIntrospector.OnField;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.container.Extension;
import app.packed.container.ExtensionBeanConfiguration;
import app.packed.container.InternalExtensionException;
import app.packed.operation.InvocationType;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import app.packed.operation.Variable;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.InvocationSite;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationTarget.FieldOperationTarget;
import internal.app.packed.operation.PackedOperationHandle;

/**
 * Implementation of {@link OnField}.
 */
public final class FieldIntrospector implements OnField {

    /** Whether or not the field can be read. */
    final boolean allowGet;

    /** Whether or not the field can be written. */
    final boolean allowSet;

    /** The annotations on the field */
    final Annotation[] annotations;

    /** The bean member. */
    protected final Field field;

    public final Introspector introspector;

    /** The extension that will operate any operations. */
    public final ExtensionSetup operator;

    private FieldIntrospector(Introspector introspector, ExtensionSetup operator, Field field, boolean allowGet, boolean allowSet, Annotation[] annotations) {
        this.introspector = introspector;
        this.operator = operator;
        this.field = field;
        this.allowGet = allowGet;
        this.allowSet = allowSet;
        this.annotations = annotations;
    }

    private OperationSetup add(MethodHandle mh, InvocationType invocationType, AccessMode accessMode) {
        FieldOperationTarget fot = new FieldOperationTarget(mh, field, accessMode);
        OperationType ot = OperationType.ofFieldAccess(field, accessMode);
        InvocationSite oi = new InvocationSite(invocationType, operator); // operator??
        OperationSetup bos = new OperationSetup(introspector.bean, ot, oi, fot, null);
        introspector.bean.operations.add(bos);
        return bos;
    }

    /** {@inheritDoc} */
    @Override
    public AnnotationReader annotations() {
        return new BeanAnnotationReader(annotations);
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
        return new PackedOperationHandle(this.operator, add(mh, invocationType, accessMode));
    }

    public OperationSetup newInternalGetOperation(ExtensionSetup operator, InvocationType invocationType) {
        MethodHandle mh = introspector.oc.unreflectGetter(field);
        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        return add(mh, invocationType, accessMode);
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newOperation(ExtensionBeanConfiguration<?> operator, AccessMode accessMode, InvocationType invocationType) {

        Lookup lookup = introspector.oc.lookup(field);

        VarHandle varHandle;
        try {
            varHandle = lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a VarHandle", e);
        }

        MethodHandle mh = varHandle.toMethodHandle(accessMode);
        return new PackedOperationHandle(this.operator, add(mh, invocationType, accessMode));
    }

    /** {@inheritDoc} */
    @Override
    public OperationHandle newSetOperation(ExtensionBeanConfiguration<?> operator, InvocationType invocationType) {
        Lookup lookup = introspector.oc.lookup(field);

        MethodHandle methodHandle;
        try {
            methodHandle = lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }

        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        return new PackedOperationHandle(this.operator, add(methodHandle, invocationType, accessMode));
    }

    /** {@inheritDoc} */
    @Override
    public Variable variable() {
        return Variable.ofField(field);
    }

    /**
     * Introspect a single field on a bean.
     * 
     * Look for hook annotations on a single field.
     * 
     * @param field
     *            the field to introspect
     * 
     * @throws BeanDefinitionException
     *             if there are multiple {@link BindingHook} on the field. Or if there are both {@link FieldHook} and
     *             {@link BindingHook} annotations
     * 
     * @apiNote Currently we allow multiple {@link FieldHook} on a field. This might change in the future, but for now we
     *          allow it.
     */
    private static void introspectField(Introspector introspector, Field field) {
        // Get all annotations on the field
        Annotation[] annotations = field.getAnnotations();

        // Iterate through the annotations and look for usage of field and binding hook (meta) annotations
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];

            // Look in the field annotation cache to see if the annotation is a meta annotation
            FieldAnnotationCache e = FieldAnnotationCache.CACHE.get(annotation.annotationType());

            // The annotation is neither a field or binding annotation
            if (e == null) {
                continue;
            }

            // A record + map that we use if have multi field hook annotations
            record MultiField(Class<? extends Extension<?>> extensionClass, boolean allowGet, boolean allowSet, Annotation... annotations) {}
            IdentityHashMap<Class<? extends Extension<?>>, MultiField> multiMatch = null;

            // Try to find additional meta annotations.
            for (int j = i; j < annotations.length; j++) {
                Annotation annotation2 = annotations[j];

                // Look in the annotation cache to see if the annotation is a meta annotation
                FieldAnnotationCache e2 = FieldAnnotationCache.CACHE.get(annotation2.annotationType());

                // The annotation is neither a field or provision annotation
                if (e2 == null) {
                    continue;
                }

                if (e.isProvision || e2.isProvision) {
                    throw new BeanDefinitionException("Cannot use both " + annotation + " and " + annotation2);
                }

                // Okay we have more than 1 valid annotation

                // Check to see if we need to create the multi match map
                if (multiMatch == null) {
                    multiMatch = new IdentityHashMap<>();
                    // Start by adding the first match
                    multiMatch.put(e.extensionType, new MultiField(e.extensionType, e.isGettable, e.isSettable, annotation));
                }

                // Add this match
                multiMatch.compute(e2.extensionType, (Class<? extends Extension<?>> key, MultiField value) -> {
                    if (value == null) {
                        return new MultiField(key, e2.isGettable, e2.isSettable, annotation2);
                    } else {
                        Annotation[] a = new Annotation[value.annotations.length + 1];
                        for (int k = 0; k < value.annotations.length; k++) {
                            a[k] = value.annotations[k];
                        }
                        a[a.length - 1] = annotation2;
                        return new MultiField(key, e2.isGettable && value.allowGet, e2.isSettable && e2.isSettable, a);
                    }
                });
            }

            // All done. Let us see if we only had a single match or multiple matches
            if (multiMatch == null) {
                // Get the matching extension, installing it if needed.
                Introspector.Delegate entry = introspector.computeExtensionEntry(e.extensionType, false);

                // Create the wrapped field that is exposed to the extension
                FieldIntrospector f = new FieldIntrospector(introspector, entry.extension(), field, e.isGettable || entry.hasFullAccess(),
                        e.isSettable || entry.hasFullAccess(), new Annotation[] { annotation });

                entry.introspector().onField(f); // Calls BeanIntrospection.onField
            } else {
                // TODO we should sort by extension order when we have more than 1 match
                for (MultiField mf : multiMatch.values()) {
                    Introspector.Delegate entry = introspector.computeExtensionEntry(mf.extensionClass, false);

                    // Create the wrapped field that is exposed to the extension
                    FieldIntrospector f = new FieldIntrospector(introspector, entry.extension(), field, mf.allowGet || entry.hasFullAccess(),
                            mf.allowSet || entry.hasFullAccess(), annotations);

                    entry.introspector().onField(f); // Calls BeanIntrospection.onField
                }
            }
        }
    }

    static void introspectFields(Introspector introspector, Class<?> clazz) {
        // We never process classes in the "java.base" module.
        if (clazz.getModule() != Introspector.JAVA_BASE_MODULE) {

            // Recursively call into superclass, before processing own fields
            introspectFields(introspector, clazz.getSuperclass());

            // PackedDevToolsIntegration.INSTANCE.reflectMembers(c, fields);

            // Iterate over all declared fields
            for (Field field : clazz.getDeclaredFields()) {
                introspectField(introspector, field);
            }
        }
    }

    /** Cache the various annotations that are placed on fields. */
    private record FieldAnnotationCache(Class<? extends Annotation> annotationType, Class<? extends Extension<?>> extensionType, boolean isGettable,
            boolean isSettable, boolean isProvision) {

        /** A cache of any extensions a particular annotation activates. */
        private static final ClassValue<FieldAnnotationCache> CACHE = new ClassValue<>() {

            @Override
            protected FieldAnnotationCache computeValue(Class<?> type) {
                @SuppressWarnings("unchecked")
                Class<? extends Annotation> annotationType = (Class<? extends Annotation>) type;
                FieldHook fieldHook = type.getAnnotation(FieldHook.class);
                BindingHook provisionHook = type.getAnnotation(BindingHook.class);

                if (provisionHook == fieldHook) { // check both null
                    return null;
                } else if (provisionHook == null) {
                    Introspector.checkExtensionClass(type, fieldHook.extension());
                    return new FieldAnnotationCache(annotationType, fieldHook.extension(), fieldHook.allowGet(), fieldHook.allowSet(), false);
                } else if (fieldHook == null) {
                    Introspector.checkExtensionClass(type, provisionHook.extension());
                    return new FieldAnnotationCache(annotationType, provisionHook.extension(), false, true, true);
                } else {
                    throw new InternalExtensionException(type + " cannot both be annotated with " + FieldHook.class + " and " + BindingHook.class);
                }
            }
        };
    }
}
