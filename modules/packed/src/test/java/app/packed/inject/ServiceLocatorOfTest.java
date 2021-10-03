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

import org.junit.jupiter.api.Test;

import app.packed.base.Key;
import app.packed.base.Tag;
import app.packed.inject.service.ServiceLocator;

/**
 *
 */
public class ServiceLocatorOfTest {

    @Test
    public void test() {
        ServiceLocator sl = ServiceLocator.of(c -> {
            c.provideInstance("FooBar");
            c.provideInstance(new Key<@Tag("fpp") String>() {}, "FooBarsdf");
        });
        sl.keys().contains(Key.of(String.class));
    }
}
