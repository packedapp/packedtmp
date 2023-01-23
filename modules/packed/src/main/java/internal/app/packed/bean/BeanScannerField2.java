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
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import app.packed.bean.BeanHook.AnnotatedFieldHook;
import app.packed.bean.BeanHook.AnnotatedVariableHook;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanIntrospector.AnnotationCollection;
import app.packed.bean.BeanIntrospector.OperationalField;
import app.packed.bean.InaccessibleBeanMemberException;
import app.packed.binding.Variable;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanHookModel.AnnotatedField;
import internal.app.packed.bean.BeanHookModel.AnnotatedFieldKind;
import internal.app.packed.bean.BeanScanner.ContributingExtension;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.OperationSetup.MemberOperationSetup.FieldOperationSetup;

/** Responsible for scanning fields on a bean. */
public final class BeanScannerField2 {

    /** Whether or not operations that read from the field can be created. */
    final boolean allowGet;

    /** Whether or not operations that write to the field can be created. */
    final boolean allowSet;

    /** Annotations ({@link Field#getAnnotations()}) on the field. */
    private final Annotation[] annotations;

    /** The extension that can create operations from the field. */
    private final ContributingExtension ce;

    /** The field. */
    private final Field field;

    /** Whether or not new operations can be created from the field. */
    private boolean isDone;

    /** The bean scanner. */
    private final BeanScanner scanner;

    private BeanScannerField2(BeanScanner scanner, Class<? extends Extension<?>> extensionType, Field field, boolean allowGet, boolean allowSet,
            Annotation[] annotations, AnnotatedField... annotatedFields) {
        this.scanner = scanner;
        this.ce = scanner.computeContributor(extensionType, false);
        this.field = field;
        this.allowGet = allowGet || ce.hasFullAccess();
        this.allowSet = allowSet || ce.hasFullAccess();
        this.annotations = annotations;
    }

    private BeanScannerField2(BeanScanner scanner, Field field, Annotation[] annotations, AnnotatedField... annotatedFields) {
        this.scanner = scanner;
        this.ce = scanner.computeContributor(annotatedFields[0].extensionType(), false);
        this.field = field;
        boolean allowGet = ce.hasFullAccess();
        for (AnnotatedField annotatedField : annotatedFields) {
            allowGet |= annotatedField.isGettable();
        }
        this.allowGet = allowGet;

        boolean allowSet = ce.hasFullAccess();
        for (AnnotatedField annotatedField : annotatedFields) {
            allowSet |= annotatedField.isSettable();
        }
        this.allowSet = allowSet;
        this.annotations = annotations;
    }

    /** {@inheritDoc} */
    
    public AnnotationCollection annotations() {
        return new PackedAnnotationCollection(annotations);
    }

    /** Check that we calling from within {@link BeanIntrospector#onField(OnField).} */
    private void checkConfigurable() {
        if (isDone) {
            throw new IllegalStateException("This method must be called from within " + BeanIntrospector.class + ":onField");
        }
    }

    /** {@inheritDoc} */
    
    public Field field() {
        return field;
    }

    /** Callback into an extension's {@link BeanIntrospector#hookOnAnnotatedField(OperationalField)} method. */
    private void matchy() {
        //ce.introspector().hookOnAnnotatedField(Set.of(), this);
        isDone = true;
        scanner.resolveOperations(); // resolve bindings for any operation(s) that have been created
    }

    /** {@inheritDoc} */
    
    public int modifiers() {
        return field.getModifiers();
    }

    /** {@inheritDoc} */
    
    public OperationHandle newGetOperation(OperationTemplate template) {
        checkConfigurable();

        Lookup lookup = scanner.oc.lookup(field);

        MethodHandle methodHandle;
        try {
            methodHandle = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }

        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.GET_VOLATILE : AccessMode.GET;
        return newOperation(template, methodHandle, accessMode);
    }

    /** {@inheritDoc} */
    
    public OperationHandle newOperation(OperationTemplate template, AccessMode accessMode) {
        checkConfigurable();
        Lookup lookup = scanner.oc.lookup(field);

        VarHandle varHandle;
        try {
            varHandle = lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a VarHandle", e);
        }

        MethodHandle mh = varHandle.toMethodHandle(accessMode);
        return newOperation(template, mh, accessMode);
    }

    private OperationHandle newOperation(OperationTemplate template, MethodHandle mh, AccessMode accessMode) {
        template = template.withReturnType(field.getType());
        OperationSetup operation = new FieldOperationSetup(ce.extension(), scanner.bean, OperationType.ofFieldAccess(field, accessMode), template, mh, field,
                accessMode);

        scanner.unBoundOperations.add(operation);
        scanner.bean.operations.add(operation);
        return operation.toHandle();
    }

    /** {@inheritDoc} */
    
