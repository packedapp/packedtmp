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
package packed.util;

import static packed.util.Formatter.format;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class AnnotationUtil {

    public static <T> Class<T> validateRetentionPolicy(Class<T> c, RetentionPolicy policy) {
        Retention r = c.getAnnotation(Retention.class);
        if (r == null) {
            throw new IllegalArgumentException("The annotation type @" + c.getSimpleName()
                    + " must have a runtime retention policy (@Retention(RetentionPolicy." + policy + "), but did not have any retention policy");
        } else if (r.value() != policy) {
            throw new IllegalArgumentException("The annotation type @" + c.getSimpleName()
                    + " must have runtime retention policy (@Retention(RetentionPolicy.RUNTIME), but was " + r.value());
        }
        return c;
    }

    /** A cache of generated annotations. */
    private static final ClassValue<Annotation> CACHE = new ClassValue<>() {
        @SuppressWarnings("unchecked")
        @Override
        public Annotation computeValue(Class<?> type) {
            if (!isAllDefaultMethods((Class<? extends Annotation>) type)) {
                throw new IllegalArgumentException("Cannot generate annotations for classes that have non-default value methods, class = " + format(type));
            }
            return generateAnnotationImpl((Class<? extends Annotation>) type);
        }
    };

    /**
     * Implements {@link Annotation#equals}.
     *
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static boolean annotationEquals(Class<? extends Annotation> type, Map<String, Object> methods, Object other)
            throws IllegalAccessException, InvocationTargetException {
        if (!type.isInstance(other)) {
            return false;
        }
        for (Method method : type.getDeclaredMethods()) {
            String name = method.getName();
            if (!Arrays.deepEquals(new Object[] { method.invoke(other) }, new Object[] { methods.get(name) })) {
                return false;
            }
        }
        return true;
    }

    /** Implements {@link Annotation#hashCode}. */
    private static int annotationHashCode(Class<? extends Annotation> type, Map<String, Object> members) {
        int result = 0;
        for (Method method : type.getDeclaredMethods()) {
            String name = method.getName();
            Object value = members.get(name);
            result += 127 * name.hashCode() ^ Arrays.deepHashCode(new Object[] { value }) - 31;
        }
        return result;
    }

    /** Implements {@link Annotation#toString}. */
    private static String annotationToString(Class<? extends Annotation> type, Map<String, Object> members) throws Exception {
        StringBuilder sb = new StringBuilder().append("@").append(type.getName()).append("(");
        for (Iterator<Map.Entry<String, Object>> iterator = members.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Object> e = iterator.next();
            sb.append(e.getKey());
            sb.append(" = ");
            String s = Arrays.deepToString(new Object[] { e.getValue() });
            sb.append(s.substring(1, s.length() - 1));
            if (iterator.hasNext()) {
                sb.append(", ");
            }

        }
        return sb.append(")").toString();
    }

    /**
     * Generates an Annotation for the annotation class. Requires that the annotation is all optionals.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T generateAnnotation(Class<T> annotationType) {
        return (T) CACHE.get(annotationType);
    }

    static <T extends Annotation> T generateAnnotationImpl(Class<? extends T> annotationType) {
        HashMap<String, Object> methods = new HashMap<>();
        for (Method method : annotationType.getDeclaredMethods()) {
            methods.put(method.getName(), method.getDefaultValue());
        }
        return annotationType.cast(Proxy.newProxyInstance(annotationType.getClassLoader(), new Class<?>[] { annotationType }, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                String name = method.getName();
                if (name.equals("annotationType")) {
                    return annotationType;
                } else if (name.equals("toString")) {
                    return annotationToString(annotationType, methods);
                } else if (name.equals("hashCode")) {
                    return annotationHashCode(annotationType, methods);
                } else if (name.equals("equals")) {
                    return annotationEquals(annotationType, methods, args[0]);
                } else {
                    return methods.get(name);
                }
            }
        }));
    }

    public static boolean isAllDefaultMethods(Class<? extends Annotation> annotationType) {
        boolean hasMethods = false;
        for (Method m : annotationType.getDeclaredMethods()) {
            hasMethods = true;
            if (m.getDefaultValue() == null) {
                return false;
            }
        }
        return hasMethods;
    }
}
