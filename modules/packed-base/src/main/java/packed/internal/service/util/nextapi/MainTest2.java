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
package packed.internal.service.util.nextapi;

import app.packed.app.App;
import app.packed.container.BaseBundle;
import app.packed.lifecycle.Main;

/**
 *
 */
public class MainTest2 extends BaseBundle {

    @Override
    protected void configure() {
        link(new MyMainX());
        System.out.println(installInstance("foo").path());
    }

    public static void main(String[] args) {
        App a = App.of(new MainTest2());
        System.out.println();
        a.stream().forEach(e -> System.out.println(e.path()));
    }

    static class MyMainX extends BaseBundle {

        @Override
        protected void configure() {
            installInstance(this);
            System.out.println(path());
            System.out.println(installInstance("foo").path());
            System.out.println(installInstance(334).path());
        }

        @Main
        public static void say() {
            System.out.println("HelloWorld!!");
        }

    }
}
