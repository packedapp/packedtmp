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

import java.util.function.Consumer;

import app.packed.component.ComponentConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;
import app.packed.hook.AggregatedHookMember;
import app.packed.lifecycle.Main;
import app.packed.util.FieldMapper;

/**
 *
 */
public class TestIt {

    @OnHook
    public void foo(AnnotatedMethodHook<Main> h) {
        // Vi kan jo toptune den aggregator....
        PreparedLambda<Runnable> main = h.newRunnable();
        System.out.println(main);
    }

    @OnHook
    public void foo(AnnotatedFieldHook<Main> h, ComponentConfiguration cc) {
        h.accessor(cc, FieldMapper.get(String.class), LifecycleSidecar.class, (s, t) -> s.listenerInstance(cc, t));
    }

    @OnHook
    public void foox(AnnotatedFieldHook<Main> h, ComponentConfiguration cc) {
        h.accessor(cc, FieldMapper.get(String.class), LifecycleSidecar.class, (s, t) -> s.listenerInstance(cc, t));
    }

    @OnHook
    public void foox(ContainerConfiguration cc, AnnotatedFieldHook<Main> h) {
        AggregatedHookMember<String> f = h.accessor(FieldMapper.get(String.class));

        f.process(cc, LifecycleSidecar.class, (s, t) -> s.listenerInstance(cc, t));
    }

    @OnHook
    public void foo(Consumer<Number> c2, Consumer<? super Number> c3) {
        Integer s = 123;
        c2.accept(s);
        c3.accept(s);
    }

    @OnHook
    public void foo(ComponentConfiguration cc, MainAggregate ma) {
        if (ma.main != null) {
            // install LifecycleSidecar

            // Problem hvad med Component
            // cc.prep(ma.main, LifecycleSidecar.class, (s, r) -> s.setRunnable(r))
            // Vi kan jo ikke bare lave en @OnHook... jo

            // Hvordan skal det here fungere med runtime componenter.
            // der kan vi jo ikke faa hjaelp af extensionen...
        }
        // Yes der er en main....
    }

    static class MainAggregate {
        PreparedLambda<Runnable> main;
    }

    public class LifecycleSidecar {

        void setRunnable(Runnable r) {

        }

        @OnHook
        public void foo(ComponentConfiguration cc, MainAggregate ma) {

        }

        public void listenerInstance(ComponentConfiguration cc, Object listenerInstance) {

        }
    }
}
