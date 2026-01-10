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
package internal.app.packed.util.accesshelper;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.WeakHashMap;

/**
 *
 */
public abstract class AccessHelper {

    private static final WeakHashMap<Class<?>, Object> INSTANCES = new WeakHashMap<>();

    protected static <T> T init(Class<T> clazz, Class<?> implementor) {
        try {
            MethodHandles.lookup().ensureInitialized(implementor);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }

        Object object = INSTANCES.get(clazz);
        requireNonNull(object);
        return clazz.cast(object);
    }

    public static synchronized void initHandler(Class<?> clazz, Object o) {
        INSTANCES.put(clazz, o);
    }

}
