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
package packed.internal.inject.test;

import app.packed.component.App;
import app.packed.container.BaseAssembly;

/**
 *
 */
class Ddd extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance(new Runnable() {

            @Override
            public void run() {
                System.out.println("RUN");
            }
        }).as(Runnable.class);

        provideInstance("asdasd");
        link(new FooAssembly());

        export(Runnable.class);
        /*
         * link(new FooAssembly(), ServiceWirelets.to(e -> e.decorate(Runnable.class, r -> () -> { System.out.println("BEFORE");
         * r.run(); System.out.println("After"); })));
         */
    }

    public static void main(String[] args) {
        System.out.println(App.start(new Ddd()).services());
    }

    public static class FooAssembly extends BaseAssembly {

        /** {@inheritDoc} */
        @Override
        protected void build() {
            //
            provide(NeedsString.class);
        }
    }

    public static class NeedsString {
        public NeedsString(Runnable s) {
            System.out.println("SD " + s);
        }
    }

}
