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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import app.packed.bean.BeanInstallationException;
import app.packed.extension.Extension;
import internal.app.packed.bean.BeanHookModel.AnnotatedField;
import internal.app.packed.bean.BeanHookModel.AnnotatedFieldKind;

/**
 *
 */
public class BeanScannerFieldHelper {

    // Maaske vi kan en generisk en for members.. <T extends Member, R>
    static void introspectFieldForAnnotations(BeanScanner scanner, Field field) {
        Annotation[] annotations = field.getAnnotations();

        // Depending on the number of annotations on the field we do the processing a bit different
        switch (annotations.length) {
        case 0:
            return;
        case 1:
            Annotation a = annotations[0];
            AnnotatedField af = scanner.hookModel.testFieldAnnotation(annotations[0].annotationType());
            if (af != null) {
                match1(scanner, field, annotations, new FieldPair(af, a));
            }
            return;
        case 2:
            Annotation a0 = annotations[0];
            Annotation a1 = annotations[1];
            AnnotatedField af0 = scanner.hookModel.testFieldAnnotation(a0.annotationType());
            AnnotatedField af1 = scanner.hookModel.testFieldAnnotation(a1.annotationType());
            if (af0 != null) {
                FieldPair fp0 = new FieldPair(af0, a0);
                if (af1 == null) {
                    match1(scanner, field, annotations, fp0);
                } else {
                    if (af0.kind() == AnnotatedFieldKind.VARIABLE && af1.kind() == AnnotatedFieldKind.VARIABLE) {
                        throw new BeanInstallationException("Cannot use both " + a0 + " and " + a1 + " on field " + field);
                    }
                    match2(scanner, field, annotations, fp0, new FieldPair(af1, a1));
                }
            } else if (af1 != null) {
                match1(scanner, field, annotations, new FieldPair(af1, a1));
            }
            return;
        }

        boolean hasProvision = false;

        // More than 2 annotations on the field
        ArrayList<FieldPair> list = new ArrayList<>(5);
        for (int i = 0; i < annotations.length; i++) {
            Annotation annotation = annotations[i];
            AnnotatedField af = scanner.hookModel.testFieldAnnotation(annotation.annotationType());
            if (af != null) {
                if (af.kind() == AnnotatedFieldKind.VARIABLE) {
                    if (hasProvision) {
                        throw new BeanInstallationException("");
                    }
                    hasProvision = true;
                }
                list.add(new FieldPair(af, annotation));
            }
        }

        // Lets see how many interesting annotations we got
        switch (list.size()) {
        case 0:
            return;
        case 1:
            FieldPair p = list.get(0);
            match1(scanner, field, annotations, p);
            return;
        case 2:
            FieldPair p0 = list.get(0);
            FieldPair p1 = list.get(1);
            match2(scanner, field, annotations, p0, p1);
            return;
        }
        Map<Class<? extends Extension<?>>, List<FieldPair>> m = list.stream().collect(Collectors.groupingBy(e -> e.af.extensionType()));
        m.forEach((k, v) -> {
            ContributingExtension ce = scanner.computeContributor(k);
            ce.onAnnotatedField(field, annotations, v.toArray(i -> new FieldPair[i]));
        });
    }

    private static void match1(BeanScanner scanner, Field field, Annotation[] annotations, FieldPair pair) {
        ContributingExtension ce = scanner.computeContributor(pair.af.extensionType());
        ce.onAnnotatedField(field, annotations, pair);
    }

    /**
     * @param scanner
     * @param field
     * @param annotations
     * @param af0
     * @param a0
     * @param af1
     * @param a02
     */
    private static void match2(BeanScanner scanner, Field field, Annotation[] annotations, FieldPair p1, FieldPair p2) {
        if (p1.af.extensionType() != p2.af.extensionType()) {
            match1(scanner, field, annotations, p1);
            match1(scanner, field, annotations, p2);
        }
        ContributingExtension ce = scanner.computeContributor(p1.af.extensionType());
        ce.onAnnotatedField(field, annotations, p1, p2);

    }

    record FieldPair(AnnotatedField af, Annotation a) {}
}
