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

import app.packed.base.OnAssembling;
import app.packed.container.Extension;
import app.packed.container.ExtensionSidecar;
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
            assertThat(c.extensions()).containsExactly(Ex3.class, Ex2.class, Ex1.class);
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

    @ExtensionSidecar(dependencies = Ex2.class)
    static final class Ex1 extends Extension {
        @OnAssembling(ExtensionSidecar.ON_INSTANTIATION)
        void on() {
            use(Ex2.class);
        }
    }

    @ExtensionSidecar(dependencies = Ex3.class)
    static final class Ex2 extends Extension {

        @OnAssembling(ExtensionSidecar.ON_INSTANTIATION)
        void on() {
            use(Ex3.class);
        }
    }

    static final class Ex3 extends Extension {

    }

    @ExtensionSidecar(dependencies = ExRecursive2.class)
    static final class ExRecursive1 extends Extension {

        @OnAssembling(ExtensionSidecar.ON_INSTANTIATION)
        void on() {
            use(ExRecursive2.class);
        }
    }

    static final class ExRecursive2 extends Extension {

        @OnAssembling(ExtensionSidecar.ON_INSTANTIATION)
        void on() {
            use(ExRecursive1.class);
        }
    }
}
