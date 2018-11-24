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

import app.packed.bundle.BundleDescriptor;
import app.packed.bundle.InjectorBundle;
import app.packed.inject.BindingMode;
import app.packed.inject.Key;
import app.packed.inject.ServiceDescriptor;

/**
 *
 */
public class BundleDescriptorTest {

    @Nested
    public static class ExposedService {

        @Test
        public void emptyBundle() {
            BundleDescriptor d = BundleDescriptor.of(new InjectorBundle() {
                @Override
                protected void configure() {}
            });
            assertThat(d.services().exposedServices()).isEmpty();

            d = BundleDescriptor.of(new InjectorBundle() {
                @Override
                protected void configure() {
                    bind("non-exposed service is not in descriptor");
                }
            });
            assertThat(d.services().exposedServices()).isEmpty();
        }

        @Test
        public void simple() {
            InjectorBundle ib = new InjectorBundle() {
                @Override
                protected void configure() {
                    bind("foo").setDescription("fooDesc");
                    expose(String.class);
                }
            };

            BundleDescriptor d = BundleDescriptor.of(ib);
            assertThat(d.services().exposedServices()).containsOnlyKeys(Key.of(String.class));
            ServiceDescriptor sd = d.services().exposedServices().get(Key.of(String.class));

            assertThat(sd.getBindingMode()).isSameAs(BindingMode.SINGLETON);
            assertThat(sd.getDescription()).isEqualTo("fooDesc");
            assertThat(sd.getKey()).isEqualTo(Key.of(String.class));
            assertThat(sd.tags()).isEmpty();
        }
    }
}
