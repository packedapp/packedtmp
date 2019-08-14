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
package app.packed.hook.usage;

import app.packed.container.ContainerConfiguration;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.DelayedAccessor;
import app.packed.hook.OnHook;
import app.packed.hook.OnHookAggregateBuilder;
import app.packed.inject.Provide;
import app.packed.util.FieldMapper;

/**
 *
 */
public class OnExtensionAggregate {

    @OnHook(aggregateWith = MyExtensionAggregate.Builder.class)
    public void foo(ContainerConfiguration cc, MyExtensionAggregate s) {
        s.fm.onReady(cc, e -> System.out.println("Field ..." + " is instantiated as " + e));
    }

    static class MyExtensionAggregate {
        final DelayedAccessor<String> fm;

        MyExtensionAggregate(Builder b) {
            this.fm = b.accessor;
        }

        public static class Builder implements OnHookAggregateBuilder<MyExtensionAggregate> {
            DelayedAccessor<String> accessor;

            public void foo(AnnotatedFieldHook<Provide> h) {
                accessor = h.accessor(FieldMapper.get(String.class));
            }

            /** {@inheritDoc} */
            @Override
            public MyExtensionAggregate build() {
                return new MyExtensionAggregate(this);
            }
        }
    }
}
