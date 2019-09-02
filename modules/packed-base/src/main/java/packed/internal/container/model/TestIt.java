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
package packed.internal.container.model;

import app.packed.app.App;
import app.packed.app.AppBundle;
import app.packed.inject.Factory2;

/**
 *
 */
public class TestIt extends AppBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        provide("foo");
        provide("foo").as(CharSequence.class);
        provide(new Factory2<>((CharSequence cs, String s) -> 3 + s.length() + cs.length()) {});
        export(Integer.class);
    }

    public static void main(String[] args) {
        try (App a = App.of(new TestIt())) {
            a.injector().services().forEach(e -> System.out.println(e));
            System.out.println(a.injector().use(Integer.class));
        }
    }

}
