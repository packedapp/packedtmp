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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.container.Extension;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.FieldOperator;
import app.packed.hook.OnHook;
import app.packed.hook.OnHookAggregateBuilder;
import app.packed.hook.RuntimeAccessor;

/**
 *
 */
public class MyExtension extends Extension {

    @OnHook(Agg.class)
    public void foo(ComponentConfiguration cc, AXA val) {
        System.out.println(cc.path());
        System.out.println(val.rars.size());
        for (RuntimeAccessor<Object> ra : val.rars) {
            ra.onReady(cc, MySidecar.class, (s, o) -> {
                s.foo(o);
            });
        }
        System.out.println("Saa godt da");
    }

    public void foo(ComponentConfiguration cc, AnnotatedMethodHook<MyA> h) {
        // ignore
    }

    // @OnHook
    public void foo(ComponentConfiguration cc, AnnotatedFieldHook<MyA> h) throws Throwable {
        Supplier<?> ss = h.accessStatic(FieldOperator.supplier());
        System.out.println(ss.get());
    }

    /** {@inheritDoc} */
    @Override
    public void onPrepareContainerInstantiate(ArtifactInstantiationContext context) {
        context.put(configuration(), new MySidecar());
    }

    public void fodddo(ComponentConfiguration cc, AnnotatedFieldHook<MyA> h) throws Throwable {
        h.checkStatic();
        System.out.println(cc.path());
        System.out.println(h.field());
        System.out.println();
        System.out.println("----   " + h.newGetter().invoke());
        System.out.println("----   " + h.newSetter().invoke("flll"));
        System.out.println("----   " + h.newGetter().invoke());
    }

    public static class Agg implements OnHookAggregateBuilder<AXA> {
        private int sum;
        private final ArrayList<RuntimeAccessor<Object>> rar = new ArrayList<>();

        public void foo(AnnotatedMethodHook<MyA> h) {
            sum += h.annotation().value();
        }

        public void foo(AnnotatedFieldHook<MyA> h) throws Throwable {
            sum += h.annotation().value();
            if (h.field().isStatic()) {
                Supplier<Object> val = h.accessStatic(FieldOperator.supplier());
                System.out.println("VAL = " + val.get());
                System.out.println("VAL = " + val.get());
            }

            RuntimeAccessor<Object> ra = h.accessAtRuntime(FieldOperator.getOnce());
            rar.add(ra);
        }

        /** {@inheritDoc} */
        @Override
        public AXA build() {
            return new AXA(sum, rar);
        }
    }

    static class AXA {
        final int val;
        final List<RuntimeAccessor<Object>> rars;

        /**
         * @param val
         * @param rars
         */
        public AXA(int val, List<RuntimeAccessor<Object>> rars) {
            this.val = val;
            this.rars = rars;
        }
    }

    public static class MySidecar {
        public void foo(Object o) {
            System.out.println("Genius : " + o);
        }
    }
}
