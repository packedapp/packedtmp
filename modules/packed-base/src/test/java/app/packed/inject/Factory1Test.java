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
import static testutil.assertj.Assertions.checkThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import app.packed.base.Key;
import packed.internal.inject.dependency.DependencyDescriptor;

/** Tests {@link Factory1}. */
public class Factory1Test {

    /**
     * Tests that we can capture information about a simple factory producing {@link Integer} instances.
     */
    @Test
    public void IntegerFactory0() {

        Factory1<String, Integer> f = new Factory1<String, Integer>(Integer::valueOf) {};
        checkThat(f).is(Integer.class);
        List<DependencyDescriptor> dependencies = f.dependencies();
        assertThat(dependencies).hasSize(1);
        DependencyDescriptor d = dependencies.get(0);

        assertThat(d.isOptional()).isFalse();
        assertThat(d.key()).isEqualTo(Key.of(String.class));
        // These would only be non-empty if we had made the factory from Factory.ofMethod(Integer.class, "valueOf",
        // String.class)
        assertThat(d.member()).isEmpty();
        //assertThat(d.variable()).isEmpty();
    }

    // TODO test that we can capture annotations
}
