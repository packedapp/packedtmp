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
package app.packed.cli;

import java.lang.invoke.MethodHandles;

import app.packed.application.App;
import app.packed.application.ApplicationDriver;
import app.packed.application.BuildWirelets;
import app.packed.container.BaseAssembly;
import app.packed.service.ServiceLocator;

/**
 *
 */
public class HelloWorldAssembly extends BaseAssembly {

    @Override
    protected void build() {
        provide(SomeComponent.class).export();
    }

    public static void main(String[] args) {
        ApplicationDriver<Aaaa> ad = ApplicationDriver.builder().build(MethodHandles.lookup(), Aaaa.class);
        ad.launch(new HelloWorldAssembly());

        // Job.compute()
        App.run(new HelloWorldAssembly(), BuildWirelets.spyOnWire(c -> {
            System.out.println(c.path() + " wired");
        }));
        
        System.out.println("BYE");

    }

    static class Aaaa {

        Aaaa(ServiceLocator ss) {
            System.out.println("NICE APP YOU GOT THERE " + ss.size());
            ss.use(SomeComponent.class).runMe();
        }
    }

    public static class SomeComponent {

        @Main
        public void runMe() {
            System.out.println("HelloWorld");
        }
    }
}
