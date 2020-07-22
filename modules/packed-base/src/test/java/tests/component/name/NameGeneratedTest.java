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
package tests.component.name;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import app.packed.container.Wirelet;
import testutil.util.AbstractArtifactTest;
import testutil.util.ContainerConfigurationTester;

/** Tests that a proper name is generated if the name of a container is not explicitly set. */
public class NameGeneratedTest extends AbstractArtifactTest {

    /** Tests the */
    @Test
    public void bundles() {
        check(f -> new AbstractConsumableBundle(f) {}, "Container"); // Anonymous class
        check(f -> new S(f), "S");
        check(f -> new NameGeneratedTest.Bundle(f), "Bundle");
        check(f -> new HelloWorld(f), "HelloWorld");
        check(f -> new HelloWorldBundle(f), "HelloWorld");
    }

    private static void check(Function<Consumer<? super ContainerConfigurationTester>, ? extends app.packed.container.ContainerBundle> cs, String defaultName) {
        appOf(cs.apply(c -> {})).nameIs(defaultName);
        appOf(cs.apply(c -> {})).nameIs(defaultName);
        // We can override default name
        appOf(cs.apply(c -> c.getNameIs("Boo")), Wirelet.name("Boo")).nameIs("Boo");

        // Images
        imageOf(cs.apply(c -> {})).nameIs(defaultName);
        imageOf(cs.apply(c -> {})).nameIs(defaultName);
        imageOf(cs.apply(c -> {})).newApp().nameIs(defaultName);

        // We can override default name from images
        imageOf(cs.apply(c -> c.getNameIs("Boo")), Wirelet.name("Boo")).nameIs("Boo");
        imageOf(cs.apply(c -> c.getNameIs("Boo")), Wirelet.name("Boo")).newApp().nameIs("Boo");

        // As a child
        appOf(new AbstractConsumableBundle(c -> {
            c.link(cs.apply(cc -> {
                cc.pathIs("/" + defaultName);
            }));
        }) {}).nameIs("Container");

        // As multiple children
        appOf(new AbstractConsumableBundle(c -> {
            c.link(cs.apply(cc -> {
                cc.pathIs("/" + defaultName);
            }));
            c.link(cs.apply(cc -> {
                cc.pathIs("/" + defaultName + "1");
            }));
        }) {}).nameIs("Container");

        // As two level nested
        appOf(new AbstractConsumableBundle(c -> {
            c.link(cs.apply(cc -> {
                cc.link(cs.apply(ccc -> {
                    ccc.pathIs("/" + defaultName + "/" + defaultName);
                }));
            }));
        }) {}).nameIs("Container");

        // As 3 level nested
        appOf(new AbstractConsumableBundle(c -> {
            c.link(cs.apply(cc -> {
                cc.link(cs.apply(ccc -> {
                    ccc.link(cs.apply(cccc -> {
                        cccc.pathIs("/" + defaultName + "/" + defaultName + "/" + defaultName);
                    }));
                }));
            }));
        }) {}).nameIs("Container");
    }

    /** We normally remove the suffix 'Bundle', so make sure Bundle works */
    private class Bundle extends AbstractConsumableBundle {
        Bundle(Consumer<? super ContainerConfigurationTester> ca) {
            super(ca);
        }
    }

    private class HelloWorld extends AbstractConsumableBundle {
        HelloWorld(Consumer<? super ContainerConfigurationTester> ca) {
            super(ca);
        }
    }

    private class HelloWorldBundle extends AbstractConsumableBundle {
        HelloWorldBundle(Consumer<? super ContainerConfigurationTester> ca) {
            super(ca);
        }
    }

    private class S extends AbstractConsumableBundle {
        S(Consumer<? super ContainerConfigurationTester> ca) {
            super(ca);
        }
    }
}
