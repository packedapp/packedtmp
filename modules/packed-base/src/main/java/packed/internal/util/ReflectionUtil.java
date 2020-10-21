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
package packed.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 */
public final class ReflectionUtil {

    /** Nein Nein Nein. */
    private ReflectionUtil() {}

    @SuppressWarnings("unchecked")
    public static <T extends Executable> T copy(T executable) {
        if (executable instanceof Method) {
            Method method = (Method) executable;
            try {
                return (T) method.getDeclaringClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        } else {
            Constructor<?> constructor = (Constructor<?>) executable;
            try {
                return (T) constructor.getDeclaringClass().getDeclaredConstructor(constructor.getParameterTypes());
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static Field copy(Field field) {
        try {
            return field.getDeclaringClass().getDeclaredField(field.getName());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }
}
