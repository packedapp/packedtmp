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

import app.packed.bundle.BundleDescriptor;
import app.packed.bundle.InjectorBundle;
import app.packed.inject.Key;
import support.stubs.Letters.A;
import support.stubs.Letters.B;
import support.stubs.annotation.CharQualifier;

/**
 *
 */
public class Xxx {

    public static void main(String[] args) {
        InjectorBundle b = new InjectorBundle() {

            @Override
            protected void configure() {
                bind(A.class);
                expose(A.class);
                bindLazy(B.class);
                expose(B.class).as(new Key<@CharQualifier('X') B>() {});
            }
        };

        BundleDescriptor d = BundleDescriptor.of(b);

        System.out.println(d.services().exposedServices().values());

        // !B
        // ?B
        // !B*
        // !B^
        // ?B^ (optional lazy B)
        // B

    }
}
