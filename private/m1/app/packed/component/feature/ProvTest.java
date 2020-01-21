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
package app.packed.component.feature;

import app.packed.artifact.App;
import app.packed.container.BaseBundle;
import app.packed.service.Injector;
import app.packed.service.Provide;

/**
 *
 */
class ProvTest extends BaseBundle {

    @Override
    protected void configure() {
        provideInstance(new Foo());
    }

    public static void main(String[] args) {
        Injector ii = Injector.of(new ProvTest());
        ii.services().forEach(e -> System.out.println(e.key()));

        System.out.println("----");
        // run(new ProvTest());

        App a = App.start(new ProvTest());
        a.stream().feature(ProvideFeature.class).forEach(e -> System.out.println(e.key() + ": " + e.configSite()));
    }

    public void foo(App a) {
        a.stream().feature(ProvideFeature.class).forEach(e -> System.out.println(e.key() + ": " + e.configSite()));
    }

    public static class Foo {

        @Provide
        public String foo() {
            return "FF";
        }

        @Provide
        public Long foos() {
            return 123L;
        }
    }
}
