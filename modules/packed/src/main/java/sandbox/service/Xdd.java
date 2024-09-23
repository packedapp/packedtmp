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
package sandbox.service;

import app.packed.application.App;
import app.packed.assembly.BaseAssembly;
import app.packed.lifetime.OnInitialize;
import app.packed.service.ServiceContract;

/**
 *
 */
public class Xdd extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {
        provideInstance(4444L);
        install(Foo.class).bindServiceInstance(String.class, "Bdoo").bindServiceInstance(Integer.class, 12333).bindServiceInstance(Long.class, 123L);
    }

    public static void main(String[] args) {
        App.run(new Xdd());
        ServiceContract sc = ServiceContract.of(new Xdd());
        System.out.println(sc);
    }

    public record Foo(String s, Integer i) {
        public Foo {
            System.out.println(s + " " + i);
        }

        @OnInitialize
        public void dd(Long l) {
            System.out.println(s + " " + l);
        }
    }

}
