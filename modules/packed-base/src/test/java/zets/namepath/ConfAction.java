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
package zets.namepath;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import app.packed.container.ContainerConfiguration;

/**
 *
 */
@FunctionalInterface
public interface ConfAction {

    static final ConfAction NONE = c -> {};

    void apply(ContainerConfiguration c);

    default ConfAction andThen(ConfAction other) {
        requireNonNull(other);
        return cc -> {
            apply(cc);
            other.apply(cc);
        };
    }

    default ConfAction throwing(Object exception) {
        // Same as AssertJ
        throw new UnsupportedOperationException();
    }

    default ConfAction thenGetName(String expected) {
        return andThen(getName(expected));
    }

    default ConfAction thenSetName(String name) {
        return andThen(setName(name));
    }

    static ConfAction setName(String name) {
        return c -> {
            assertThat(c.setName(name)).isSameAs(c);
        };
    }

    static ConfAction getName(String expected) {
        return c -> {
            assertThat(c.getName()).isEqualTo(expected);
        };
    }
}
