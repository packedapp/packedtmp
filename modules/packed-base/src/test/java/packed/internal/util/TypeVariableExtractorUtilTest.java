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
package packed.internal.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.TypeVariable;
import java.util.List;

import org.junit.jupiter.api.Test;

import packed.internal.util.TypeVariableExtractorUtil;
import support.stubs.TypeStubs;

/**
 *
 */
public class TypeVariableExtractorUtilTest {

    @Test
    public void test1Level() {
        class X<T1, T2, T3, T4> {}
        class Y<T> extends X<String, int[], List<String>, T> {}

        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Y.class, X.class, 0)).isSameAs(String.class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Y.class, X.class, 1)).isSameAs(int[].class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Y.class, X.class, 2)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Y.class, X.class, 3)).isInstanceOf(TypeVariable.class);
    }

    @Test
    public void test2Level() {
        class X<T1, T2, T3, T4> {}
        class Y<S1, S2> extends X<S1, String, int[], S2> {}
        class Z<U1> extends Y<List<String>, U1> {}

        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Z.class, X.class, 0)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Z.class, X.class, 1)).isSameAs(String.class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Z.class, X.class, 2)).isSameAs(int[].class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Z.class, X.class, 3)).isInstanceOf(TypeVariable.class);
    }

    @Test
    public void test3Level() {
        class V<A1, A2, A3, A4> {}
        class W<B1, B2, B3> extends V<B1, B2, int[], B3> {}
        class X<C1, C2> extends W<C1, List<String>, C2> {}
        class Y<V1> extends X<String, V1> {}
        class Z extends Y<List<?>> {}

        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(W.class, V.class, 0)).isInstanceOf(TypeVariable.class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(W.class, V.class, 1)).isInstanceOf(TypeVariable.class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(W.class, V.class, 2)).isSameAs(int[].class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(W.class, V.class, 3)).isInstanceOf(TypeVariable.class);

        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(X.class, V.class, 0)).isInstanceOf(TypeVariable.class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(X.class, V.class, 1)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(X.class, V.class, 2)).isSameAs(int[].class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(X.class, V.class, 3)).isInstanceOf(TypeVariable.class);

        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Y.class, V.class, 0)).isSameAs(String.class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Y.class, V.class, 1)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Y.class, V.class, 2)).isSameAs(int[].class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Y.class, V.class, 3)).isInstanceOf(TypeVariable.class);

        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Z.class, V.class, 0)).isSameAs(String.class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Z.class, V.class, 1)).isEqualTo(TypeStubs.LIST_STRING);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Z.class, V.class, 2)).isSameAs(int[].class);
        assertThat(TypeVariableExtractorUtil.findTypeParameterFromSuperClass(Z.class, V.class, 3)).isEqualTo(TypeStubs.LIST_WILDCARD);

    }
}
