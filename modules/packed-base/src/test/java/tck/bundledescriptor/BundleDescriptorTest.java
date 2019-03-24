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
package tck.bundledescriptor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.packed.bundle.Bundle;
import app.packed.bundle.BundleContract;
import app.packed.util.Key;

/**
 *
 */
public class BundleDescriptorTest {

    @Nested
    public static class ExposedService {

        @Test
        public void emptyBundle() {
            BundleContract c = BundleContract.of(new Bundle() {
                @Override
                protected void configure() {}
            });
            assertThat(c.services().provides()).isEmpty();

            c = BundleContract.of(new Bundle() {
                @Override
                protected void configure() {
                    provide("non-exported service are not in descriptor");
                }
            });
            assertThat(c.services().provides()).isEmpty();
        }

        @Test
        public void simple() {
            Bundle b = new Bundle() {
                @Override
                protected void configure() {
                    provide("foo").setDescription("fooDesc");
                    export(String.class);
                }
            };

            BundleContract c = BundleContract.of(b);
            assertThat(c.services().provides()).containsOnly(Key.of(String.class));
            // ServiceDescriptor sd = d.services().provides().get(Key.of(String.class));
            //
            // // assertThat(sd.getInstantiationMode()).isSameAs(InstantiationMode.SINGLETON);
            // assertThat(sd.description()).isEqualTo("fooDesc");
            // assertThat(sd.key()).isEqualTo(Key.of(String.class));
            // assertThat(sd.tags()).isEmpty();
        }
    }
}
