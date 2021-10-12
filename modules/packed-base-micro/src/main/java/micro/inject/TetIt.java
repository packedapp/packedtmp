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

import app.packed.bundle.BaseAssembly;
import app.packed.inject.Factory;
import packed.internal.inject.service.sandbox.Injector;

/**
 *
 */
public class TetIt {

    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        System.in.read();
        System.out.println("STarting");
        for (int i = 0; i < 1_000_0000; i++) {
            Injector inj = Injector.configure(c -> {
                c.lookup(MethodHandles.lookup());
                c.provide(Factory.ofConstant("foo"));
                c.provide(NeedsString.class);
            });
            requireNonNull(inj.use(NeedsString.class));
        }
        System.out.println("Total time: " + (System.currentTimeMillis() - start) + " milliseconds");
    }

    static class NeedsString {
        NeedsString(String s) {}
    }

    public static class MyContainer extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            provide(Factory.ofConstant("Root"));
            provide(Factory.ofConstant("Child1"));
            provide(Factory.ofConstant("Child2"));
        }
    }
}
