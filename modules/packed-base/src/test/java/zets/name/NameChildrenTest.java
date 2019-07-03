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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import zets.name.spi.AbstractArtifactTest;
import zets.name.spi.AppTester;

/**
 *
 */
public class NameChildrenTest extends AbstractArtifactTest {

    @Test
    public void childName() {
        class Fff {}

        AppTester a = appOf(c -> assertThat(c.installHelper(Fff.class).getName()).isEqualTo("Fff"));
        a.assertPathExist("/Fff");

        a = appOf(c -> assertThat(c.installHelper(Fff.class).path().toString()).isEqualTo("/Fff"));
        a.assertPathExist("/Fff");

        // TODO FIX names for anonymous classes...
        // appOf(c -> assertThat(c.installHelper(new Object() {}.getClass()).getName()).isNotEqualTo(""));
    }

    @Test
    public void test() {
        class Gff {}
        class Fff {}
        AppTester a = appOf(c -> {
            assertThat(c.installHelper(Fff.class).path().toString()).isEqualTo("/Fff");
            assertThat(c.installHelper(Gff.class).path().toString()).isEqualTo("/Gff");
            for (int i = 1; i < 10; i++) {
                assertThat(c.installHelper(Fff.class).path().toString()).isEqualTo("/Fff" + i);
            }
            assertThat(c.installHelper(Gff.class).path().toString()).isEqualTo("/Gff1");
        });
        a.assertPathExist("/Fff");
        for (int i = 1; i < 10; i++) {
            a.assertPathExist("/Fff" + i);
        }
        a.assertPathExist("/Gff");
        a.assertPathExist("/Gff1");
    }
}
