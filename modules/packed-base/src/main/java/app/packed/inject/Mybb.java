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
package app.packed.inject;

import java.lang.invoke.MethodHandles;

import app.packed.bundle.BundleDescriptor;
import app.packed.bundle.ConfigureWiringOperation;

/**
 *
 */
public class Mybb extends InjectorBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        setDescription("Nice Bundle");
    }

    public static void main(String[] args) {
        BundleDescriptor bd = BundleDescriptor.of(Mybb.class);

        System.out.println(bd.bundleId());
        System.out.println(bd.bundleDescription());

        Injector.of(Mybb.class, ConfigureWiringOperation.patchBundle(), ServiceRebinder.rewrite(Long.class).as(123L));

        Injector.of(Mybb.class, new ConfigureWiringOperation(MethodHandles.lookup()) {
            @Provides(instantionMode = InstantiationMode.PROTOTYPE)
            public Long foo() {
                return System.nanoTime();
            }
        });

    }
}
