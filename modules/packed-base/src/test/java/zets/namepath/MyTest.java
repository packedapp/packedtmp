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
package zets.namepath;

import static zets.name.spi.AppTester.of;
import static zets.namepath.ConfAction.NONE;
import static zets.namepath.ConfAction.getName;
import static zets.namepath.ConfAction.setName;

import org.junit.jupiter.api.Test;

import app.packed.container.Wirelet;

/**
 *
 */
public class MyTest extends AbstractSSSTest {

    @Test
    public void questionMarks() {
        of(NONE, Wirelet.name("Boo?")).nameIs("Boo");
        of(NONE, Wirelet.name("Boo?"), Wirelet.name("Goo?")).nameIs("Goo");

        // Tests that getName returns wirelet name
        of(getName("Boo"), Wirelet.name("Boo?")).nameIs("Boo");
        of(getName("Goo"), Wirelet.name("Boo?"), Wirelet.name("Goo")).nameIs("Goo");

        of(setName("Foo?").thenGetName("Foo")).nameIs("Foo");
        of(setName("Foo?").thenGetName("Boo"), Wirelet.name("Boo?")).nameIs("Boo");
        of(setName("Foo?").thenGetName("Goo"), Wirelet.name("Boo?"), Wirelet.name("Goo?")).nameIs("Goo");
    }

    @Test
    public void basics() {
        of(NONE, Wirelet.name("Boo")).nameIs("Boo");
        of(NONE, Wirelet.name("Boo"), Wirelet.name("Goo")).nameIs("Goo");

        // Tests that getName returns wirelet name
        of(getName("Boo"), Wirelet.name("Boo")).nameIs("Boo");
        of(getName("Goo"), Wirelet.name("Boo"), Wirelet.name("Goo")).nameIs("Goo");

        of(setName("Foo").thenGetName("Foo")).nameIs("Foo");
        of(setName("Foo").thenGetName("Boo"), Wirelet.name("Boo")).nameIs("Boo");
        of(setName("Foo").thenGetName("Goo"), Wirelet.name("Boo"), Wirelet.name("Goo")).nameIs("Goo");
    }
}
