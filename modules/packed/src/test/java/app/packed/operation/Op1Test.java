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
package app.packed.operation;

import static testutil.assertj.Assertions.checkThat;

import org.junit.jupiter.api.Test;

/** Tests {@link Op1}. */
public class Op1Test {

    /**
     * Tests that we can capture information about a simple factory producing {@link Integer} instances.
     */
    @Test
    public void toInteger() {

        Op1<String, Integer> f = new Op1<String, Integer>(Integer::valueOf) {};
        
        // Make an abstract op test?
        // Maybe just asssert.
        // assertKeyEquals(key, Class);
        
        checkThat(f).is(Integer.class);
        // These would only be non-empty if we had made the factory from Factory.ofMethod(Integer.class, "valueOf",
        // String.class)
        //assertThat(d.variable()).isEmpty();
    }

    // TODO test that we can capture annotations
}
