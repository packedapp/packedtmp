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
package tck.component.name;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import app.packed.component.Wirelet;
import app.packed.container.BaseAssembly;
import testutil.util.AbstractArtifactTest;
import testutil.util.ContainerConfigurationTester;

/** Tests that a proper name is generated if the name of a container is not explicitly set. */
public class NameGeneratedTest extends AbstractArtifactTest {

    /** Tests the */
    @Test
    public void bundles() {
        check(f -> new AbstractConsumableAssembly(f) {}, "Assembly"); // Anonymous class
        check(f -> new S(f), "S");
        check(f -> new NameGeneratedTest.Assembly(f), "Assembly");
        check(f -> new HelloWorld(f), "HelloWorld");
        check(f -> new HelloWorldAssembly(f), "HelloWorld");
    }

    private static void check(Function<Consumer<? super ContainerConfigurationTester>, ? extends BaseAssembly> cs, String defaultName) {
        appOf(cs.apply(c -> {})).nameIs(defaultName);
        appOf(cs.apply(c -> {})).nameIs(defaultName);
        // We can override default name
        appOf(cs.apply(c -> c.getNameIs("Boo")), Wirelet.named("Boo")).nameIs("Boo");

        // Images
        imageOf(cs.apply(c -> {})).nameIs(defaultName);
        imageOf(cs.apply(c -> {})).nameIs(defaultName);
        imageOf(cs.apply(c -> {})).newApp().nameIs(defaultName);

        // We can override default name from images
        imageOf(cs.apply(c -> c.getNameIs("Boo")), Wirelet.named("Boo")).nameIs("Boo");
        imageOf(cs.apply(c -> c.getNameIs("Boo")), Wirelet.named("Boo")).newApp().nameIs("Boo");

        // As a child
        appOf(new AbstractConsumableAssembly(c -> {
            c.link(cs.apply(cc -> {
                cc.pathIs("/" + defaultName);
            }));
        }) {}).nameIs("Assembly");

        // As multiple children
        appOf(new AbstractConsumableAssembly(c -> {
            c.link(cs.apply(cc -> {
                cc.pathIs("/" + defaultName);
            }));
            c.link(cs.apply(cc -> {
                cc.pathIs("/" + defaultName + "1");
            }));
        }) {}).nameIs("Assembly");

        // As two level nested
        appOf(new AbstractConsumableAssembly(c -> {
            c.link(cs.apply(cc -> {
                cc.link(cs.apply(ccc -> {
                    ccc.pathIs("/" + defaultName + "/" + defaultName);
                }));
            }));
        }) {}).nameIs("Assembly");

        // As 3 level nested
        appOf(new AbstractConsumableAssembly(c -> {
            c.link(cs.apply(cc -> {
                cc.link(cs.apply(ccc -> {
                    ccc.link(cs.apply(cccc -> {
                        cccc.pathIs("/" + defaultName + "/" + defaultName + "/" + defaultName);
                    }));
                }));
            }));
        }) {}).nameIs("Assembly");
    }

    /** We normally remove the suffix 'Bundle', so make sure Bundle works */
    private class Assembly extends AbstractConsumableAssembly {
        Assembly(Consumer<? super ContainerConfigurationTester> ca) {
            super(ca);
        }
    }

    private class HelloWorld extends AbstractConsumableAssembly {
        HelloWorld(Consumer<? super ContainerConfigurationTester> ca) {
            super(ca);
        }
    }

    private class HelloWorldAssembly extends AbstractConsumableAssembly {
        HelloWorldAssembly(Consumer<? super ContainerConfigurationTester> ca) {
            super(ca);
        }
    }

    private class S extends AbstractConsumableAssembly {
        S(Consumer<? super ContainerConfigurationTester> ca) {
            super(ca);
        }
    }
}
