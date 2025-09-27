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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import app.packed.bean.BeanInstallationException;
import internal.app.packed.bean.scanning.BeanTriggerModel.FieldCache;
import internal.app.packed.bean.scanning.BeanTriggerModel.OnAnnotatedFieldCache;
import internal.app.packed.bean.scanning.BeanTriggerModel.OnAnnotatedVariableCache;
import internal.app.packed.util.PackedAnnotationList;

/** Scans fields on a bean. */
class BeanScannerOnFields {

    private static void failOnIllegalVariable(Field f, List<FieldCache> fc) {
        // Multiple
        throw new BeanInstallationException("OOPS");
    }

    static void fiendIntrospect(BeanScanner scanner, Class<?> clazzToScan) {
        // We never process classes in the "java.base" module.
        if (clazzToScan.getModule() != BeanScanner.JAVA_BASE_MODULE) {
            // Recursively call into superclass, before processing own fields
            fiendIntrospect(scanner, clazzToScan.getSuperclass());

            // Iterate over all declared fields
            for (Field field : clazzToScan.getDeclaredFields()) {
                Annotation[] annotations = field.getAnnotations();
                switch (annotations.length) {
                case 0 -> {
                }
                case 1 -> fieldIntrospect1(scanner, field, annotations);
                case 2 -> fieldIntrospect2(scanner, field, annotations);
                default -> fieldIntrospectN(scanner, field, annotations);
                }
            }
        }
    }

    private static void fieldIntrospect1(BeanScanner scanner, Field field, Annotation[] annotations) {
        Annotation a = annotations[0];

        FieldCache fc = scanner.triggerModel.testField(a.annotationType());
        if (fc == null) {
            return;
        }
        PackedAnnotationList pal = new PackedAnnotationList(annotations);
        fc.handleOne(scanner, field, pal, pal);
    }

    private static void fieldIntrospect2(BeanScanner scanner, Field field, Annotation[] annotations) {
        Annotation a0 = annotations[0];
        Annotation a1 = annotations[1];

        FieldCache fc0 = scanner.triggerModel.testField(a0.annotationType());
        FieldCache fc1 = scanner.triggerModel.testField(a1.annotationType());
        if (fc0 == null && fc1 == null) {
            // do nothing
        } else if (fc0 == null) {
            fc1.handleOne(scanner, field, new PackedAnnotationList(annotations), new PackedAnnotationList(a1));
        } else if (fc1 == null) {
            fc0.handleOne(scanner, field, new PackedAnnotationList(annotations), new PackedAnnotationList(a0));
        } else {
            fieldIntrospect2(scanner, field, annotations, a0, fc0, a1, fc1);
        }
    }

    private static void fieldIntrospect2(BeanScanner scanner, Field field, Annotation[] annotations, Annotation a0, FieldCache fc0, Annotation a1,
            FieldCache fc1) {
        // Let fieldIntrospectN handle this error scenario
        if (fc0 instanceof OnAnnotatedVariableCache || fc1 instanceof OnAnnotatedVariableCache) {
            failOnIllegalVariable(field, List.of(fc0, fc1));
            fieldIntrospectN(scanner, field, annotations);
        }

        OnAnnotatedFieldCache afc0 = (OnAnnotatedFieldCache) fc0;
        OnAnnotatedFieldCache afc1 = (OnAnnotatedFieldCache) fc1;
        PackedAnnotationList pal = new PackedAnnotationList(annotations);

        // Test if we are using the same introspector
        if (afc0.bim() == afc1.bim()) {
            IntrospectorOnField.process(scanner.introspector(afc0.bim()), field, pal, new PackedAnnotationList(a0, a1), afc0, afc1);
        } else {
            afc0.handleOne(scanner, field, pal, new PackedAnnotationList(a0));
            afc1.handleOne(scanner, field, pal, new PackedAnnotationList(a1));
        }
    }

    private static void fieldIntrospectN(BeanScanner scanner, Field field, Annotation[] annotations) {
        record Pair(FieldCache af, Annotation a) {}
        ArrayList<Pair> l = new ArrayList<>();
        for (Annotation a : annotations) {
            FieldCache fc = scanner.triggerModel.testField(a.annotationType());
            if (fc != null) {
                l.add(new Pair(fc, a));
            }
        }

        int size = l.size();
        if (size == 0) {
            // ignore
        } else if (size == 1) {
            Pair p = l.get(0);
            p.af.handleOne(scanner, field, new PackedAnnotationList(annotations), new PackedAnnotationList(p.a));
        } else if (size == 2) {
            Pair p0 = l.get(0);
            Pair p1 = l.get(1);
            fieldIntrospect2(scanner, field, annotations, p0.a, p0.af, p1.a, p1.af);
        } else {
            // 3 or more triggering annotations, this should be fairly theoretically

//          // I think we want deterministic order
//          // But if we want that we need to look up the ExtensionSetup. Because we have speciel sort
//          // for extensions with same canonical name from different class loaders
//          // But wait with the sort. Maybe we have queues and then sort somewhere else
            Map<BeanIntrospectorClassModel, List<Pair>> map = new IdentityHashMap<>();
            for (Pair p : l) {
                if (p.af instanceof OnAnnotatedVariableCache) {
                    failOnIllegalVariable(field, l.stream().map(z -> z.af).toList());
                }
                map.computeIfAbsent(p.af.bim(), _ -> new ArrayList<>(3)).add(p);
            }

            PackedAnnotationList pal = new PackedAnnotationList(annotations);
            map.forEach((bim, pList) -> {
                Annotation[] triggers = pList.stream().map(p -> p.a).toArray(i -> new Annotation[i]);
                OnAnnotatedFieldCache[] fields = pList.stream().map(p -> (OnAnnotatedFieldCache) p.af).toArray(i -> new OnAnnotatedFieldCache[i]);
                IntrospectorOnField.process(scanner.introspector(bim), field, pal, new PackedAnnotationList(triggers), fields);
            });
        }
    }


}
