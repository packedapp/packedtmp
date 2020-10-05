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
package packed.internal.inject;

import app.packed.component.App;
import app.packed.container.BaseBundle;

/**
 *
 */
public class FooBar extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        install(NeedsString.class);
        provideInstance(123L);

        link(new Child());
    }

    public static class NeedsString {
        public NeedsString(ChildServ string) {
            System.out.println("GOt " + string);
        }
    }

    public static void main(String[] args) {
        App.of(new FooBar());
    }

    public static class Child extends BaseBundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            provide(ChildServ.class).export();
        }
    }

    public static class ChildServ {

        public ChildServ(Long l) {
            System.out.println("L " + l);
        }
    }
}
