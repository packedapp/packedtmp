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
package internal.app.packed.util;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.StringJoiner;

/** A utility class with various formatting routines. */
public final class StringFormatter {

    /** Cannot instantiate. */
    private StringFormatter() {}

    /**
     * Creates a short string representation of the specified type. Basically this method uses {@link Class#getSimpleName()}
     * instead of {@link Class#getCanonicalName()}. Which results in short string such as {@code List<String>} instead of
     * {@code java.util.List<java.lang.String>}.
     * 
     * @param type
     *            the type to create a short representation of
     * @return the representation
     */
    public static String formatSimple(Type type) {
        StringBuilder sb = new StringBuilder();
        formatSimple(type, sb);
        return sb.toString();
    }

    /**
     * Helper method for {@link #formatSimple(Type)}.
     * 
     * @param type
     *            the type to process
     * @param sb
     *            the string builder
     */
    private static void formatSimple(Type type, StringBuilder sb) {
        requireNonNull(type, "type is null");
        if (type instanceof Class<?> cl) {
            sb.append(cl.getSimpleName());
        } else if (type instanceof ParameterizedType pt) {
            formatSimple(pt.getRawType(), sb);
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            // The array can be empty according to #ParameterizedType.getActualTypeArguments()
            if (actualTypeArguments.length > 0) {
                sb.append("<");
                formatSimple(actualTypeArguments[0], sb);
                for (int i = 1; i < actualTypeArguments.length; i++) {
                    sb.append(", ");
                    formatSimple(actualTypeArguments[i], sb);
                }
                sb.append(">");
            }
        } else if (type instanceof GenericArrayType gat) {
            formatSimple(gat.getGenericComponentType(), sb);
            sb.append("[]");
        } else if (type instanceof TypeVariable<?> tv) {
            sb.append(tv.getName());
        } else if (type instanceof WildcardType wt) {
            Type[] lowerBounds = wt.getLowerBounds();
            if (lowerBounds.length == 1) {
                sb.append("? super ");
                formatSimple(lowerBounds[0], sb);
            } else {
                Type[] upperBounds = wt.getUpperBounds();
                if (upperBounds[0] == Object.class) {
                    sb.append("?");
                } else {
                    sb.append("? extends ");
                    formatSimple(upperBounds[0], sb);
                }
            }
        } else {
            throw new IllegalArgumentException("Don't know how to process type '" + type + "' of type: " + type.getClass().getName());
        }
    }

    /**
     * Formats the specified class.
     *
     * @param type
     *            the type to format
     * @return the string representation of the specified class
     */
    public static String format(Type type) {
        if (!(type instanceof Class)) {
            return type.toString();
        }
        int dimensions = 0;
        Class<?> c = (Class<?>) type;
        while (c.isArray()) {
            dimensions++;
            c = c.getComponentType();
        }
        String s = c.getName();
        if (dimensions > 0) {
            s += "[]".repeat(dimensions);
        }
        return s;
    }

    /**
     * Returns a string with all the specified classes separated by a ','.
     *
     * @param classes
     *            the classes to return a string representation of
     * @return the string representation
     */
    public static String format(Class<?>... classes) {
        StringJoiner sj = new StringJoiner(", ");
        List.of(classes).forEach(e -> sj.add(format(e)));
        return sj.toString();
    }

    public static String format(Constructor<?> constructor) {
        return format(constructor.getDeclaringClass()) + "(" + format(constructor.getParameterTypes()) + ")";
    }

    public static String format(Annotation annotation) {
        return annotation.toString();
    }

    public static String formatSimple(Annotation annotation) {
        // TODO fix for inner qualifier classes
        Class<? extends Annotation> annotationType = annotation.annotationType();
        return annotation.toString().replace(annotationType.getPackageName() + ".", "");
    }

    public static String format(Field field) {
        return format(field.getDeclaringClass()) + "#" + field.getName();
    }

    public static String format(Method method) {
        return format(method.getDeclaringClass()) + "#" + method.getName() + "(" + format(method.getParameterTypes()) + ")";
    }

    public static String formatShortParameters(Executable e) {
        return "(" + formatSimple(e.getParameterTypes()) + ")";
    }

    public static String formatShortWithParameters(Method m) {
        return m.getDeclaringClass().getSimpleName() + "#" + m.getName() + "(" + formatSimple(m.getParameterTypes()) + ")";
    }

    public static String formatSimple(Class<?>... classes) {
        StringJoiner sj = new StringJoiner(", ");
        List.of(classes).forEach(e -> sj.add(e.getSimpleName()));
        return sj.toString();
    }

    /**
     * Returns a short string representation of this method such as <code>String.charAt()</code> as opposed to
     * <code>java.lang.String.charAt(int)</code>.
     *
     * @param m
     *            the method to return a string representation of
     * @return a short string representation of the specified string
     */
    public static String formatSimple(Method m) {
        return m.getDeclaringClass().getSimpleName() + "#" + m.getName() + "()";
    }

    public static String formatSimple(Constructor<?> c) {
        return format(c.getDeclaringClass()) + formatShortParameters(c);
    }
}

// static Comparator<Class<?>> COMPARATOR_BY_NAME = new Comparator<Class<?>>() {
// @Override
// public int compare(Class<?> o1, Class<?> o2) {
// return o1.getName().compareTo(o2.getName());
// }
// };
//
//
/// **
// * Returns a string with all the specified classes separated by a ','.
// *
// * @param iterable
// * an iterable with classes
// * @return the string representation
// */
// private static String formatClass(Iterable<Class<?>> iterable) {
// StringBuilder sb = new StringBuilder();
// Iterator<Class<?>> iter = iterable.iterator();
// if (iter.hasNext()) {
// sb.append(formatClass(iter.next()));
// while (iter.hasNext()) {
// sb.append(", ").append(formatClass(iter.next()));
// }
// }
// return sb.toString();
// }
//
// private static String formatSortedClass(Iterable<Class<?>> iterable) {
// ArrayList<Class<?>> list = new ArrayList<>();
// iterable.forEach(c -> list.add(c));
// list.sort(COMPARATOR_BY_NAME);
// return formatClass(list);
// }
