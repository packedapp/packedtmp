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
package packed.internal.inject.test;

import app.packed.application.Program;
import app.packed.base.Key;
import app.packed.base.Tag;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import app.packed.service.ServiceLocator;
import packed.internal.inject.test.FooBar.NeedsString;

/**
 *
 */
class Xxx extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        use(MyEx.class);
        provideInstance("FooBar").export();
        provide(new Factory<Long>(System::currentTimeMillis) {}).export();
        service().transformExports(c -> {
            c.decorate(Long.class, l -> -l);
        });
    }

    public static void main(String[] args) {
        Program a = Program.start(new Xxx());
        System.out.println(a.services().keys());
        ServiceLocator l = a.services().spawn(s -> {
            s.provideInstance(123);
            s.decorate(String.class, t -> t + t);
            s.decorate(String.class, t -> t + t);
            s.decorate(String.class, t -> t + t);
            s.rekey(Key.of(Long.class), new Key<@Tag("foo") Long>() {});
        });
        System.out.println(l.keys());
        System.out.println(l.use(Integer.class));
        System.out.println(l.use(String.class));
        System.out.println("---");
        l.selectAll().forEachInstance(i -> System.out.println(i));
        System.out.println("---");
        System.out.println("Bye");
    }

    public static class MyEx extends Extension {
        MyEx() {}
    }

    static class FooAssembly extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            provide(NeedsString.class);
        }
    }

}
