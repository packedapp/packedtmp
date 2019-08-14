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

import java.lang.invoke.MethodHandles;

import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHookAggregateBuilder;
import app.packed.lifecycle.Main;

/**
 *
 */
final class GAggregate {

    private GAggregate(Builder b) {

    }

    static final class Builder implements OnHookAggregateBuilder<GAggregate> {

        /** {@inheritDoc} */
        @Override
        public GAggregate build() {
            return new GAggregate(this);
        }

        /// Not sure we need annotations???
        // Just take all methods that takes a hook????
        public void foo(AnnotatedMethodHook<Main> f) {

        }
    }

    public static void main(String[] args) {
        GAggregate generate = OnHookAggregateBuilder.generate(MethodHandles.lookup(), Builder.class, MethodHandles.lookup(), String.class);
        System.out.println(generate);
    }
}
