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
package app.packed.bundle;

import java.time.LocalDate;

import app.packed.app.App;
import app.packed.inject.Factory;
import app.packed.inject.Factory0;
import app.packed.inject.Provide;

/**
 *
 */
public class Ggg2 extends Bundle {

    @Provide(prototype = true)
    static final Factory<LocalDate> TODAY = new Factory0<>(() -> LocalDate.now()) {};

    public static void main(String[] args) {
        App app = App.of(new Ggg2(), new WiringOption() {

            @Override
            protected void process(BundleLink link) {
                System.out.println("Du er sgu ret sej Kasper");
                // System.out.println(link.configSite());
            }
        });

        // app = App.of(new Ggg2(), WiringOption.of(l -> System.out.println("cool")));

        // System.out.println(app.services().count() + "");
        // System.out.println(app.use(String.class));

        System.out.println(app.name());
    }

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        setName("MyApppp");
        provide("asdsad");
        export(String.class);

        wire(new CBundle(), WiringOption.of(l -> System.out.println("cool baby-----")));
    }

    static class CBundle extends Bundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            setName("SomeChild");
        }
    }
}
