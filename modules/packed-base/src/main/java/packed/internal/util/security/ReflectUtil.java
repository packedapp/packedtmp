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
package packed.internal.util.security;

/**
 *
 */
public class ReflectUtil {

    /**
     * Tests whether two classes are in the same package.
     * 
     * @param c1
     *            the first class
     * @param c2
     *            the second class.
     * @return true if the two classes are in the same package, otherwise null
     */
    public static boolean isClassesInSamePackage(Class<?> c1, Class<?> c2) {
        return c1.getClassLoader() == c2.getClassLoader() && c1.getPackageName().equals(c2.getPackageName());
    }

    public static boolean isSuperInterfaceOf(Class<?> clazz, Class<?> interfaze) {
        for (Class<?> i : clazz.getInterfaces()) {
            if (i == interfaze || isSuperInterfaceOf(i, interfaze)) {
                return true;
            }
        }
        return false;
    }
}
