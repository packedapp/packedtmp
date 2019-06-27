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
package app.packed.container;

import java.util.IdentityHashMap;

/**
 * Like build context. But for instantiation....
 */
public class InstantiationContext {

    private IdentityHashMap<ContainerConfiguration, IdentityHashMap<Class<?>, Object>> m = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(ContainerConfiguration dcc, Class<T> type) {
        var e = m.get(dcc);
        if (e == null) {
            throw new IllegalStateException();
        }
        Object o = e.get(type);
        if (o == null) {
            throw new IllegalStateException();
        }
        return (T) o;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrNull(ContainerConfiguration dcc, Class<T> type) {
        var e = m.get(dcc);
        if (e == null) {
            return null;
        }
        Object o = e.get(type);
        if (o == null) {
            return null;
        }
        return (T) o;
    }

    public void put(ContainerConfiguration dcc, Object o) {
        m.computeIfAbsent(dcc, e -> new IdentityHashMap<>()).put(o.getClass(), o);
    }
}
