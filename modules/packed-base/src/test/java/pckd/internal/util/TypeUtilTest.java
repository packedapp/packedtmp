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
package pckd.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static support.assertj.Assertions.npe;
import static support.stubs.TypeStubs.LIST_STRING;
import static support.stubs.TypeStubs.LIST_STRING_ARRAY;
import static support.stubs.TypeStubs.LIST_STRING_ARRAY_ARRAY;
import static support.stubs.TypeStubs.LIST_WILDCARD;
import static support.stubs.TypeStubs.MAP_EXTENDSSTRING_SUPERINTEGER;
import static support.stubs.TypeStubs.MAP_STRING_INTEGER;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import pckd.internal.util.TypeUtil;

/** Tests {@link TypeUtil}. */
public class TypeUtilTest {

    /** Tests {@link TypeUtil#boxClass(Class)}. */
    @Test
    public void boxClass() {
        assertThat(TypeUtil.boxClass(String.class)).isSameAs(String.class);
        assertThat(TypeUtil.boxClass(boolean.class)).isSameAs(Boolean.class);
        assertThat(TypeUtil.boxClass(byte.class)).isSameAs(Byte.class);
        assertThat(TypeUtil.boxClass(char.class)).isSameAs(Character.class);
        assertThat(TypeUtil.boxClass(double.class)).isSameAs(Double.class);
        assertThat(TypeUtil.boxClass(float.class)).isSameAs(Float.class);
        assertThat(TypeUtil.boxClass(int.class)).isSameAs(Integer.class);
        assertThat(TypeUtil.boxClass(long.class)).isSameAs(Long.class);
        assertThat(TypeUtil.boxClass(short.class)).isSameAs(Short.class);
        assertThat(TypeUtil.boxClass(void.class)).isSameAs(Void.class);
    }

    /** Tests {@link TypeUtil#unboxClass(Class)}. */
    @Test
    public void unboxClass() {
        assertThat(TypeUtil.unboxClass(String.class)).isSameAs(String.class);
        assertThat(TypeUtil.unboxClass(Boolean.class)).isSameAs(boolean.class);
        assertThat(TypeUtil.unboxClass(Byte.class)).isSameAs(byte.class);
        assertThat(TypeUtil.unboxClass(Character.class)).isSameAs(char.class);
        assertThat(TypeUtil.unboxClass(Double.class)).isSameAs(double.class);
        assertThat(TypeUtil.unboxClass(Float.class)).isSameAs(float.class);
        assertThat(TypeUtil.unboxClass(Integer.class)).isSameAs(int.class);
        assertThat(TypeUtil.unboxClass(Long.class)).isSameAs(long.class);
        assertThat(TypeUtil.unboxClass(Short.class)).isSameAs(short.class);
        assertThat(TypeUtil.unboxClass(Void.class)).isSameAs(void.class);
    }

    /** Tests {@link TypeUtil#toShortString(java.lang.reflect.Type)}. */
    @Test
    public void toShortString() {
        npe(() -> TypeUtil.toShortString(null), "type");
        assertThat(TypeUtil.toShortString(String.class)).isEqualTo("String");
        assertThat(TypeUtil.toShortString(LIST_STRING)).isEqualTo("List<String>");
        assertThat(TypeUtil.toShortString(LIST_STRING_ARRAY)).isEqualTo("List<String>[]");
        assertThat(TypeUtil.toShortString(LIST_STRING_ARRAY_ARRAY)).isEqualTo("List<String>[][]");
        assertThat(TypeUtil.toShortString(MAP_STRING_INTEGER)).isEqualTo("Map<String, Integer>");

        class Y<T> {}
        assertThat(TypeUtil.toShortString(Y.class.getTypeParameters()[0])).isEqualTo("T");
        assertThat(TypeUtil.toShortString(LIST_WILDCARD)).isEqualTo("List<?>");
        assertThat(TypeUtil.toShortString(MAP_EXTENDSSTRING_SUPERINTEGER)).isEqualTo("Map<? extends String, ? super Integer>");

        assertThatThrownBy(() -> TypeUtil.toShortString(new Type() {})).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    /** Tests {@link TypeUtil#isFreeFromTypeVariables(Type)}. */
    @Test
    public void isFreeFromTypeVariables() {
        npe(() -> TypeUtil.isFreeFromTypeVariables(null), "type");

        @SuppressWarnings("unused")
        class C<T> {
            public String f1;
            public String[] f2;
            public List<String> f3;
            public Map<? extends String, ? super Integer> f4;
            public int[] f5;

            public T[] n1;
            public List<T> n2;
            public List<T>[] n3;
            public List<? extends T> n4;
            public List<? super Collection<? extends T>> n5;
        }

        for (Field f : C.class.getDeclaredFields()) {
            if (!f.isSynthetic()) {// Anonymous class capture this test class
                assertThat(TypeUtil.isFreeFromTypeVariables(f.getGenericType())).isEqualTo(f.getName().startsWith("f"));
            }
        }
        assertThatThrownBy(() -> TypeUtil.isFreeFromTypeVariables(new Type() {})).isExactlyInstanceOf(IllegalArgumentException.class);

    }
}