    public OperationHandle newSetOperation(OperationTemplate template) {
        checkConfigurable();
        Lookup lookup = scanner.oc.lookup(field);

        // Create a method handle by unreflecting the field.
        // Will fail if the framework does not have access to the member
        MethodHandle methodHandle;
        try {
            methodHandle = lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }

        AccessMode accessMode = Modifier.isVolatile(field.getModifiers()) ? AccessMode.SET_VOLATILE : AccessMode.SET;
        return newOperation(template, methodHandle, accessMode);
    }

    /** {@inheritDoc} */
    
    public Variable variable() {
        return Variable.ofField(field);
    }

    // Maaske vi kan en generisk en for members.. <T extends Member, R>
    static void introspectFieldForAnnotations(BeanScanner scanner, Field field) {
        Annotation[] annotations = field.getAnnotations();

        // Depending on the number of annotations on the field we do the processing a bit different
        switch (annotations.length) {
        case 0:
            return;
        case 1:
            AnnotatedField e = scanner.hookModel.testFieldAnnotation(annotations[0].annotationType());
            if (e != null) {
                match1(scanner, field, annotations, e, annotations);
            }
            return;
        case 2:
            Annotation a0 = annotations[0];
            Annotation a1 = annotations[1];
            AnnotatedField af0 = scanner.hookModel.testFieldAnnotation(a0.annotationType());
            AnnotatedField af1 = scanner.hookModel.testFieldAnnotation(a1.annotationType());
            if (af0 != null) {
                if (af1 == null) {
                    match1(scanner, field, annotations, af0, new Annotation[] { a0 });
                } else {
                    match2(scanner, field, annotations, af0, a0, af1, a0);
                }
            } else if (af1 != null) {
                match1(scanner, field, annotations, af1, new Annotation[] { a1 });
            }
            return;
        }

        // More than 2 annotations on the field
        ArrayList<Pair> list = new ArrayList<>(5);
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            AnnotatedField af = scanner.hookModel.testFieldAnnotation(annotation.annotationType());
            if (af != null) {
                list.add(new Pair(af, annotation));
            }
        }

        // Lets see how many interesting annotations we got
        switch (list.size()) {
        case 0:
            return;
        case 1:
            Pair p = list.get(0);
            match1(scanner, field, annotations, p.af, new Annotation[] { p.a });
            return;
        case 2:
            Pair p0 = list.get(0);
            Pair p1 = list.get(1);
            match2(scanner, field, annotations, p0.af, p0.a, p1.af, p1.a);
            return;
        }

        TreeMap<Class<? extends Extension<?>>, List<Pair>> m = new TreeMap<>();
        list.stream().forEach(e -> m.computeIfAbsent(e.af.extensionType(), v -> new ArrayList<>()).add(e));

