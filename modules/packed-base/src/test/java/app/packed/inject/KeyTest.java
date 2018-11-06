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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import support.stubs.CharQualifier;
import support.stubs.CharQualifiers;

/**
 *
 */
public class KeyTest {
    static final Key<Integer> KEY_INT_OF = Key.of(int.class);
    static final Key<Integer> KEY_INTEGER = new Key<Integer>() {};
    static final Key<Integer> KEY_INTEGER_OF = Key.of(Integer.class);

    static final Key<Integer> KEY_INTEGER_X = new Key<@CharQualifier('X') Integer>() {};
    static final Key<Integer> KEY_INTEGER_Y = new Key<@CharQualifier('Y') Integer>() {};
    static final Key<Integer> KEY_INTEGER_X_OF = Key.of(Integer.class, CharQualifiers.X);
    static final Key<Integer> KEY_INTEGER_Y_OF = Key.of(Integer.class, CharQualifiers.Y);

    static final Key<?> TL_INTEGER_OFTYPE = Key.of((Type) Integer.class);

    static final Key<String> TL_STRING = new Key<String>() {};
    static final Key<List<String>> TL_LIST_STRING = new Key<List<String>>() {};
    static final Key<List<?>> TL_LIST_WILDCARD = new Key<List<?>>() {};

    static final Key<Map<? extends String, ?>> TL_MAP = new Key<Map<? extends String, ?>>() {};

    // Se ogsaa
    // https://github.com/leangen/geantyref/blob/master/src/main/java/io/leangen/geantyref/AnnotationInvocationHandler.java

    @Test
    public void canonicalize() {
        Key<Integer> key = Key.of(Integer.class, CharQualifiers.X);

        assertThat(key).isEqualTo(KEY_INTEGER_X);

        assertThat(key).isSameAs(key.canonicalize());
        assertThat(KEY_INTEGER_X).isNotSameAs(KEY_INTEGER_X.canonicalize());
    }

    @Test
    public void equalsHashCode() {
        assertThat(KEY_INT_OF).isEqualTo(KEY_INT_OF).isEqualTo(KEY_INTEGER).isEqualTo(KEY_INTEGER_OF);
        assertThat(KEY_INTEGER).isEqualTo(KEY_INTEGER_OF).isEqualTo(new Key<Integer>() {});
        assertThat(KEY_INTEGER).isNotEqualTo(null).isNotEqualTo(Integer.class);

        assertThat(KEY_INTEGER).hasSameHashCodeAs(KEY_INTEGER).hasSameHashCodeAs(KEY_INTEGER_OF);
        assertThat(KEY_INTEGER).hasSameHashCodeAs(KEY_INTEGER_OF).hasSameHashCodeAs(new Key<Integer>() {});

        // WithQualifiers
        assertThat(KEY_INTEGER_X).isEqualTo(KEY_INTEGER_X_OF).isEqualTo(new Key<@CharQualifier('X') Integer>() {});
        assertThat(KEY_INTEGER_Y).isEqualTo(KEY_INTEGER_Y_OF).isEqualTo(new Key<@CharQualifier('Y') Integer>() {});

        assertThat(KEY_INTEGER_X).hasSameHashCodeAs(KEY_INTEGER_X_OF).hasSameHashCodeAs(new Key<@CharQualifier('X') Integer>() {});
        assertThat(KEY_INTEGER_Y).hasSameHashCodeAs(KEY_INTEGER_Y_OF).hasSameHashCodeAs(new Key<@CharQualifier('Y') Integer>() {});

        assertThat(KEY_INTEGER_X).isNotEqualTo(KEY_INTEGER).isNotEqualTo(KEY_INTEGER_Y).isNotEqualTo((new Key<@CharQualifier('X') Long>() {}));
    }
}
