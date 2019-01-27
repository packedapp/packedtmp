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
package micro.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.bundle.Bundle;
import app.packed.inject.Factory;
import app.packed.inject.Injector;

/**
 *
 */
public class TetIt {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            Injector inj = Injector.of(c -> {
                c.lookup(MethodHandles.lookup());
                c.bind(Factory.ofInstance("foo"));
                c.bind(NeedsString.class);
            });
            requireNonNull(inj.with(NeedsString.class));
        }
        System.out.println("Total time: " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    static class NeedsString {
        NeedsString(String s) {}
    }

    public static class MyContainer extends Bundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            install(Factory.ofInstance("Root")).asNone();
            install(Factory.ofInstance("Child1")).asNone();
            install(Factory.ofInstance("Child2")).asNone();
        }
    }
}
