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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/** Tests {@link Factory0}. */
public class Factory0Test {

    @Test
    public void typeParameter() {
        Factory<Integer> f = new Factory0<>(() -> 1) {};
        assertEquals(TypeLiteral.of(Integer.class), new Factory0<>(() -> 1) {}.getTypeLiteral());
        assertEquals(Integer.class, f.getRawType());
        assertThat(f.getDependencies()).isEmpty();

        assertEquals(new TypeLiteral<Integer>() {}, new X<String, Integer, Long>(() -> 123) {}.getTypeLiteral());
    }

    @Test
    public void typeParameterListInteger() {
        Factory<List<Integer>> f = new Factory0<>(() -> List.of(1)) {};
        assertEquals(new TypeLiteral<List<Integer>>() {}, f.getTypeLiteral());
        assertEquals(List.class, f.getRawType());
        assertThat(f.getDependencies()).isEmpty();

        assertEquals(new TypeLiteral<List<Integer>>() {}, new X<>(() -> List.of(1)) {}.getTypeLiteral());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void typeParameterMissing() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Factory0(() -> 1) {}).withNoCause()
                .withMessageStartingWith("Cannot determine type variable <R> for Factory<T> on class app.packed.inject.");

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new X(() -> 1) {}).withNoCause()
                .withMessageStartingWith("Cannot determine type variable <R> for Factory<T> on class app.packed.inject.Factory0");
    }

    @Test
    public void typeParameterVarious() {
        assertEquals(new TypeLiteral<List<Integer>>() {}, new X<>(() -> List.of(1)) {}.getTypeLiteral());
    }

    /** Check that we can have an intermediate abstract class. */
    static abstract class X<S, T, R> extends Factory0<T> {
        protected X(Supplier<T> supplier) {
            super(supplier);
        }
    }
}
