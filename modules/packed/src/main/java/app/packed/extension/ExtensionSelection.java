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
package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import app.packed.bean.BeanMirror;

/**
 *
 */
// Maaske T extends Extension | T extends ExtensionBean... 
// Saa kan vi ogsaa bruge den paa runtime

// Den bliver vel noedt til at vaere live...
public interface ExtensionSelection<T extends Extension> extends Iterable<T> {

    // Ideen er at man kan faa saadan en injected ind i et mirror...

    default int intSum(ToIntFunction<? super T> mapper) {
        requireNonNull(mapper, "mapper is null");
        int result = 0;
        for (T t : this) {
            int tmp = mapper.applyAsInt(t);
            result = Math.addExact(result, tmp);
        }
        return result;
    }
    
    default <E> List<E> listCollect(BiConsumer<T, List<E>> action) {
        requireNonNull(action, "action is null");
        ArrayList<E> result = new ArrayList<>();
        for (T t : this) {
            action.accept(t, result);
        }
        return result;
    }

    default long longSum(ToLongFunction<? super T> mapper) {
        requireNonNull(mapper, "mapper is null");
        long result = 0;
        for (T t : this) {
            long tmp = mapper.applyAsLong(t);
            result = Math.addExact(result, tmp);
        }
        return result;
    }
}

class MyExtMirror {
    final ExtensionSelection<TestExtension> es;

    MyExtMirror(ExtensionSelection<TestExtension> es) {
        this.es = es;
    }

    public int beanCount() {
        return es.intSum(e -> e.count());
    }

    public Collection<BeanMirror> beans() {
        return es.listCollect((e, c) -> c.addAll(e.beans()));
    }
}

class TestExtension extends Extension {

    Collection<BeanMirror> beans() {
        return List.of();
    }

    int count() {
        return 3;
    }
}