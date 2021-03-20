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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.container.Extension;
import testutil.util.AbstractApplicationTest;

/**
 *
 */
@Disabled
public class ExtensionDependenciesTest extends AbstractApplicationTest {

    /** Test that we can depend on an uninstalled extension via. */
    @Test
    public void testCanCallUseFromOnExtensionAdded() {
        appOf(c -> {
            c.use(Ex1.class);
            assertThat(c.extensions()).containsExactlyInAnyOrder(Ex3.class, Ex2.class, Ex1.class);
        });
    }

    static final class Ex1 extends Extension {
        static {
            $dependsOn(Ex2.class);
        }

        @Override
        protected void onNew() {
            use(Ex2.Sub.class);
        }


    }

    static final class Ex2 extends Extension {
        static {
            $dependsOn(Ex3.class);
        }

        @Override
        protected void onNew() {
            use(Ex3.Sub.class);
        }

        class Sub extends Subtension {

        }

    }

    static final class Ex3 extends Extension {

        class Sub extends Subtension {

        }
    }
}
