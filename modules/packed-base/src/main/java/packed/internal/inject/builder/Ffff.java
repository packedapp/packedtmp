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
package packed.internal.inject.builder;

import app.packed.bundle.Bundle;
import app.packed.inject.Injector;

/**
 *
 */
public class Ffff extends Bundle {

    public static void main(String[] args) {
        Injector i = Injector.of(new Ffff());
        System.out.println(i.services().count() + "");
        System.out.println(i.use(String.class));

        // run(new Ffff(), args);
    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        provide("asdsad");
        export(String.class);
    }
}
