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
package tck.old;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionPoint;

/** Tests basic functionality of a {@link Extension}. */
@Disabled // this is okay now
public class CannotCallFromConstructorExtensionTest {

    /** Checks that we cannot call certain methods from the constructor of an extension. */
    @Test
    public void cannotCallUseExtensionFromConstructor() {}

    /** This extension is used by to check that we cannot call certain methods from the constructor of an extension. */
    public static final class CallingMethodsFromTheConstructor extends Extension<CallingMethodsFromTheConstructor> {
        /**
         * @param handle
         */
        protected CallingMethodsFromTheConstructor(ExtensionHandle<CallingMethodsFromTheConstructor> handle) {
            super(handle);
            String msg = "This operation cannot be invoked from the constructor of an extension. If you need to perform initialization before the extension is returned to the user, override Extension#onNew()";
            // assertThatIllegalStateException().isThrownBy(() -> buildContext()).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> checkIsConfigurable()).withMessage(msg);
            assertThatIllegalStateException().isThrownBy(() -> use(TestExtension1.TestExtension1ExtensionPoint.class)).withMessage(msg);
        }
    }

    public static final class TestExtension1 extends Extension<TestExtension1> {

        /**
         * @param handle
         */
        protected TestExtension1(ExtensionHandle<TestExtension1> handle) {
            super(handle);
        }

        class TestExtension1ExtensionPoint extends ExtensionPoint<TestExtension1> {
            protected TestExtension1ExtensionPoint(ExtensionUseSite usesite) {
                super(usesite);
            }
        }
    }

    public static final class TestExtension extends Extension<TestExtension> {

        /**
         * @param handle
         */
        protected TestExtension(ExtensionHandle<TestExtension> handle) {
            super(handle);
        }
        // public ArtifactBuildContext publicBuildContext() {
        // return buildContext();
        // }

    }
}
