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
package tests.container;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import app.packed.container.Component;
import app.packed.container.Container;

/**
 *
 */
public class SimpleContainersTest {

    @Test
    public void root() {
        String foo = "foo";
        Container co = Container.of(c -> c.install(foo));
        Component c = co.root();
        assertThat(c.children()).isEmpty();
        assertThat(c.getContainer()).isSameAs(co);
        assertThat(c.getDescription()).isNull();
        // assertThat(c.getInstance()).isSameAs(foo);
        assertThat(c.getName()).isNotNull();
        // assertThat(c.getPath())
        // assertThat(c.getPath()).isEqualToIgnoringCase("/");

        assertThat(c.stream().count()).isEqualTo(1);
        assertThat(c.tags()).isEmpty();

    }
}
