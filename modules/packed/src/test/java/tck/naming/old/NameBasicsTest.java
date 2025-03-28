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
package tck.naming.old;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.container.Wirelet;

/**
 * Various basic test for naming of components.
 */
// There is a lot of runtime thingsies here
public class NameBasicsTest extends AbstractApplicationTest {

    @Disabled // TODO Enable again (After ComponentConfiguration gets a handle
    @Test
    public void basics() {
        appOf(_ -> {}, Wirelet.renameApplication("Boo")).nameIs("Boo");
        appOf(_ -> {}, Wirelet.renameApplication("Boo"), Wirelet.renameApplication("Goo")).nameIs("Goo");

        // Tests that getName returns wirelet name
        appOf(c -> c.getNameIs("Boo"), Wirelet.renameApplication("Boo")).nameIs("Boo");
        appOf(c -> c.getNameIs("Goo"), Wirelet.renameApplication("Boo"), Wirelet.renameApplication("Goo")).nameIs("Goo");

        appOf(c -> c.setName("Foo").getNameIs("Foo")).nameIs("Foo");
        appOf(c -> c.setName("Foo").getNameIs("Boo"), Wirelet.renameApplication("Boo")).nameIs("Boo");
        appOf(c -> c.setName("Foo").getNameIs("Goo"), Wirelet.renameApplication("Boo"), Wirelet.renameApplication("Goo")).nameIs("Goo");
    }

    /** Tests that we can use question marks in names */
    @Test
    @Disabled // ENABLE again
    public void questionMarks() {
        appOf(_ -> {}, Wirelet.renameApplication("Boo?")).nameIs("Boo");
        appOf(_ -> {}, Wirelet.renameApplication("Boo?"), Wirelet.renameApplication("Goo?")).nameIs("Goo");

        // Tests that getName returns wirelet name
        appOf(c -> c.getNameIs("Boo"), Wirelet.renameApplication("Boo?")).nameIs("Boo");
        appOf(c -> c.getNameIs("Goo"), Wirelet.renameApplication("Boo?"), Wirelet.renameApplication("Goo")).nameIs("Goo");

        appOf(c -> c.setName("Foo?").getNameIs("Foo")).nameIs("Foo");
        appOf(c -> c.setName("Foo?").getNameIs("Boo"), Wirelet.renameApplication("Boo?")).nameIs("Boo");
        appOf(c -> c.setName("Foo?").getNameIs("Goo"), Wirelet.renameApplication("Boo?"), Wirelet.renameApplication("Goo?")).nameIs("Goo");
    }

    /** Tests valid names for components. */
    @Test
    public void validNames() {
        // TODO implement

        // Foo??
        // Foo-?
        // Foo-

        // Maybe also test wirelets...
    }
}
