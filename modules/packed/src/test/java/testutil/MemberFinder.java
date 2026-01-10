/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package testutil;

import java.lang.StackWalker.Option;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Tools for findings members. */
public class MemberFinder {

    public static Method findMethod(String name, Class<?>... parameterTypes) {
        Class<?> c = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        try {
            return c.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public static Field findFieldOnThisClass(String name) {
        Class<?> c = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).getCallerClass();
        return findField(c, name);
    }

    public static Field findField(Class<?> declaringClass, String name) {
        try {
            return declaringClass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }
}
