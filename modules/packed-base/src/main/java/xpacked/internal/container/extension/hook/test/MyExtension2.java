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
package xpacked.internal.container.extension.hook.test;

import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.Extension;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.HookGroupBuilder;
import app.packed.hook.OnHook;
import app.packed.reflect.MethodOperator;
import app.packed.reflect.VarOperator;

/**
 *
 */
public class MyExtension2 extends Extension {

    public void foo(ComponentConfiguration cc, AXA val) {
        val.rars.readyAll(cc, MySidecar.class, (s, o) -> s.foo(o));
        System.out.println("Saa godt da");
    }

    public void foo(ComponentConfiguration cc, AnnotatedMethodHook<MyA> h) {
        // ignore
    }

    // @OnHook
    public void foo(ComponentConfiguration cc, AnnotatedFieldHook<MyA> h) throws Throwable {
        Supplier<?> ss = h.applyStatic(VarOperator.supplier());
        System.out.println(ss.get());
    }

    // /** {@inheritDoc} */
    // @Override
    // public void onPrepareContainerInstantiation(ArtifactInstantiationContext context) {
    // context().putIntoInstantiationContext(context, new MySidecar());
    // }

    public static class Agg implements HookGroupBuilder<AXA> {
        private int sum;

        private final RuntimeAccessorList<Supplier<Object>> ral = new RuntimeAccessorList<>();

        @OnHook
        public void foo(AnnotatedMethodHook<MyA> h) {
            sum += h.annotation().value();
            if (h.method().isStatic()) {
                Runnable val = h.applyStatic(MethodOperator.runnable());
                val.run();
            }
        }

        @OnHook
        public void foo(AnnotatedFieldHook<MyA> h) throws Throwable {
            sum += h.annotation().value();
            if (h.field().isStatic()) {
                Supplier<Object> val = h.applyStatic(VarOperator.supplier());
                System.out.println("VAL = " + val.get());
            }
            ral.add(h, VarOperator.supplier());
        }

        /** {@inheritDoc} */
        @Override
        public AXA build() {
            return new AXA(sum, ral);
        }
    }

    static class AXA {
        final int val;
        final RuntimeAccessorList<Supplier<Object>> rars;

        /**
         * @param val
         * @param rars
         */
        public AXA(int val, RuntimeAccessorList<Supplier<Object>> rars) {
            this.val = val;
            this.rars = rars;
        }
    }

    public static class MySidecar {
        public void foo(Supplier<Object> o) {
            System.out.println("Genius : " + o.get());
            System.out.println("Genius : " + o.get());
        }
    }
}
