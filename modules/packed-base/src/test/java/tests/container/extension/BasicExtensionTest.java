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
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Test;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.WireletList;
import support.testutil.AbstractArtifactTest;

/** Tests basic functionality of a {@link Extension}. */
public class BasicExtensionTest extends AbstractArtifactTest {

    /**
     * Checks that {@link Extension#buildContext()} is identical to {@link ContainerConfiguration#buildContext()}.
     */
    @Test
    public void buildContext() {
        appOf(c -> assertThat(c.use(TestExtension.class).publicBuildContext()).isSameAs(c.buildContext()));
    }

    /** Checks that we cannot call certain methods from the constructor of an extension. */
    @Test
    public void cannotCallExtensionsFromConstructor() {
        appOf(c -> assertThat(c.use(CallingMethodsFromTheConstructor.class)).isNotNull());
    }

    /** Checks that {@link Extension#wirelets()} is identical to {@link ContainerConfiguration#wirelets()}. */
    @Test
    public void wirelets() {
        appOf(c -> assertThat(c.use(TestExtension.class).publicWirelets()).isSameAs(c.wirelets()));
    }

    /** This extension is used by to check that we cannot call certain methods from the constructor of an extension. */
    public static class CallingMethodsFromTheConstructor extends Extension {
        public CallingMethodsFromTheConstructor() {
            String msg = "This operation cannot be called from the constructor of the extension, #onAdd() can be overridden, as an alternative, to perform initialization";
            assertThatIllegalStateException().isThrownBy(() -> buildContext()).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> checkConfigurable()).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> use(TestExtension1.class)).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> wirelets()).withMessage(msg);
        }
    }

    public static class TestExtension1 extends Extension {}

    public static class TestExtension extends Extension {

        public ArtifactBuildContext publicBuildContext() {
            return buildContext();
        }

        public WireletList publicWirelets() {
            return wirelets();
        }
    }
}
