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

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import support.testutil.AbstractArtifactTest;

/** Tests {@link ContainerConfiguration#extensions()} and {@link ContainerConfiguration#use(Class)}. */
public class ContainerConfigurationExtensionTest extends AbstractArtifactTest {

    /** Tests basic use of {@link ContainerConfiguration#use(Class)}. */
    @Test
    public void use() {
        appOf(e -> e.use(TestExtension1.class, i -> {
            assertThat(i).isNotNull();
            assertThat(i).isExactlyInstanceOf(TestExtension1.class);
            // Return the same instance every time
            assertThat(i).isSameAs(e.use(TestExtension1.class));
            assertThat(i).isSameAs(e.use(TestExtension1.class));
        }));
    }

    /** Tests basic use of {@link ContainerConfiguration#extensions()}. */
    @Test
    public void extensions() {
        appOf(e -> {
            assertThat(e.extensions()).isEmpty();
            e.use(TestExtension1.class);
            assertThat(e.extensions()).containsExactlyInAnyOrder(TestExtension1.class);
            e.use(TestExtension1.class);
            assertThat(e.extensions()).containsExactlyInAnyOrder(TestExtension1.class);
            e.use(TestExtension2.class);
            assertThat(e.extensions()).containsExactlyInAnyOrder(TestExtension1.class, TestExtension2.class);
        });
    }

    /**
     * Tests what happens if people try to use any of the extension methods outside of the configure of the defining bundle.
     * We allow invoking {@link ContainerConfiguration#extensions()} and allow {@link ContainerConfiguration#use(Class)} for
     * extension that have already been installed. Calling {@link ContainerConfiguration#use(Class)} with an extension that
     * have not previously been installed will throw an {@link IllegalStateException}.
     */
    @Test
    public void unconfigurable() {
        AtomicReference<ContainerConfiguration> r = new AtomicReference<>();

        // Test empty
        appOf(e -> r.set(e.configuration()));
        assertThat(r.get().extensions()).isEmpty();
        assertThatIllegalStateException().isThrownBy(() -> r.get().use(TestExtension1.class));

        AtomicReference<TestExtension1> t1 = new AtomicReference<>();
        // Test Existing
        appOf(e -> {
            t1.set(e.use(TestExtension1.class));
            r.set(e.configuration());
        });
        assertThat(r.get().extensions()).contains(TestExtension1.class);
        assertThat(r.get().use(TestExtension1.class)).isSameAs(t1.get());
        assertThatIllegalStateException().isThrownBy(() -> r.get().use(TestExtension2.class));
    }

    public static class TestExtension1 extends Extension {}

    public static class TestExtension2 extends Extension {}
}
