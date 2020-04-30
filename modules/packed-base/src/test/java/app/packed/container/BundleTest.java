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

import app.packed.analysis.BundleDescriptor;
import testutil.util.AbstractArtifactTest;

/** Various Bundle tests. */
public class BundleTest extends AbstractArtifactTest {

    /** Tests that a bundle cannot be reused. */
    @Test
    public void notReusable() {
        Bundle empty = new Bundle() {

            @Override
            protected void compose() {}
        };

        BundleDescriptor.of(empty);
        assertThatThrownBy(() -> BundleDescriptor.of(empty)).isExactlyInstanceOf(IllegalStateException.class);
    }

    /** Tests that a bundle cannot be reused. */
    @Test
    public void cannotLinkSelf() {
        Bundle b = new Bundle() {

            @Override
            protected void compose() {
                link(this);
            }
        };
        assertThatThrownBy(() -> BundleDescriptor.of(b)).isExactlyInstanceOf(IllegalStateException.class);
    }
}