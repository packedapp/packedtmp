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
package a;

import a.ITest.QQ;
import app.packed.container.BaseBundle;
import app.packed.inject.Injector;
import app.packed.inject.ServiceWirelets;
import app.packed.util.Key;

/**
 *
 */
public class ImportTest extends BaseBundle {

    public static void main(String[] args) {
        Injector i = Injector.of(new ImportTest());
        i.services().forEach(e -> {
            System.out.println(e.key());
        });
    }

    @Override
    public void configure() {
        Injector inj = Injector.of(new IBundle());
        provide("fooo");
        injector().importAll(inj, ServiceWirelets.removeKeys(new Key<@QQ("B") String>() {}, new Key<@QQ("C") String>() {}));

    }

    public static class IBundle extends BaseBundle {

        @Override
        public void configure() {
            provide("fooA").as(new Key<@QQ("A") String>() {});
            provide("fooB").as(new Key<@QQ("B") String>() {});
            provide("fooC").as(new Key<@QQ("C") String>() {});
        }
    }

}
