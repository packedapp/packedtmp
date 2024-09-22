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
package tck.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import app.packed.container.ContainerConfiguration;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import tck.AppAppTest;

/** Tests {@link ContainerConfiguration#extensionTypes()} and {@link ContainerConfiguration#use(Class)}. */
public class BasicExtensionTest extends AppAppTest {

    /** Tests basic use of {@link ContainerConfiguration#use(Class)}. */
    @Test
    public void use() {
        assertFalse(configuration().isExtensionUsed(TestExtension1.class));
        TestExtension1 e = configuration().use(TestExtension1.class);
        assertTrue(configuration().isExtensionUsed(TestExtension1.class));

        assertThat(e).isNotNull();
        assertThat(e).isExactlyInstanceOf(TestExtension1.class);
        // Return the same instance every time
        assertThat(e).isSameAs(configuration().use(TestExtension1.class));
        assertThat(e).isSameAs(configuration().use(TestExtension1.class));
    }

    /** Tests basic use of {@link ContainerConfiguration#extensionTypes()}. */
    @Test
    public void extensions() {
        assertThat(configuration().extensionTypes()).containsExactly(BaseExtension.class);
        configuration().use(TestExtension1.class);
        assertThat(configuration().extensionTypes()).containsExactlyInAnyOrder(BaseExtension.class, TestExtension1.class);
        configuration().use(TestExtension1.class);
        assertThat(configuration().extensionTypes()).containsExactlyInAnyOrder(BaseExtension.class, TestExtension1.class);
        configuration().use(TestExtension2.class);
        assertThat(configuration().extensionTypes()).containsExactlyInAnyOrder(BaseExtension.class, TestExtension1.class, TestExtension2.class);
    }

    /**
     * Tests what happens if people try to use any of the extension methods outside of the configure of the defining
     * assembly. We allow invoking {@link ContainerConfiguration#extensionTypes()} and allow
     * {@link ContainerConfiguration#use(Class)} for extension that have already been installed. Calling
     * {@link ContainerConfiguration#use(Class)} with an extension that have not previously been installed will throw an
     * {@link IllegalStateException}.
     */
    @Test
    public void unconfigurable() {
        ContainerConfiguration cc = configuration();

        launch();

        // Test empty
        assertThat(cc.extensionTypes()).containsExactly(BaseExtension.class);
        assertThatIllegalStateException().isThrownBy(() -> cc.use(TestExtension1.class));

        reset();

        ContainerConfiguration cc2 = configuration();
        TestExtension1 t1 = cc2.use(TestExtension1.class);
        launch();

        assertThat(cc2.extensionTypes()).contains(TestExtension1.class);
        assertThat(cc2.use(TestExtension1.class)).isSameAs(t1);
        assertThatIllegalStateException().isThrownBy(() -> cc2.use(TestExtension2.class));
    }

    public static final class TestExtension1 extends Extension<TestExtension1> {

        TestExtension1(ExtensionHandle<TestExtension1> handle) {
            super(handle);
        }

    }

    public static final class TestExtension2 extends Extension<TestExtension2> {

        TestExtension2(ExtensionHandle<TestExtension2> handle) {
            super(handle);
        }
    }
}
