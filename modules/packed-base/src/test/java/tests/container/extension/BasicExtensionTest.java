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

import app.packed.container.Extension;
import testutil.util.AbstractApplicationTest;

/** Tests basic functionality of a {@link Extension}. */
public class BasicExtensionTest extends AbstractApplicationTest {

    /**
     * Checks that {@link Extension#buildContext()} is identical to {@link ContainerConfiguration#buildContext()}.
     */
    // @Test
    // public void buildContext() {
    // appOf(c -> assertThat(c.use(TestExtension.class).publicBuildContext()).isSameAs(c.buildContext()));
    // }

    /** Checks that we cannot call certain methods from the constructor of an extension. */
    @Test
    public void cannotCallExtensionsFromConstructor() {
        appOf(c -> assertThat(c.use(CallingMethodsFromTheConstructor.class)).isNotNull());
    }

    /** This extension is used by to check that we cannot call certain methods from the constructor of an extension. */
    public static final class CallingMethodsFromTheConstructor extends Extension {
        CallingMethodsFromTheConstructor() {
            String msg = "This operation cannot be invoked from the constructor of the extension. If you need to perform initialization before the extension is returned to the user, override Extension#onNew()";
            // assertThatIllegalStateException().isThrownBy(() -> buildContext()).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> checkConfigurable()).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> use(TestExtension1.Sub.class)).withMessage(msg);
        }
    }

    public static final class TestExtension1 extends Extension {
        TestExtension1() {}

        class Sub extends Subtension {}
    }

    public static final class TestExtension extends Extension {
        TestExtension() {}
        // public ArtifactBuildContext publicBuildContext() {
        // return buildContext();
        // }

    }
}
