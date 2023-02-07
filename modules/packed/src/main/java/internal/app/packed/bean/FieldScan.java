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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;

import app.packed.bean.BeanInstallationException;
import app.packed.extension.Extension;
import internal.app.packed.bean.BeanHookModel.AnnotatedField;
import internal.app.packed.bean.BeanHookModel.AnnotatedFieldKind;

/**
 *
 */
class FieldScan {

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

        record Pair(AnnotatedField af, Annotation a) {}

        // More than 2 annotations on the field
        ArrayList<Pair> list = new ArrayList<>(5);
        for (Annotation annotation : annotations) {
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
        throw new UnsupportedOperationException();
        // So many annotations... Take the slow path
    }

    /**
     * Introspect a single field on a bean looking for hook annotations.
     *
     * @param field
     *            the field to introspect
     *
     * @throws BeanInstallationException
     *             if there are multiple {@link AnnotatedBindingHook} on the field. Or if there are both
     *             {@link AnnotatedFieldHook} and {@link AnnotatedBindingHook} annotations
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
                PackedBeanField f = new PackedBeanField(scanner, e.extensionType(), field, e.isGettable(), e.isSettable(), annotations);
                f.matchy();
            } else {
                // TODO we should sort by extension order when we have more than 1 match
                for (MultiMatch mf : multiMatches.values()) {
                    BeanScannerExtension contributor = scanner.computeContributor(mf.extensionClass);

                    // Create the wrapped field that is exposed to the extension
                    PackedBeanField f = new PackedBeanField(scanner, e.extensionType(), field, mf.allowGet, mf.allowSet, annotations);
                    f.matchy();
                }
            }
        }
    }

    static void match(AnnotatedFieldKind kind, BeanScanner scanner, Field f, Annotation[] annotations, Class<? extends Extension<?>> extensionType,
            boolean isGettable, boolean isSettable, Annotation[] hooks) {
        if (kind == AnnotatedFieldKind.FIELD) {
            PackedBeanField of = new PackedBeanField(scanner, extensionType, f, isGettable, isSettable, annotations);
            of.extension.introspector.hookOnAnnotatedField(PackedAnnotationList.of(hooks[0]), of);
        } else {

            // Okay we need to make a new operation

            // ContributingExtension ei = scanner.computeContributor(extensionType, false);

            // BeanScannerBeanVariable h = new BeanScannerBeanVariable(scanner, os, index, ei.extension(), var);
            // ei.introspector().hookOnAnnotatedVariable(hooks[0], h);

            throw new UnsupportedOperationException();
        }
    }

    static void match1(BeanScanner scanner, Field f, Annotation[] annotations, AnnotatedField af, Annotation[] hooks) {
        if (af.kind() == AnnotatedFieldKind.FIELD) {
            matchAnnotatedField(scanner, f, annotations, hooks, af);
        } else {

            // Annotations on record components may be propagated both to fields and parameters.

            // Records have annotations both on parameters and field
            //
        }
    }

    static void match2(BeanScanner scanner, Field f, Annotation[] annotations, AnnotatedField af0, Annotation a0, AnnotatedField af1, Annotation a1) {
        if (af0.kind() == AnnotatedFieldKind.VARIABLE && af1.kind() == AnnotatedFieldKind.VARIABLE) {
            throw new BeanInstallationException("Cannot use both " + a0 + " and " + a1 + " on field " + f);
        }
        if (af0.extensionType() == af1.extensionType()) {
            match(null, scanner, f, annotations, null, false, false, annotations);
            // Create the wrapped field that is exposed to the extension
            PackedBeanField of = new PackedBeanField(scanner, af0.extensionType(), f, af0.isGettable() || af1.isGettable(),
                    af0.isSettable() || af1.isSettable(), annotations);

            of.extension.introspector.hookOnAnnotatedField(PackedAnnotationList.of(a0, a1), of);
        } else {

            match1(scanner, f, annotations, af0, new Annotation[] { a0 });
            match1(scanner, f, annotations, af1, new Annotation[] { a1 });
        }
    }

    private static void matchAnnotatedField(BeanScanner scanner, Field field, Annotation[] annotations, Annotation[] hooks, AnnotatedField... annotatedFields) {
        PackedBeanField of = new PackedBeanField(scanner, field, annotations, annotatedFields);
        of.extension.introspector.hookOnAnnotatedField(PackedAnnotationList.of(hooks), of);
    }

    static void matchManySameExtension(BeanScanner scanner, Field f, Annotation[] annotations, P... ps) {
//        if (af.kind() == AnnotatedFieldKind.FIELD) {
//            matchAnnotatedField(scanner, f, annotations, hooks, af);
//        } else {
//            throw new UnsupportedOperationException();
//        }
    }

    record P(AnnotatedField af, Annotation a) {}
}