        throw new UnsupportedOperationException();
        // So many annotations... Take the slow path
    }

    static void introspectFieldForAnnotations0(BeanScanner scanner, Field field, List<Pair> pairs) {

    }

    /**
     * Introspect a single field on a bean looking for hook annotations.
     * 
     * @param field
     *            the field to introspect
     * 
     * @throws BeanInstallationException
     *             if there are multiple {@link AnnotatedVariableHook} on the field. Or if there are both
     *             {@link AnnotatedFieldHook} and {@link AnnotatedVariableHook} annotations
     * 
     * @apiNote Currently we allow multiple {@link AnnotatedFieldHook} on a field. This might change in the future, but for
     *          now we allow it.
     */
    @SuppressWarnings("unused")
    private static void introspectFieldForAnnotationsSlow(BeanScanner scanner, Field field) {

        // Algorithm
        // Foerst find alle non-null Annotated fields.
        // Smid dem i en liste
        // Er der under

        // Get all annotations on the field
        Annotation[] annotations = field.getAnnotations();

        // Iterate through the annotations and look for usage of field and binding hook (meta) annotations
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];

            // Look in the field annotation cache to see if the annotation is a meta annotation
            AnnotatedField e = scanner.hookModel.testFieldAnnotation(annotation.annotationType());

            // The annotation is neither a annotated field or variable annotation
            if (e == null) {
                continue;
            }

            // A record + map that we use if have multi field hook annotations
            record MultiMatch(AnnotatedField af, Class<? extends Extension<?>> extensionClass, boolean allowGet, boolean allowSet, Annotation... annotations) {}
            IdentityHashMap<Class<? extends Extension<?>>, MultiMatch> multiMatches = null;

            // Try to find additional meta annotations.
            for (int j = i + 1; j < annotations.length; j++) {
                Annotation annotation2 = annotations[j];

                // Look in the annotation cache to see if the annotation is a meta annotation
                AnnotatedField e2 = scanner.hookModel.testFieldAnnotation(annotation2.annotationType());

                // The annotation is neither a field or provision annotation
                if (e2 == null) {
                    continue;
                }

                // Cannot have multiple AnnotatedVariableHook annotations
                if (e.kind() == AnnotatedFieldKind.VARIABLE && e2.kind() == AnnotatedFieldKind.VARIABLE) {
                    throw new BeanInstallationException("Cannot use both " + annotation + " and " + annotation2);
                }

                // Okay we have more than 1 valid annotation

                // Check to see if we need to create the multi match map
                if (multiMatches == null) {
                    multiMatches = new IdentityHashMap<>();
                    // Start by adding the first match
                    multiMatches.put(e.extensionType(), new MultiMatch(e, e.extensionType(), e.isGettable(), e.isSettable(), annotation));
                }

                // Add this match
                multiMatches.compute(e2.extensionType(), (Class<? extends Extension<?>> key, MultiMatch value) -> {
                    if (value == null) {
                        return new MultiMatch(e2, key, e2.isGettable(), e2.isSettable(), annotation2);
                    } else {
                        Annotation[] a = new Annotation[value.annotations.length + 1];
                        for (int k = 0; k < value.annotations.length; k++) {
                            a[k] = value.annotations[k];
                        }
                        a[a.length - 1] = annotation2;
                        return new MultiMatch(e2, key, e2.isGettable() && value.allowGet, e2.isSettable() && e2.isSettable(), a);
                    }
                });
            }

            // All done. Let's see if we only had a single match or multiple matches
            if (multiMatches == null) {
                // Get the matching extension, installing it if needed.

                // Create the wrapped field that is exposed to the extension
                BeanScannerField2 f = new BeanScannerField2(scanner, e.extensionType(), field, e.isGettable(), e.isSettable(), annotations);
                f.matchy();
            } else {
                // TODO we should sort by extension order when we have more than 1 match
                for (MultiMatch mf : multiMatches.values()) {
                    BeanScanner.ContributingExtension contributor = scanner.computeContributor(mf.extensionClass, false);

                    // Create the wrapped field that is exposed to the extension
                    BeanScannerField2 f = new BeanScannerField2(scanner, e.extensionType(), field, mf.allowGet, mf.allowSet, annotations);
                    f.matchy();
                }
            }
        }
    }



    static void match(BeanScanner scanner, Field f, Annotation[] annotations, AnnotatedFieldKind kind, Class<? extends Extension<?>> extensionType,
            boolean isGettable, boolean isSettable, Annotation[] hooks) {
        if (kind == AnnotatedFieldKind.FIELD) {
            BeanScannerField2 of = new BeanScannerField2(scanner, extensionType, f, isGettable, isSettable, annotations);
      //      of.ce.introspector().hookOnAnnotatedField(Set.of(hooks[0].annotationType()), of);
            of.isDone = true;
        } else {

            // Okay we need to make a new operation

            // ContributingExtension ei = scanner.computeContributor(extensionType, false);

            // BeanScannerBeanVariable h = new BeanScannerBeanVariable(scanner, os, index, ei.extension(), var);
            // ei.introspector().hookOnAnnotatedVariable(hooks[0], h);

            throw new UnsupportedOperationException();
        }
    }

    static void processAnnotatedField(BeanScanner scanner, Field f, Annotation[] annotations, Annotation[] hooks, AnnotatedField... afs) {
        BeanScannerField2 of = new BeanScannerField2(scanner, f, annotations, afs);
        @SuppressWarnings("unused")
        Set<?> set = Set.copyOf(List.of(hooks).stream().map(Object::getClass).toList());
    //    of.ce.introspector().hookOnAnnotatedField((Set<Class<? extends Annotation>>) set, of);
        of.isDone = true;
    }
    
    static void match1(BeanScanner scanner, Field f, Annotation[] annotations, AnnotatedField af, Annotation[] hooks) {
        if (af.kind() == AnnotatedFieldKind.FIELD) {
            processAnnotatedField(scanner, f, annotations, hooks, af);
        }
        match(scanner, f, annotations, af.kind(), af.extensionType(), af.isGettable(), af.isSettable(), hooks);
    }

    static void match2(BeanScanner scanner, Field f, Annotation[] annotations, AnnotatedField af0, Annotation a0, AnnotatedField af1, Annotation a1) {
        if (af0.kind() == AnnotatedFieldKind.VARIABLE && af1.kind() == AnnotatedFieldKind.VARIABLE) {
            throw new BeanInstallationException("Cannot use both " + a0 + " and " + a1 + " on field " + f);
        }
        if (af0.extensionType() == af1.extensionType()) {
            match(scanner, f, annotations, af0.kind(), null, false, false, annotations);
            // Create the wrapped field that is exposed to the extension
            BeanScannerField2 of = new BeanScannerField2(scanner, af0.extensionType(), f, af0.isGettable() || af1.isGettable(),
                    af0.isSettable() || af1.isSettable(), annotations);

     //       of.ce.introspector().hookOnAnnotatedField(Set.of(a0.annotationType(), a1.annotationType()), of);
            of.isDone = true;
        } else {
            // TODO Sort
            match1(scanner, f, annotations, af0, new Annotation[] { a0 });
            match1(scanner, f, annotations, af1, new Annotation[] { a1 });
        }
    }

    record Pair(AnnotatedField af, Annotation a) {}
}
