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

import static testutil.assertj.Assertions.checkThat;

import org.junit.jupiter.api.Test;

import app.packed.base.Key;

public class InternalFunctionInstanceTest {

    @Test
    public void ofInstance() {
        Factory<String> f = Factory.ofInstance("foo");
        checkThat(f).is(new Key<String>() {});
        // checkThat(f).hasBound(String.class, String.class);

    }

    @Test
    public void ofInstanceClass() {
        // checkThat(f).hasBound(String.class, String.class);
    }
}
