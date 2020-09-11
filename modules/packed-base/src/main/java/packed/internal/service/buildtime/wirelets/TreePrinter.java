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
package packed.internal.service.buildtime.wirelets;

import java.util.Collection;
import java.util.function.Function;

/**
 *
 */
public class TreePrinter {

    public static <T> void print(T root, Function<T, Collection<T>> childMapper, String indent, Function<? super T, String> consumer) {
        print(0, root, childMapper, indent, consumer);
    }

    static <T> void print(int indent, T root, Function<T, Collection<T>> childMapper, String indentStr, Function<? super T, String> consumer) {
        System.out.println(indentStr.repeat(indent) + consumer.apply(root));
        Collection<T> c = childMapper.apply(root);
        if (c != null) {
            for (T t : c) {
                print(indent + 1, t, childMapper, indentStr, consumer);
            }
        }
    }
}
