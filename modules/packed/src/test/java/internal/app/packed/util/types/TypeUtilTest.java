/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.util.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static testutil.Assertions.npe;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Tests {@link TypeUtil}. */
public class TypeUtilTest {

    /** Tests {@link TypeUtil#isFreeFromTypeVariables(Type)}. */
    @Test
    public void isFreeFromTypeVariables() {
        npe(() -> TypeUtil.isFreeFromTypeVariables(null), "type");

        @SuppressWarnings("unused")
        class C<T> {
            // Types free from type variables (f = free)
            public String f1;
            public String[] f2;
            public List<String> f3;
            public Map<? extends String, ? super Integer> f4;
            public int[] f5;
            public List<String>[] f6;
            public List<String>[][] f7;

            // Types with type variables (n = not free)
            public T[] n1;
            public List<T> n2;
            public List<T>[] n3;
            public List<? extends T> n4;
            public List<? super Collection<? extends T>> n5;
            public List<T>[][] n6;
        }

        for (Field f : C.class.getDeclaredFields()) {
            if (!f.isSynthetic()) {// Anonymous class capture this test class
                assertThat(TypeUtil.isFreeFromTypeVariables(f.getGenericType())).isEqualTo(f.getName().startsWith("f"));
            }
        }
        assertThatThrownBy(() -> TypeUtil.isFreeFromTypeVariables(new Type() {})).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    /** Tests {@link TypeUtil#isFreeFromWildcardVariables(Type)}. */
    @Test
    public void isFreeFromWildcardVariables() {
        npe(() -> TypeUtil.isFreeFromWildcardVariables(null), "type");

        @SuppressWarnings("unused")
        class C {
            // Types free from wildcards (f = free)
            public String f1;
            public String[] f2;
            public List<String> f3;
            public int[] f4;
            public List<String>[] f5;
            public List<String>[][] f6;
            public Map<String, Integer> f7;
            public List<List<String>> f8;

            // Types with wildcards (n = not free)
            public List<?> n1;
            public List<? extends String> n2;
            public List<? super String> n3;
            public Map<? extends String, ? super Integer> n4;
            public List<?>[] n5;
            public List<? extends List<?>> n6;
            public Map<String, ? extends Integer> n7;
        }

        for (Field f : C.class.getDeclaredFields()) {
            if (!f.isSynthetic()) {
                assertThat(TypeUtil.isFreeFromWildcardVariables(f.getGenericType()))
                    .as("Field %s", f.getName())
                    .isEqualTo(f.getName().startsWith("f"));
            }
        }

        // Test that TypeVariable throws UnsupportedOperationException
        @SuppressWarnings("unused")
        class D<T> {
            public T typeVar;
            public List<T> paramWithTypeVar;
            public T[] arrayOfTypeVar;
        }

        for (Field f : D.class.getDeclaredFields()) {
            if (!f.isSynthetic()) {
                assertThatThrownBy(() -> TypeUtil.isFreeFromWildcardVariables(f.getGenericType()))
                    .as("Field %s should throw UnsupportedOperationException", f.getName())
                    .isExactlyInstanceOf(UnsupportedOperationException.class);
            }
        }

        // Unknown Type throws IllegalArgumentException
        assertThatThrownBy(() -> TypeUtil.isFreeFromWildcardVariables(new Type() {}))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    /** Tests {@link TypeUtil#rawTypeOf(Type)}. */
    @Test
    public void rawTypeOf() throws Exception {
        @SuppressWarnings("unused")
        class C<T> {
            public List<String> f1;
            public Map<? extends String, ? super Integer> f2;
            public List<String>[] f3;
            public List<?> f4;
            public List<String>[][] f5;
        }
        npe(() -> TypeUtil.rawTypeOf(null), "type");
        assertThat(TypeUtil.rawTypeOf(String.class)).isSameAs(String.class);
        assertThat(TypeUtil.rawTypeOf(String[].class)).isSameAs(String[].class);

        assertThat(TypeUtil.rawTypeOf(C.class.getField("f1").getGenericType())).isSameAs(List.class);
        assertThat(TypeUtil.rawTypeOf(C.class.getField("f2").getGenericType())).isSameAs(Map.class);
        assertThat(TypeUtil.rawTypeOf(C.class.getField("f3").getGenericType())).isSameAs(List[].class);
        assertThat(TypeUtil.rawTypeOf(C.class.getField("f4").getGenericType())).isSameAs(List.class);
        assertThat(TypeUtil.rawTypeOf(C.class.getField("f5").getGenericType())).isSameAs(List[][].class);

        WildcardType wt = (WildcardType) ((ParameterizedType) C.class.getField("f4").getGenericType()).getActualTypeArguments()[0];
        assertThat(TypeUtil.rawTypeOf(wt)).isSameAs(Object.class);

        assertThat(TypeUtil.rawTypeOf(C.class.getTypeParameters()[0])).isSameAs(Object.class);

        assertThatThrownBy(() -> TypeUtil.rawTypeOf(new Type() {})).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    /** Tests {@link TypeUtil#typeVariableNamesOf(Type)}. */
    @Test
    public void typeVariableNamesOf() throws Exception {
        npe(() -> TypeUtil.typeVariableNamesOf(null), "type");

        // Types with no type variables - should return empty set
        assertThat(TypeUtil.typeVariableNamesOf(String.class)).isEmpty();
        assertThat(TypeUtil.typeVariableNamesOf(String[].class)).isEmpty();

        @SuppressWarnings("unused")
        class NoVars {
            public List<String> f1;
            public Map<String, Integer> f2;
            public List<?> f3;
            public List<? extends String> f4;
        }

        for (Field f : NoVars.class.getDeclaredFields()) {
            if (!f.isSynthetic()) {
                assertThat(TypeUtil.typeVariableNamesOf(f.getGenericType()))
                    .as("Field %s should have no type variables", f.getName())
                    .isEmpty();
            }
        }

        // Types with type variables
        @SuppressWarnings("unused")
        class WithVars<T, K, V, A, B> {
            public T single;
            public List<T> inList;
            public T[] inArray;
            public Map<K, V> twoVars;
            public List<? extends T> inWildcardUpper;
            public List<? super T> inWildcardLower;
            public Map<? extends Collection<? super T>, K> deepNested;
            public Map<T, List<T>> sameVarTwice;
            public Map<B, A> orderTest;
        }

        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("single").getGenericType()))
            .containsExactly("T");

        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("inList").getGenericType()))
            .containsExactly("T");

        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("inArray").getGenericType()))
            .containsExactly("T");

        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("twoVars").getGenericType()))
            .containsExactly("K", "V");

        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("inWildcardUpper").getGenericType()))
            .containsExactly("T");

        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("inWildcardLower").getGenericType()))
            .containsExactly("T");

        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("deepNested").getGenericType()))
            .containsExactly("T", "K");

        // Same variable appearing multiple times should only be in set once
        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("sameVarTwice").getGenericType()))
            .containsExactly("T");

        // Verify order is preserved (LinkedHashSet)
        assertThat(TypeUtil.typeVariableNamesOf(WithVars.class.getField("orderTest").getGenericType()))
            .containsExactly("B", "A");
    }

    class NestedNonStaticClass {}

    static class NestedStaticClass {}
}
