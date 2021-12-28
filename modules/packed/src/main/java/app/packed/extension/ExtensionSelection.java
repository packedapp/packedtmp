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
public interface ExtensionSelection<T extends Extension> extends Iterable<T> {

    // Ideen er at man kan faa saadan en injected ind i et mirror...

    int intSum(ToIntFunction<? super T> mapper);

    long longSum(ToLongFunction<? super T> mapper);

    <E> Collection<E> collectionCollect(BiConsumer<T, Collection<E>> action);
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
        return es.collectionCollect((e, c) -> c.addAll(e.beans()));
    }
}

class TestExtension extends Extension{
    
    int count() {
        return 3;
    }
    
    Collection<BeanMirror> beans() {
        return List.of();
    }
}