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
package app.packed.inject.fac2;

import app.packed.inject.Factory;
import app.packed.inject.Key;

/**
 *
 */
public class NewFactory<T> {

    public static void main(String[] args) {
        NewFactory<String> as = fromInstance("doo").as(new Key<CharSequence>() {});

        Factory.ofInstance("String");// .as(CharSequence.class);

        // NewFactory<CharSequence> f = fromInstance("doo").as(CharSequence.class);

        // fromInstance("doo").as(new Key<@SystemProperty("hejhej") CharSequence>() {});

        Factory<String> ff = null;

        Factory<CharSequence> exposeAs = exposeAs(ff, CharSequence.class);

        System.out.println(as);
        System.out.println(exposeAs);
    }

    public <S extends T> NewFactory<S> as(Class<? super S> k) {
        throw new UnsupportedOperationException();
    }

    public <S extends T> NewFactory<S> as(Key<? super S> k) {
        throw new UnsupportedOperationException();
    }

    public static <T> NewFactory<T> fromInstance(T instance) {
        throw new UnsupportedOperationException();
    }

    public static <S, T extends S> Factory<S> exposeAs(Factory<T> f, Class<S> cl) {
        return null;
    }
}
