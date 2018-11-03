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
package pckd.internals.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.StringJoiner;

/** A utility class with various formatting routines. */
public final class Formatter {

    /** Cannot instantiate. */
    private Formatter() {}
    
    /**
     * Formats the specified class.
     *
     * @param clazz
     *            the class to to format
     * @return the string representation of the specified class
     */
    public static String format(Class<?> clazz) {
        // TODO check for Arrays and Anynymous classes
        String name = clazz.getCanonicalName();
        return name == null ? clazz.getName() : name;
    }

    /**
     * Returns a string with all the specified classes separated by a ','.
     *
     * @param clazz
     *            the classes to return a string representation of
     * @return the string representation
     */
    public static String format(Class<?>... clazz) {
        StringJoiner sj = new StringJoiner(", ");
        List.of(clazz).forEach(e -> sj.add(format(e)));
        return sj.toString();
    }

    public static String formatShort(Class<?>... clazz) {
        StringJoiner sj = new StringJoiner(", ");
        List.of(clazz).forEach(e -> sj.add(e.getSimpleName()));
        return sj.toString();
    }

    public static String format(Method method) {
        return format(method.getDeclaringClass()) + "#" + method.getName() + "(" + format(method.getParameterTypes()) + ")";
    }

    public static String format(Constructor<?> constructor) {
        return format(constructor.getDeclaringClass()) + "(" + format(constructor.getParameterTypes()) + ")";
    }

    /**
     * Returns a short string representation of this method such as <code>String.charAt()</code> as opposed to
     * <code>java.lang.String.charAt(int)</code>.
     *
     * @param m
     *            the method to return a string representation of
     * @return a short string representation of the specified string
     */
    public static String formatShort(Method m) {
        return m.getDeclaringClass().getSimpleName() + "#" + m.getName() + "()";
    }

    public static String formatShortWithParameters(Method m) {
        return m.getDeclaringClass().getSimpleName() + "#" + m.getName() + "(" + formatShort(m.getParameterTypes()) + ")";
    }

    public static String format(Field field) {
        return format(field.getDeclaringClass()) + "#" + field.getName();
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
