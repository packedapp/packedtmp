package zandbox.internal.hooks2.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor.FieldProcessor;
import zandbox.internal.hooks2.bootstrap.ClassBootstrapProcessor.MethodProcessor;

class ClassBootstrapProcessorHelper {

    static void onConstructor(ClassBootstrapProcessor.Builder classBuilder, Constructor<?> constructor) {
        // taenker vi ogsaa maa processere factories et eller andet sted
        // Ihvertfald hvis vi skal supportere hooks...
        // Hvis det er en method kan jo vi jo bare direkte kalde onMethod taenker jeg? idk.
    }

    static void scanFieldForAnnotations(ClassBootstrapProcessor.Builder builder, Field field) {
        // We scan all annotations before we start any bootstrap process
        Annotation[] annotations = field.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            AbstractBootstrapModel h1 = builder.repository.lookupFieldAnnotation(a1.annotationType());
            if (h1 != null) {
                // See if we have more than 1 hook
                for (int j = i + 1; j < annotations.length; j++) {
                    Annotation a2 = annotations[j];
                    AbstractBootstrapModel h2 = builder.repository.lookupFieldAnnotation(a2.annotationType());
                    if (h2 != null) {
                        // We need to keep scanning, just in case we have more than 2 hooks
                        List<Map.Entry<Annotation, AbstractBootstrapModel>> moreThan2 = null;
                        for (int k = j + 1; k < annotations.length; k++) {
                            Annotation a3 = annotations[j];
                            AbstractBootstrapModel h3 = builder.repository.lookupFieldAnnotation(a3.annotationType());
                            if (h3 != null) {
                                if (moreThan2 == null) {
                                    moreThan2 = new ArrayList<>(3);
                                    moreThan2.add(Map.entry(a1, h1));
                                    moreThan2.add(Map.entry(a2, h2));
                                }
                                moreThan2.add(Map.entry(a3, h3));
                            }
                        }
                        if (moreThan2 == null) {
                            scanFieldAnnotations2(builder, field, a1, h1, a2, h2);
                        } else {
                            scanFieldAnnotationsMany(builder, field, moreThan2);
                        }
                        return;
                    }
                }
                // Found a single hook
                h1.bootstrapField(builder.newFieldProcessor(field));
                return;
            }
        }
    }

    private static void scanFieldAnnotations2(ClassBootstrapProcessor.Builder builder, Field field, Annotation a1, AbstractBootstrapModel h1, Annotation a2,
            AbstractBootstrapModel h2) {
        // Multiple hooked annotations
        // Might fail if the hooks are not compatible.. after some rules
        FieldProcessor fb = builder.newFieldProcessor(field);
        if (h1 == h2) {
            h1.bootstrapField(fb);
        } else {
            if (h1 instanceof InjectableVariableBootstrapModel i1) {
                
            }
            
            h1.bootstrapField(fb);
            h2.bootstrapField(fb);
        }

    }

    private static void scanFieldAnnotationsMany(ClassBootstrapProcessor.Builder builder, Field field,
            List<Map.Entry<Annotation, AbstractBootstrapModel>> list) {
        throw new UnsupportedOperationException();
    }

    static void scanMethodForAnnotations(ClassBootstrapProcessor.Builder builder, Method method) {
        // We scan all annotations before we start any bootstrap process
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a1 = annotations[i];
            AbstractBootstrapModel h1 = builder.repository.lookupMethodAnnotation(a1.annotationType());
            if (h1 != null) {
                // See if we have more than 1 hook
                for (int j = i + 1; j < annotations.length; j++) {
                    Annotation a2 = annotations[j];
                    AbstractBootstrapModel h2 = builder.repository.lookupMethodAnnotation(a2.annotationType());
                    if (h2 != null) {
                        // We need to keep scanning, just in case we have more than 2 hooks
                        List<Map.Entry<Annotation, AbstractBootstrapModel>> moreThan2 = null;
                        for (int k = j + 1; k < annotations.length; k++) {
                            Annotation a3 = annotations[j];
                            AbstractBootstrapModel h3 = builder.repository.lookupMethodAnnotation(a3.annotationType());
                            if (h3 != null) {
                                if (moreThan2 == null) {
                                    moreThan2 = new ArrayList<>(3);
                                    moreThan2.add(Map.entry(a1, h1));
                                    moreThan2.add(Map.entry(a2, h2));
                                }
                                moreThan2.add(Map.entry(a3, h3));
                            }
                        }
                        if (moreThan2 == null) {
                            scanMethodAnnotations2(builder, method, a1, h1, a2, h2);
                        } else {
                            scanMethodAnnotationsMany(builder, method, moreThan2);
                        }
                        return;
                    }
                }
                // Found a single hook
                h1.bootstrapMethod(builder.newMethodProcessor(method));
                return;
            }
        }
    }

    private static void scanMethodAnnotations2(ClassBootstrapProcessor.Builder builder, Method method, Annotation a1, AbstractBootstrapModel h1, Annotation a2,
            AbstractBootstrapModel h2) {
        // Multiple hooked annotations
        // Might fail if the hooks are not compatible.. after some rules
        MethodProcessor fb = builder.newMethodProcessor(method);
        h1.bootstrapMethod(fb);
        h2.bootstrapMethod(fb);
    }

    private static void scanMethodAnnotationsMany(ClassBootstrapProcessor.Builder builder, Method method,
            List<Map.Entry<Annotation, AbstractBootstrapModel>> list) {
        throw new UnsupportedOperationException();
    }
}
