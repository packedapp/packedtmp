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
package internal.app.packed.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.TypeVariable;
import java.util.List;

import org.junit.jupiter.api.Test;

import internal.app.packed.util.typevariable.TypeVariableExtractor;
import testutil.stubs.TypeStubs;

/**
 *
 */
public class TypeVariableExtractorUtilTest {

    @Test
    public void test1Level() {
        class X<T1, T2, T3, T4> {}
        class Y<T> extends X<String, int[], List<String>, T> {}

        assertThat(TypeVariableExtractor.of(X.class, 0).extract(Y.class)).isSameAs(String.class);
        assertThat(TypeVariableExtractor.of(X.class, 1).extract(Y.class)).isSameAs(int[].class);
        assertThat(TypeVariableExtractor.of(X.class, 2).extract(Y.class)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractor.of(X.class, 3).extract(Y.class)).isInstanceOf(TypeVariable.class);
    }

    @Test
    public void test2Level() {
        class X<T1, T2, T3, T4> {}
        class Y<S1, S2> extends X<S1, String, int[], S2> {}
        class Z<U1> extends Y<List<String>, U1> {}

        assertThat(TypeVariableExtractor.of(X.class, 0).extract(Z.class)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractor.of(X.class, 1).extract(Z.class)).isSameAs(String.class);
        assertThat(TypeVariableExtractor.of(X.class, 2).extract(Z.class)).isSameAs(int[].class);
        assertThat(TypeVariableExtractor.of(X.class, 3).extract(Z.class)).isInstanceOf(TypeVariable.class);
    }

    @Test
    public void test3Level() {
        class V<A1, A2, A3, A4> {}
        class W<B1, B2, B3> extends V<B1, B2, int[], B3> {}
        class X<C1, C2> extends W<C1, List<String>, C2> {}
        class Y<V1> extends X<String, V1> {}
        class Z extends Y<List<?>> {}

        assertThat(TypeVariableExtractor.of(V.class, 0).extract(W.class)).isInstanceOf(TypeVariable.class);
        assertThat(TypeVariableExtractor.of(V.class, 1).extract(W.class)).isInstanceOf(TypeVariable.class);
        assertThat(TypeVariableExtractor.of(V.class, 2).extract(W.class)).isSameAs(int[].class);
        assertThat(TypeVariableExtractor.of(V.class, 3).extract(W.class)).isInstanceOf(TypeVariable.class);

        assertThat(TypeVariableExtractor.of(V.class, 0).extract(X.class)).isInstanceOf(TypeVariable.class);
        assertThat(TypeVariableExtractor.of(V.class, 1).extract(X.class)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractor.of(V.class, 2).extract(X.class)).isSameAs(int[].class);
        assertThat(TypeVariableExtractor.of(V.class, 3).extract(X.class)).isInstanceOf(TypeVariable.class);

        assertThat(TypeVariableExtractor.of(V.class, 0).extract(Y.class)).isSameAs(String.class);
        assertThat(TypeVariableExtractor.of(V.class, 1).extract(Y.class)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractor.of(V.class, 2).extract(Y.class)).isSameAs(int[].class);
        assertThat(TypeVariableExtractor.of(V.class, 3).extract(Y.class)).isInstanceOf(TypeVariable.class);

        assertThat(TypeVariableExtractor.of(V.class, 0).extract(Z.class)).isSameAs(String.class);
        assertThat(TypeVariableExtractor.of(V.class, 1).extract(Z.class)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractor.of(V.class, 2).extract(Z.class)).isSameAs(int[].class);
        assertThat(TypeVariableExtractor.of(V.class, 3).extract(Z.class)).isEqualTo(TypeStubs.LIST_WILDCARD);

    }
}
