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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 *
 */
public final class ReflectionUtil {

    /** Nein Nein Nein. */
    private ReflectionUtil() {}

    public static <T> Constructor<T> copy(Constructor<T> constructor) {
        try {
            return constructor.getDeclaringClass().getDeclaredConstructor(constructor.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public static int getIndex(Parameter parameter) {
        Executable e = parameter.getDeclaringExecutable();
        if (e.getParameterCount() == 0) {
            return 0;
        }
        
        Parameter[] p = e.getParameters();
        int i = 0;
        while (!parameter.equals(p[i])) {
            i++;
        }
        return i;
    }

    public static Field copy(Field field) {
        try {
            return field.getDeclaringClass().getDeclaredField(field.getName());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Type getParameterizedType(Parameter parameter, int index) {
        // Workaround for https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8213278
        Class<?> dc = parameter.getDeclaringExecutable().getDeclaringClass();
        if (index > 0 && (dc.isLocalClass() || (dc.isMemberClass() && !Modifier.isStatic(dc.getModifiers())))) {
            return parameter.getDeclaringExecutable().getGenericParameterTypes()[index - 1];
        } else {
            return parameter.getParameterizedType();
        }
    }
}
// Method Overrides
//public boolean overrides(PackedMethodDescriptor supeer) {
//    PackedMethodDescriptor pmd = supeer;
//    if (methodOverrides(this.method, pmd.method)) {
//        if (method.getName().equals(supeer.method.getName())) {
//            return Arrays.equals(parameterTypes, pmd.parameterTypes);
//        }
//    }
//    return false;
//}
//
///**
// * Returns true if a overrides b. Assumes signatures of a and b are the same and a's declaring class is a subclass of
// * b's declaring class.
// */
//private static boolean methodOverrides(Method sub, Method supeer) {
//    int modifiers = supeer.getModifiers();
//    if (Modifier.isPrivate(modifiers)) {
//        return false;
//    }
//    return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers)
//            || sub.getDeclaringClass().getPackage().equals(supeer.getDeclaringClass().getPackage());
//}