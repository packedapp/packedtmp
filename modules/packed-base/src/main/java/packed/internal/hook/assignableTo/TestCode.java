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
package packed.internal.hook.assignableTo;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import app.packed.container.Extension;

/**
 *
 */
public class TestCode {

    public static void main(String[] args) {
        System.out.println(get(LinkedHashMap.class));
    }

    public static Set<Class<?>> get(Class<?> clazz) {
        HashSet<Class<?>> into = new HashSet<>();
        for (Class<?> current = clazz; current != Object.class; current = current.getSuperclass()) {
            putInto(into, current);
            into.add(current);
        }
        return into;
    }

    public static void putInto(HashSet<Class<?>> into, Class<?> clazz) {
        for (Class<?> cl : clazz.getInterfaces()) {
            if (into.add(cl)) {
                putInto(into, cl);
            }
        }
    }

    static class KeepIt {
        Set<Class<?>> all;
        // Unused.. (optimization)

        Map<Class<?>, Extension> used;
    }
}
