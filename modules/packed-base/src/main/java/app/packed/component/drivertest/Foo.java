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
package app.packed.component.drivertest;

import java.lang.invoke.MethodHandles;

import app.packed.artifact.App;
import app.packed.component.ComponentDriver.Option;
import app.packed.component.WireableComponentDriver;
import app.packed.container.BaseBundle;

/**
 *
 */
public class Foo {

    public static void main(String[] args) {
        WireableComponentDriver<Foo> create = WireableComponentDriver.create(MethodHandles.lookup(), Option.container());

        BaseBundle bb = new BaseBundle() {

            @Override
            protected void configure() {
                Foo wire = wire(create);
                System.out.println(wire);
            }
        };

        App.create(bb);
    }
}
