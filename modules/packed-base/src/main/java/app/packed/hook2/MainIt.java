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
package app.packed.hook2;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.function.Supplier;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.field.AbstractFieldOperator;
import app.packed.lifecycle.Main;

/**
 *
 */
public class MainIt {

    public void foo(AnnotatedFieldHook<Main> h) {
        // Supplier<String> s = h.accessStatic(AbstractFieldOperator.supplier(String.class));
        //
        // s.get();
        //
        // BiPredicate<Object, Object> a = h.accessStatic(AbstractFieldOperator.compareAndSet());
        //
        // while (a.test("Foo", Integer.valueOf(23))) {
        //
        // }
        //
        // BiLongPredicate aa = h.accessStatic(AbstractFieldOperator.custom(BiLongPredicate.class));
        //
        // int f = 1;
        // while (aa.test(f, 23)) {
        // f += 1;
        // }
    }

    interface BiLongPredicate {
        boolean test(long l1, long l2);
    }

    void ff(Field f) {
        Supplier<?> s = AbstractFieldOperator.supplier().accessStatic(MethodHandles.lookup(), f);

        s.get();
    }
}
