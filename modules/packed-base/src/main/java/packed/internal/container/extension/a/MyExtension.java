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
package packed.internal.container.extension.a;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.container.extension.AnnotatedFieldHook;
import app.packed.container.extension.AnnotatedMethodHook;
import app.packed.container.extension.Extension;
import app.packed.container.extension.HookApplicator;
import app.packed.container.extension.HookGroupBuilder;
import app.packed.container.extension.OnHook;
import app.packed.container.extension.OnHookGroup;
import app.packed.reflect.FieldOperator;
import app.packed.reflect.MethodOperator;

/**
 *
 */
public final class MyExtension extends Extension {

    @OnHookGroup(Agg.class)
    public void foo(ComponentConfiguration cc, AXA val) {
        System.out.println("Saa godt da");
    }

    @Override
    protected MyExtensionNode onAdded() {
        return new MyExtensionNode(context());
    }

    public void foo(ComponentConfiguration cc, AnnotatedMethodHook<MyAnnotation> h) {
        // ignore
    }

    public static class Agg implements HookGroupBuilder<AXA> {
        private int sum;
        private final ArrayList<HookApplicator<Supplier<Object>>> rar = new ArrayList<>();

        private final ArrayList<HookApplicator<Object>> methods = new ArrayList<>();

        @OnHook
        public void foo(AnnotatedMethodHook<MyAnnotation> h) {
            sum += h.annotation().value();
            methods.add(h.applicator(MethodOperator.invokeOnce()));

            if (h.method().isStatic()) {
                // System.out.println(h.applyStatic(MethodOperator.invokeOnce()));
                // Runnable val = h.applyStatic(MethodOperator.runnable());
                // val.run();
            }
        }

        @OnHook
        public void foo(AnnotatedFieldHook<MyAnnotation> h) throws Throwable {
            sum += h.annotation().value();
            if (h.field().isStatic()) {
                Supplier<Object> val = h.applyStatic(FieldOperator.supplier());
                System.out.println("VAL = " + val.get());
                System.out.println("VAL = " + val.get());
            }

            HookApplicator<Supplier<Object>> ra = h.applicator(FieldOperator.supplier());
            rar.add(ra);
        }

        /** {@inheritDoc} */
        @Override
        public AXA build() {
            return new AXA(this);
        }
    }

    static class AXA {
        final int val;
        final List<HookApplicator<Supplier<Object>>> rars;
        final List<HookApplicator<Object>> methods;

        public AXA(Agg agg) {
            this.val = agg.sum;
            this.rars = agg.rar;
            this.methods = agg.methods;
        }
    }
}
