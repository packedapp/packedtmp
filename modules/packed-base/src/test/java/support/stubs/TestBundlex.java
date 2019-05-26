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
package support.stubs;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.Test;

import app.packed.container.Bundle;
import app.packed.container.BundleDescriptor;
import app.packed.inject.Injector;

/**
 *
 */
public class TestBundlex extends Bundle {

    @Test
    public void main() {
        BundleDescriptor bd = BundleDescriptor.of(new TestBundlex());

        System.out.println(bd);

        Injector i = Injector.of(new TestBundlex());
        i.services().forEach(e -> System.out.println(e));
    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        lookup(MethodHandles.lookup());
        provide(A.class);
        provide(this);
        export(A.class);
    }

    public static class A {
        public A() {}
    }
}
