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

import static org.assertj.core.api.Assertions.assertThat;
import static support.assertj.Assertions.checkThat;

import org.junit.jupiter.api.Test;

import app.packed.util.Key;

/** Tests {@link Factory1}. */
public class Factory1Test {

    /** Tests that we can capture information about a simple factory producing {@link Integer} instances. */
    @Test
    public void IntegerFactory0() {
        Factory<Integer> f = new Factory1<String, Integer>(Integer::valueOf) {};
        checkThat(f).is(Integer.class);
        assertThat(f.dependencies()).hasSize(1);
        Dependency d = f.dependencies().get(0);

        assertThat(d.isOptional()).isFalse();
        assertThat(d.key()).isEqualTo(Key.of(String.class));
        // These would only be non-empty if we had made the factory from Factory.ofMethod(Integer.class, "valueOf",
        // String.class)
        assertThat(d.member()).isEmpty();
        assertThat(d.parameterIndex()).isEmpty();
        assertThat(d.variable()).isEmpty();
    }
}
