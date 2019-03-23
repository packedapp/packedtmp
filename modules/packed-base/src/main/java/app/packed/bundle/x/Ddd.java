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
package app.packed.bundle.x;

import java.io.IOException;
import java.util.List;

import app.packed.app.App;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleDescriptor;

/**
 *
 */
public class Ddd {

    public static void main(String[] args) throws IOException {
        // App.run(new D(), a -> {
        // try (FileInputStream fis = new FileInputStream(new File("/sdsd"))) {
        // fis.read();
        //
        // }
        // });

        App.run(new D(), c -> c.with(List.class).size());

        App.runAsync(new D());
        App.runAsync(new D(), c -> c.with(List.class).size());

        Integer i = App.invoke(new D(), c -> c.with(List.class).size());
        System.out.println(i);
        for (var h : BundleDescriptor.of(new D()).hooks().annotatedFieldExports(Deprecated.class)) {
            System.out.println(h.field() + " is " + h.annotation());
        }

    }

    static class D extends Bundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {}

    }
}
