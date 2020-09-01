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
package packed.internal.base.attribute;

import java.util.HashMap;
import java.util.Map;

import app.packed.base.AttributeProvide;

/**
 *
 */
public class ClassAttributes {

    private static final ClassValue<Map<String, PackedAttribute<?>>> ATR = new ClassValue<>() {

        @Override
        protected Map<String, PackedAttribute<?>> computeValue(Class<?> type) {
            // Force load of class..
            try {
                Class.forName(type.getCanonicalName(), true, type.getClassLoader());
            } catch (ClassNotFoundException e) {
                // ignore
            }
            return CV.get(type).close();
        }
    };

    public static PackedAttribute<?> find(Class<?> owner, String name) {
        Map<String, PackedAttribute<?>> m = ATR.get(owner);
        if (m != null) {
            return m.get(name);
        }
        return null;
    }

    public static PackedAttribute<?> find(AttributeProvide ap) {
        return find(ap.by(), ap.name());
    }

    public static void register(PackedAttribute<?> pa) {
        CV.get(pa.declaredBy()).add(pa);
    }

    public static void printFor(Class<?> clazz) {
        System.out.println(ATR.get(clazz));
    }

    private static final ClassValue<Tmp> CV = new ClassValue<>() {

        @Override
        protected Tmp computeValue(Class<?> type) {
            return new Tmp();
        }
    };

    static class Tmp {
        HashMap<String, PackedAttribute<?>> map = new HashMap<>();

        // Tmp is never exposed
        synchronized void add(PackedAttribute<?> pa) {
            if (map == null) {
                throw new IllegalStateException("Attribute must be registered in class initializer of " + pa.declaredBy());
            } else if (map.putIfAbsent(pa.name(), pa) != null) {
                throw new IllegalStateException("An attribute with the name '" + pa.name() + "' has already been registered");
            }
        }

        synchronized Map<String, PackedAttribute<?>> close() {
            Map<String, PackedAttribute<?>> result = Map.copyOf(map);
            map = null;
            return result;
        }

    }
}
