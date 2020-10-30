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
package app.packed.inject.sandbox;

import app.packed.base.Named;
import app.packed.bundle.BaseAssembly;
import app.packed.component.App;
import app.packed.inject.Factory;
import app.packed.inject.Factory2;

/**
 *
 */
class Stuff extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        Factory<String> f = new Factory2<Long, Long, String>((l, i) -> "ffoo " + l + " " + i) {};
        Factory<String> ff = new Factory2<Long, Long, @Named("foo") String>((l, i) -> "ffoo " + l + " " + i) {};
        f = f.postConstruction(s -> System.out.println(s));
        f = f.postConstruction(s -> System.out.println(s));
        f = f.bind(3333L);
        f = f.bind(2343L);
        System.out.println();
        ff = ff.postConstruction(s -> System.out.println(s));
        install(f);
        install(ff);
        provideInstance(-123123);
        providePrototype(new Factory<Long>(System::nanoTime) {});
    }

    public static void main(String[] args) {
        App.of(new Stuff());
    }
}
