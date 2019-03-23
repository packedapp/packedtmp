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
package examples.app;

import java.lang.invoke.MethodHandles;

import app.packed.bundle.Bundle;
import app.packed.bundle.BundleDescriptor;
import app.packed.bundle.ConfigureWiringOperation;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.inject.Provides;
import app.packed.inject.ServiceRebinder;

/**
 *
 */
public class Mybb extends Bundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        setDescription("Nice Bundle");
    }

    public static void main(String[] args) {
        BundleDescriptor bd = BundleDescriptor.of(new Mybb());

        System.out.println(bd.bundleId());
        System.out.println(bd.bundleDescription());

        Injector.of(new Mybb(), ConfigureWiringOperation.patchBundle(), ServiceRebinder.rewrite(Long.class).as(123L));

        Injector.of(new Mybb(), new ConfigureWiringOperation(MethodHandles.lookup()) {
            @Provides(instantionMode = InstantiationMode.PROTOTYPE)
            public Long foo() {
                return System.nanoTime();
            }
        });
    }
}
