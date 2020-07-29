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
package app.packed.container;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import testutil.util.AbstractArtifactTest;

/** Various Bundle tests. */
public class BundleTest extends AbstractArtifactTest {

    /** Tests that a bundle cannot be reused. */
    @Test
    public void notReusable() {
        ContainerBundle empty = new ContainerBundle() {

            @Override
            protected void configure() {}
        };

        ContainerDescriptor.of(empty);
        assertThatThrownBy(() -> ContainerDescriptor.of(empty)).isExactlyInstanceOf(IllegalStateException.class);
    }

    /** Tests that a bundle cannot be reused. */
    @Test
    public void cannotLinkSelf() {
        ContainerBundle b = new ContainerBundle() {

            @Override
            protected void configure() {
                link(this);
            }
        };
        assertThatThrownBy(() -> ContainerDescriptor.of(b)).isExactlyInstanceOf(IllegalStateException.class);
    }
}
