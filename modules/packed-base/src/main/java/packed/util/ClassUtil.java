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

/** Various class utility methods. */
public final class ClassUtil {

    /** Cannot instantiate. */
    private ClassUtil() {}

    /**
     * Converts the specified primitive class to the corresponding Object based class. Or returns the specified class if it
     * is not a primitive class.
     *
     * @param type
     *            the class to convert
     * @return the converted class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> boxClass(Class<T> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                return (Class<T>) Boolean.class;
            } else if (type == byte.class) {
                return (Class<T>) Byte.class;
            } else if (type == char.class) {
                return (Class<T>) Character.class;
            } else if (type == double.class) {
                return (Class<T>) Double.class;
            } else if (type == float.class) {
                return (Class<T>) Float.class;
            } else if (type == int.class) {
                return (Class<T>) Integer.class;
            } else if (type == long.class) {
                return (Class<T>) Long.class;
            } else { /* if (type == short.class) */ 
                return (Class<T>) Short.class;
            }
        }
        return type;
    }

    /**
     * Converts the specified primitive wrapper class to the corresponding primitive class. Or returns the specified class
     * if it is not a primitive wrapper class.
     *
     * @param type
     *            the class to convert
     * @return the converted class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> unboxClass(Class<T> type) {
        if (type == Boolean.class) {
            return (Class<T>) boolean.class;
        } else if (type == Byte.class) {
            return (Class<T>) byte.class;
        } else if (type == Character.class) {
            return (Class<T>) char.class;
        } else if (type == Double.class) {
            return (Class<T>) double.class;
        } else if (type == Float.class) {
            return (Class<T>) float.class;
        } else if (type == Integer.class) {
            return (Class<T>) int.class;
        } else if (type == Long.class) {
            return (Class<T>) long.class;
        } else if (type == Short.class) {
            return (Class<T>) short.class;
        }
        return type;
    }
}
