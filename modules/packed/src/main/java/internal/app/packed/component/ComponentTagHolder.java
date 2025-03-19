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
package internal.app.packed.component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public final class ComponentTagHolder {

    final HashMap<ComponentSetup, Set<String>> map = new HashMap<>();

    public void addComponentTags(ComponentSetup cs, String... tags) {
        map.compute(cs, (_, v) -> {
            if (v == null) {
                return Set.of(tags);
            } else {
                return copyAndAdd(v, tags);
            }
        });
    }

    public Set<String> tags(ComponentSetup component) {
        Set<String> s = map.get(component);
        return s == null ? Set.of() : s;
    }

    public static Set<String> copyAndAdd(Set<String> set, String tag) {
        if (set.size() == 0) {
            return Set.of(tag);
        }
        if (set.contains(tag)) {
            return set;
        }
        Set<String> newSet = new HashSet<>(set);
        newSet.add(tag);
        return Set.copyOf(newSet);

//        Iterator<String> it = set.iterator();
//        return size <= 10 ? switch (size) {
//        case 0 -> Set.of();
//        case 1 -> Set.of(it.next());
//        case 2 -> Set.of(it.next(), it.next());
//        case 3 -> Set.of(it.next(), it.next(), it.next());
//        case 4 -> Set.of(it.next(), it.next(), it.next(), it.next());
//        default ->{
//            Set<String> newSet = new HashSet<>(set);
//            newSet.add(tag);
//            yield newSet;
//        }};
//
//        switch (set.size()) {
//        case 0:
//            return Set.of(tag);
//        case 1:
//            return Set.of(set.iterator().next(), tag);
//        case 2:
//            return Set.of(it.next(), it.next(), tag);
//        default:
//            Set<String> newSet = new HashSet<>(set);
//            newSet.add(tag);
//            return newSet;
//        }
    }

    public static Set<String> copyAndAdd(Set<String> set, String... tags) {
        Set<String> result = set;
        for (String tag : tags) {
            set = copyAndAdd(result, tag);
        }
        return result;
    }
}
