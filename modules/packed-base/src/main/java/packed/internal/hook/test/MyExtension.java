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
package packed.internal.hook.test;

import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;

/**
 *
 */
public class MyExtension extends Extension {

    // @OnHook(aggreateWith = Agg.class)
    public void foo(ComponentConfiguration cc, Integer val) {
        System.out.println(cc.path());
        System.out.println(val);
    }

    @OnHook
    public void foo(ComponentConfiguration cc, AnnotatedMethodHook<MyA> h) {
        // ignore
    }

    @OnHook
    public void foo(ComponentConfiguration cc, AnnotatedFieldHook<MyA> h) throws Throwable {
        Supplier<?> ss = h.newGetAccessor(cc);
        System.out.println(ss.get());
    }

    public void fodddo(ComponentConfiguration cc, AnnotatedFieldHook<MyA> h) throws Throwable {
        h.checkStatic();
        System.out.println(cc.path());
        System.out.println(h.field());
        System.out.println();
        System.out.println("----   " + h.newMethodHandleGetter().invoke());
        System.out.println("----   " + h.newMethodHandleSetter().invoke("flll"));
        System.out.println("----   " + h.newMethodHandleGetter().invoke());
    }

    public static class Agg implements Supplier<Integer> {
        private int sum;

        @OnHook
        public void foo(AnnotatedMethodHook<MyA> h) {
            sum += h.annotation().value();
        }

        @OnHook
        public void foo(AnnotatedFieldHook<MyA> h) throws Throwable {
            sum += h.annotation().value();
        }

        /** {@inheritDoc} */
        @Override
        public Integer get() {
            return sum;
        }
    }
}
