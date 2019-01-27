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
package tests.inject.bindinjector.bundle;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import app.packed.bundle.Bundle;
import app.packed.inject.Factory1;
import app.packed.inject.Injector;

/**
 *
 */
public class ExportTest2 {

    // The import at (Xxxx) and (Yyyy) both defines are service with Key<ZoneId>
    public static void main(String[] args) {

        Injector i = Injector.of(c -> {
            c.bind(ZoneId.systemDefault()).as(ZoneId.class);
            c.wireInjector(new I());
        });

        i.services().forEach(e -> System.out.println(e.key().toStringSimple()));

        System.out.println(i.with(ZonedDateTime.class));
    }

    public static final class I extends Bundle {

        /** {@inheritDoc} */
        @Override
        protected void configure() {
            // Requirements
            requireService(ZoneId.class);
            export(bindPrototype(new Factory1<ZoneId, ZonedDateTime>(ZonedDateTime::now) {}));
        }
    }

}
