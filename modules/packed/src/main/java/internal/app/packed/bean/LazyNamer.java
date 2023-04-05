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
package internal.app.packed.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 *
 */
public class LazyNamer {

    @SuppressWarnings({ "rawtypes", "unchecked"})
    public static <K> Map<K, String> calculate(Collection<K> values, Function<K, String> stringifier) {
        Map<String, Object> tmp = new HashMap<>();
        ArrayList<String> collisions = new ArrayList<>();

        for (K k : values) {
            String s = stringifier.apply(k);
            tmp.compute(s, (kk, vv) -> {
                if (vv == null) {
                    return k;
                } else if (vv instanceof ArrayList l) {
                    l.add(k);
                    return vv;
                } else {
                    collisions.add(s);
                    ArrayList<K> l = new ArrayList<>();
                    l.add((K) vv);
                    l.add(k);
                    return l;
                }
            });
        }

        for (String s : collisions) {
            ArrayList<K> list = (ArrayList<K>) tmp.get(s);
            tmp.put(s, list.get(0));
            int counter = 1;
            for (int i = 1; i < list.size(); i++) {
                K k = list.get(i);
                for (;;) {
                    String nn = s + counter++;
                    if (tmp.putIfAbsent(nn, k) == null) {
                        break;
                    }
                }
            }
        }

        Map<K, String> result = new HashMap<>();
        for (Entry<String, Object> entry : tmp.entrySet()) {
            result.put((K) entry.getValue(), entry.getKey());
        }
        return Map.copyOf(result);
    }
}
