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
import app.packed.container.ExtensionComposer;
import app.packed.container.UseExtension;
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
            assertThat(c.extensions()).containsExactly(Ex1.class, Ex2.class, Ex3.class);
        });
    }

    /** While we do not advertise it. We do allow cyclic dependencies between extensions. */
    // Ehmmm no we don't
    @Test
    @Disabled
    public void testAllowCyclicDependenciesExtension() {
        appOf(c -> {
            c.use(ExRecursive1.class);
            assertThat(c.extensions()).containsExactly(ExRecursive1.class, ExRecursive2.class);
        });
    }

    @UseExtension(Ex2.class)
    static final class Ex1 extends Extension {

        static class Composer extends ExtensionComposer<Ex1> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                onExtensionInstantiated(e -> e.use(Ex2.class));
            }
        }
    }

    @UseExtension(Ex3.class)
    static final class Ex2 extends Extension {

        static class Composer extends ExtensionComposer<Ex2> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                onExtensionInstantiated(e -> e.use(Ex3.class));
            }
        }
    }

    static final class Ex3 extends Extension {

    }

    @UseExtension(ExRecursive2.class)
    static final class ExRecursive1 extends Extension {

        static class Composer extends ExtensionComposer<ExRecursive1> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                onExtensionInstantiated(e -> e.use(ExRecursive2.class));
            }
        }
    }

    static final class ExRecursive2 extends Extension {
        static class Composer extends ExtensionComposer<ExRecursive2> {

            /** {@inheritDoc} */
            @Override
            protected void configure() {
                onExtensionInstantiated(e -> e.use(ExRecursive1.class));
            }
        }
    }
}
