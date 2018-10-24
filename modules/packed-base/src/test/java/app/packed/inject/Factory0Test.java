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

import static app.packed.inject.Factory0.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static support.assertj.Assertions.assertThatFactory;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import support.stubs.annotation.SystemProperty;

/** Tests {@link Factory0}. */
public class Factory0Test {

    /** Tests the static methods. */
    public static class StaticMethods {

        /** Tests {@link Factory0#of(Supplier, Class)} */
        @Test
        public void classParameter() {
            assertThatFactory(of(() -> 1, Number.class)).is(Number.class);
            assertThatFactory(of(() -> 1, Integer.class)).is(Integer.class);
        }

        /** Tests {@link Factory0#of(Supplier, TypeLiteral)} */
        @Test
        @Disabled
        public void typeLiteralParameter() {
            assertThatFactory(of(() -> List.of(1), new Key<@SystemProperty("fff") List<Integer>>() {})).is(new Key<List<Integer>>() {});

            assertThatFactory(of(() -> List.of(1), new TypeLiteral<List<Integer>>() {})).is(new Key<List<Integer>>() {});
            assertThatFactory(of(() -> List.of(1), new TypeLiteral<List<Number>>() {})).is(new Key<List<Number>>() {});
            assertThatFactory(of(() -> List.of(1), new TypeLiteral<List<?>>() {})).is(new Key<List<?>>() {});

        }
    }

    /** Tests parsing of type parameter (<T>) info */
    public static class TypeParameterRead {

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
            String expectedMsg = "Could not determine the type variable <T> of " + Factory0.class.getSimpleName() + "<T> for "
                    + Factory0Test.class.getCanonicalName();
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Factory0(() -> 1) {}).withNoCause()
                    .withMessageStartingWith(expectedMsg);

            expectedMsg = "Could not determine the type variable <T> of " + X.class.getSimpleName() + "<S,T,R> for " + Factory0Test.class.getCanonicalName();
            assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new X(() -> 1) {}).withNoCause().withMessageStartingWith(expectedMsg);
        }

        @Test
        public void typeParameterVarious() {
            assertEquals(new TypeLiteral<List<Integer>>() {}, new X<>(() -> List.of(1)) {}.getTypeLiteral());
        }

        /** Check that we can have an intermediate abstract class. */
        abstract class X<S, T, R> extends Factory0<T> {
            protected X(Supplier<T> supplier) {
                super(supplier);
            }
        }
    }
}
