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
package tck.bundle.descriptor;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.bundle.Bundle;
import app.packed.bundle.BundleContract;
import app.packed.bundle.BundleDescriptor;
import app.packed.inject.ServiceDescriptor;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.Letters.C;

/**
 * Test {@link BundleContract#services()}.
 */
public class ServicesTest {

    /** A service will never be both requires and optional. */
    @Test
    public void requiresOverrideOptional() {
        BundleDescriptor d = BundleDescriptor.of(new Bundle() {

            @Override
            protected void configure() {
                lookup(MethodHandles.lookup());
                provide(B.class);
                provide(A.class);
                provide(C.class);
            }
        });
        for (ServiceDescriptor s : d.services()) {
            System.out.println(s);
        }
    }
}
