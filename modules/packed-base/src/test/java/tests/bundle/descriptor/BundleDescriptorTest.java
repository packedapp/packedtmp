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
package tests.bundle.descriptor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.packed.bundle.Bundle;
import app.packed.bundle.BundleDescriptor;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;

/**
 *
 */
public class BundleDescriptorTest {

    @Nested
    public static class ExposedService {

        @Test
        public void emptyBundle() {
            BundleDescriptor d = BundleDescriptor.of(new Bundle() {
                @Override
                protected void configure() {}
            });
            assertThat(d.services().exports()).isEmpty();

            d = BundleDescriptor.of(new Bundle() {
                @Override
                protected void configure() {
                    bind("non-exposed service is not in descriptor");
                }
            });
            assertThat(d.services().exports()).isEmpty();
        }

        @Test
        public void simple() {
            Bundle ib = new Bundle() {
                @Override
                protected void configure() {
                    bind("foo").setDescription("fooDesc");
                    expose(String.class);
                }
            };

            BundleDescriptor d = BundleDescriptor.of(ib);
            assertThat(d.services().exports()).containsOnlyKeys(Key.of(String.class));
            ServiceDescriptor sd = d.services().exports().get(Key.of(String.class));

            assertThat(sd.getInstantiationMode()).isSameAs(InstantiationMode.SINGLETON);
            assertThat(sd.getDescription()).isEqualTo("fooDesc");
            assertThat(sd.getKey()).isEqualTo(Key.of(String.class));
            assertThat(sd.tags()).isEmpty();
        }
    }
}
