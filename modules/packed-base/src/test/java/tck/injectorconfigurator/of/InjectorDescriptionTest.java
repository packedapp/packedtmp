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
package tck.injectorconfigurator.of;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import app.packed.inject.Injector;

/**
 *
 */
public class InjectorDescriptionTest {

    @Test
    public void description() {
        assertThat(Injector.of(c -> {}).description().isEmpty());

        Injector i = Injector.of(c -> {
            assertThat(c.getDescription()).isNull();

            assertThat(c.setDescription("fooo")).isSameAs(c);
            assertThat(c.getDescription()).isEqualTo("fooo");

            assertThat(c.setDescription(null)).isSameAs(c);
            assertThat(c.getDescription()).isNull();

            c.setDescription("final_desc");
        });
        assertThat(i.description()).hasValue("final_desc");
    }
}
