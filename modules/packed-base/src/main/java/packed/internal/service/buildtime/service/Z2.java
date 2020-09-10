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
package packed.internal.service.buildtime.service;

import app.packed.container.BaseBundle;
import app.packed.inject.Provide;
import app.packed.service.Injector;

/**
 *
 */
public class Z2 extends BaseBundle {

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        install(NoDep.class);
    }

    public static void main(String[] args) {
        Injector.create(new Z2());
    }

    public static class NoDep {
        public NoDep() {
            System.out.println("Instantiated");
        }

        @Provide(constant = true)
        public String s() {
            return "adsasd";
        }
    }
}