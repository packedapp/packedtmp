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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutil.assertj.Assertions.checkThat;

import java.util.function.Supplier;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/** Tests {@link Op}. */
public class OpXTest {

    /** Tests that we can capture information about a simple factory producing {@link Integer} instances. */
    @Test
    public void integerFactory0() {
        Op<Integer> f = new Op0<>(() -> 1) {};
        assertEquals(Integer.class, f.type().returnType());
        checkThat(f).is(Integer.class);
        checkThat(f).hasNoDependencies();

        f = new Intermediate<String, Integer, Long>(() -> 123) {};
        checkThat(f).is(Integer.class);
        checkThat(f).hasNoDependencies();
    }

    /** Tests that we can capture information about a simple factory producing lists of integers instances. */
    @Test
    public void listIntegerFactory0() {
//        Op<List<Integer>> f = new Op0<>(() -> List.of(1)) {};
//        checkThat(f).is(new GenericType<List<Integer>>() {});
//        checkThat(f).hasNoDependencies();
//
//        f = new Intermediate<>(() -> List.of(1)) {};
//        checkThat(f).is(new GenericType<List<Integer>>() {});
//        checkThat(f).hasNoDependencies();
    }

    /**
     * Tests that we fail if we cannot read the type variable. We test it both when directing inheriting from Factory0, and
     * via an intermediate class.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void typeParameterIndeterminable() {
        // TODO change to Factory instead of BaseFactory
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Op0(() -> 1) {}).withNoCause()
                .withMessageStartingWith("Cannot determine type variable <R> for " + Op0.class.getSimpleName() + "<R> on class " + OpXTest.class.getPackageName());

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> new Intermediate(() -> 1) {}).withNoCause()
                .withMessageStartingWith("Cannot determine type variable <T> for " + Op0.class.getSimpleName() + "<R> on class " + OpXTest.class.getCanonicalName());
    }

    @Test
    @Disabled
    public void fff() throws Throwable {
//        @SuppressWarnings("rawtypes")
//        Supplier s = () -> 23;
//        @SuppressWarnings("unchecked")
//        Factory<?> f = new Factory0<String>(s) {};
//        try {
//            f.toMethodHandle(null).invoke();
//        } catch (FactoryException ok) {}
    }

    /** Check that we can have an intermediate abstract class. */
    static abstract class Intermediate<S, T, R> extends Op0<T> {
        protected Intermediate(Supplier<T> supplier) {
            super(supplier);
        }
    }
}
