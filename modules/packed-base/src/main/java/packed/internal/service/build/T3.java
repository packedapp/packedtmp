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
package packed.internal.service.build;

import app.packed.app.App;
import app.packed.container.BaseBundle;
import app.packed.container.Bundle;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceWirelets;

/**
 *
 */
public class T3 {

    static Bundle b() {
        return new BaseBundle() {
            @Override
            protected void configure() {
                provide(Foo.class);
            }
        };
    }

    public static void main(String[] args) {
        System.out.println(ServiceContract.of(b()));

        App.of(b(), ServiceWirelets.provide("foo"));
    }

    public static class Foo {
        public Foo(String s) {
            System.out.println("Yes");
        }
    }
}
