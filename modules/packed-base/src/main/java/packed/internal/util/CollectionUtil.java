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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 *
 */
public class CollectionUtil {

    public static <K, V, W> Map<K, W> copyOf(Map<? extends K, ? extends V> map, Function<? super V, ? extends W> transformer) {
        HashMap<K, W> tmp = new HashMap<>();
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            tmp.put(e.getKey(), transformer.apply(e.getValue()));
        }
        return Map.copyOf(tmp);
    }
}
