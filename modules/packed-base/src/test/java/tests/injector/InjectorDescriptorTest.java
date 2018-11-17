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
package tests.injector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import app.packed.bundle.BundleDescriptor;
import app.packed.bundle.InjectorBundle;
import app.packed.inject.BindingMode;
import app.packed.inject.Key;
import app.packed.inject.ServiceDescriptor;
import support.stubs.Letters.A;

/**
 *
 */
public class InjectorDescriptorTest {

    @Test
    public void testBasicService() {
        BundleDescriptor d = BundleDescriptor.of(new InjectorBundle() {

            @Override
            protected void configure() {
                bind(A.class);
                expose(A.class);
            }
        });
        ServiceDescriptor sd = d.services().exposedServices().values().iterator().next();

        assertThat(sd.getBindingMode()).isSameAs(BindingMode.SINGLETON);
        assertThat(sd.getDescription()).isNull();
        assertThat(sd.getKey()).isEqualTo(Key.of(A.class));
        assertThat(sd.tags()).isEmpty();

        // BundleDescriptor er saa meget en AssertThatTarget
    }

}
