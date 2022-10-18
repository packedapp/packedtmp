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
package tck.extension.service;

import org.junit.jupiter.api.Test;

import app.packed.service.Provide;

/**
 *
 */

// Maybe have 2 tests.

// One, which doesn't provide

public class TryIt extends VerifyingTestCase {

    @Test
    public void foo() {
        bean().installInstance("HejHej").provide();

        checkMirror(c -> {
            System.out.println(c.extensionTypes());
        });
    }

    @Test
    public void foox() {
        bean().multiInstallInstance("HejHej").provide();
        bean().multiInstallInstance("HejHej").provideAs(CharSequence.class);
        bean().multiInstallInstance("HejHej").provideAs(CharSequence.class);
    }

    @Test
    public void fooxxx() {
        class MyX {

            @Provide
            public String s(Integer ss) {
                throw new UnsupportedOperationException();
            }
        }
        bean().multiInstall(MyX.class).provide();
        // bean().multiInstallInstance("HejHej").provide();
    }

}
