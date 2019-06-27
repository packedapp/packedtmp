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
package zets.name;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import app.packed.component.ComponentExtension;
import app.packed.container.Bundle;
import zets.name.spi.AbstractBaseTest;
import zets.name.spi.ContainerConfigurationTester;

/**
 * Tests the various ways the name can be frozen.
 */
public class NameFreezeTest extends AbstractBaseTest {

    @Test
    public void setNameISE() {
        // Cannot call setName twice
        assertISE(c -> c.setName("Foo").setName("Bar"), "#setName(String) can only be called once for a container");

        // Cannot call setName after getName
        assertISE(c -> c.getNameIs("Container").setName("Bar"), "Cannot call #setName(String) after name has been initialized via call to #getName()");

        // Cannot call setName after path
        assertISE(c -> c.pathIs("/").setName("Bar"), "Cannot call #setName(String) after name has been initialized via call to #path()");

        // Cannot call setName after having linked another bundle
        assertISE(c -> c.link(new Bundle() {}).setName("Bar"), "Cannot call this method after containerConfiguration.link has been invoked");

        // Cannot call setName after having linked another bundle
        assertISE(c -> c.use(ComponentExtension.class, e -> e.install("Foo")).setName("Bar"),
                "Cannot call this method after installing new components in the container");

        // TODO should we test this for children as well????
    }

    private static void assertISE(Consumer<? super ContainerConfigurationTester> source, String message) {
        assertThatIllegalStateException().isThrownBy(() -> appOf(source)).withNoCause().withMessage(message);
    }
}
