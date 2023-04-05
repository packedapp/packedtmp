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
package tests2.service;

import org.junit.jupiter.api.Test;

import app.packed.util.Key;
import app.packed.util.KeyAlreadyUsedException;
import testutil.stubs.annotation.IntQualifier;
import tools.TestApp;

/**
 *
 */
public class DublicateServicesTest {

    @Test
    public void dublicateKeyDifferentBeans() {
        @SuppressWarnings("unused")
        KeyAlreadyUsedException k = TestApp.assertThrows(KeyAlreadyUsedException.class, c -> {
            c.installInstance(1L).provideAs(Number.class);
            c.installInstance(1).provideAs(Number.class);
        });

        // TODO check message

        k = TestApp.assertThrows(KeyAlreadyUsedException.class, c -> {
            c.installInstance(1L).provideAs(new Key<Number>() {});
            c.installInstance(1).provideAs(Number.class);
        });

        k = TestApp.assertThrows(KeyAlreadyUsedException.class, c -> {
            c.installInstance(1L).provideAs(new Key<@IntQualifier Number>() {});
            c.installInstance(1).provideAs(new Key<@IntQualifier Number>() {});
        });
    }
}
