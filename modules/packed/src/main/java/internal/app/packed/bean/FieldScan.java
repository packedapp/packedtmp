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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import app.packed.bean.BeanInstallationException;
import app.packed.extension.Extension;
import app.packed.extension.InternalExtensionException;
import internal.app.packed.bean.PackedBeanField.HookOnFieldAnnotation;
import internal.app.packed.util.PackedAnnotationList;

/**
 *
 */
class FieldScan {

    private static void checkNoBindingHooks(BeanScanner scanner, Field field, Annotation[] annotations, Annotation fieldAnnotation) {
        for (Annotation a : annotations) {
            HookOnAnnotatedBinding b = HookOnAnnotatedBinding.find(a.annotationType());
            if (b != null) {
                throw new InternalExtensionException("Nope");
            }
        }
    }

    static void introspectField(BeanScanner scanner, Field field) {
        Annotation[] annotations = field.getAnnotations();
        PackedAnnotationList pal = new PackedAnnotationList(annotations);

        switch (annotations.length) {
        case 0 -> { // fall-through
        }
        case 1 -> {
            Annotation a = annotations[0];
            HookOnFieldAnnotation hook = HookOnFieldAnnotation.find(a.annotationType());
            if (hook != null) {
                checkNoBindingHooks(scanner, field, annotations, a);
                new PackedBeanField(scanner, field, pal, pal, hook).onHook();
                return;
            }
        }
        case 2 -> {
            Annotation a0 = annotations[0];
            Annotation a1 = annotations[1];
            HookOnFieldAnnotation hook0 = HookOnFieldAnnotation.find(a0.annotationType());
            HookOnFieldAnnotation hook1 = HookOnFieldAnnotation.find(a1.annotationType());
            if (hook0 != null) {
                checkNoBindingHooks(scanner, field, annotations, a0);
                if (hook1 == null) {
                    new PackedBeanField(scanner, field, pal, new PackedAnnotationList(a0), hook0).onHook();
                } else if (hook0.extensionType() == hook1.extensionType()) {
                    new PackedBeanField(scanner, field, pal, new PackedAnnotationList(a0, a1), hook0, hook1).onHook();
                } else {
                    // TODO sort
                    new PackedBeanField(scanner, field, pal, new PackedAnnotationList(a0), hook0).onHook();
                    new PackedBeanField(scanner, field, pal, new PackedAnnotationList(a1), hook1).onHook();
                }
                return;
            } else if (hook1 != null) {
                checkNoBindingHooks(scanner, field, annotations, a1);
                new PackedBeanField(scanner, field, pal, new PackedAnnotationList(a1), hook1).onHook();
                return;
            }
        }
        default -> {
            record Pair(HookOnFieldAnnotation af, Annotation a) {}
            // I think we want deterministic order
            // But if we want that we need to look up the ExtensionSetup. Because we have speciel sort
            // for extensions with same canonical name from different class loaders
            // But wait with the sort. Maybe we have queues and then sort somewhere else
            Annotation a = null;
            Map<Class<? extends Extension<?>>, List<Pair>> map = new IdentityHashMap<>();
            for (Annotation annotation : annotations) {
                HookOnFieldAnnotation hook = HookOnFieldAnnotation.find(annotation.annotationType());
                if (hook != null) {
                    a = annotation;
                    map.computeIfAbsent(hook.extensionType(), e -> new ArrayList<>(3)).add(new Pair(hook, annotation));
                }
            }
            if (map.isEmpty()) {
                break;
            }

            checkNoBindingHooks(scanner, field, annotations, a);
            if (map.size() > 1) {
                // Annotations will always be for a single class loader so
                // never any with same canonical name
                // TODO add comparator
                map = new TreeMap<>(map);
            }
            for (List<Pair> list : map.values()) {
                HookOnFieldAnnotation[] ha = new HookOnFieldAnnotation[list.size()];
                Annotation[] hooks = new Annotation[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    Pair p = list.get(i);
                    ha[i] = p.af;
                    hooks[i] = p.a;
                }
                new PackedBeanField(scanner, field, pal, new PackedAnnotationList(hooks), ha).onHook();
            }
            return;
        }
        }

        // There were no field hook annotations, let's see if we have any binding annotations.
        HookOnAnnotatedBinding b = null;
        for (Annotation a : annotations) {
            HookOnAnnotatedBinding bb = HookOnAnnotatedBinding.find(a.annotationType());
            if (b != null) {
                throw new BeanInstallationException("Can only have 1 binding annotation");
            }
            b = bb;
        }
        if (b == null) {}

        // BeanScannerExtension e = scanner.computeContributor(b.extensionType());

        // Skal vi lave operationen med det samme?
        // Eller foerst naar vi binder?

        // Jeg tror bare vi skal lave en operation...
    }
}
