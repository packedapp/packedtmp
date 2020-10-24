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
package tests.container.extension;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import app.packed.block.Extension;
import app.packed.block.ExtensionSetup;
import testutil.util.AbstractArtifactTest;

/**
 *
 */
public class ExtensionDependenciesTest extends AbstractArtifactTest {

    /** Test that we can depend on an uninstalled extension via. */
    @Test
    public void testCanCallUseFromOnExtensionAdded() {
        appOf(c -> {
            c.use(Ex1.class);
            assertThat(c.extensions()).containsExactlyInAnyOrder(Ex3.class, Ex2.class, Ex1.class);
        });
    }

    @ExtensionSetup(dependencies = Ex2.class)
    static final class Ex1 extends Extension {
        @Override
        protected void add() {
            useOld(Ex2.class);
        }
    }

    @ExtensionSetup(dependencies = Ex3.class)
    static final class Ex2 extends Extension {
        @Override
        protected void add() {
            useOld(Ex3.class);
        }
    }

    static final class Ex3 extends Extension {

    }
}
