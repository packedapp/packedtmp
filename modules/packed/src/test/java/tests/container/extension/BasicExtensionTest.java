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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionPoint;
import testutil.util.AbstractApplicationTest;

/** Tests basic functionality of a {@link Extension}. */
public class BasicExtensionTest extends AbstractApplicationTest {

    /** Checks that we cannot call certain methods from the constructor of an extension. */
    @Disabled // this is okay now
    @Test
    public void cannotCallUseExtensionFromConstructor() {
        appOf(c -> assertThat(c.use(CallingMethodsFromTheConstructor.class)).isNotNull());
    }

    /** This extension is used by to check that we cannot call certain methods from the constructor of an extension. */
    public static final class CallingMethodsFromTheConstructor extends Extension<CallingMethodsFromTheConstructor> {
        CallingMethodsFromTheConstructor() {
            String msg = "This operation cannot be invoked from the constructor of an extension. If you need to perform initialization before the extension is returned to the user, override Extension#onNew()";
            // assertThatIllegalStateException().isThrownBy(() -> buildContext()).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> checkIsConfigurable()).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> use(TestExtension1.TestExtension1ExtensionPoint.class)).withMessage(msg);
        }
    }

    public static final class TestExtension1 extends Extension<TestExtension1> {
        TestExtension1() {}

        class TestExtension1ExtensionPoint extends ExtensionPoint<TestExtension1> {}
    }

    public static final class TestExtension extends Extension<TestExtension> {
        TestExtension() {}
        // public ArtifactBuildContext publicBuildContext() {
        // return buildContext();
        // }

    }
}
