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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static testutil.assertj.Assertions.npe;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import packed.internal.util.types.TypeUtil;

/** Tests {@link TypeUtil}. */
public class TypeUtilTest {

    @Test
    public void checkClassIsInstantiable() {
        try {
            TypeUtil.checkClassIsInstantiable(Test.class);
            fail("oops");
        } catch (IllegalArgumentException ok) {}
        try {
            TypeUtil.checkClassIsInstantiable(Map.class);
            fail("oops");
        } catch (IllegalArgumentException ok) {}
        try {
            TypeUtil.checkClassIsInstantiable(Object[].class);
            fail("oops");
        } catch (IllegalArgumentException ok) {}
        try {
            TypeUtil.checkClassIsInstantiable(AbstractMap.class);
            fail("oops");
        } catch (IllegalArgumentException ok) {}

        try {
            TypeUtil.checkClassIsInstantiable(Integer.TYPE);
            fail("oops");
        } catch (IllegalArgumentException ok) {}

        assertThat(TypeUtil.checkClassIsInstantiable(HashMap.class)).isSameAs(HashMap.class);
    }

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

    /** Tests {@link TypeUtil#findRawType(Type)}. */
    @Test
    public void findRawType() throws Exception {
        @SuppressWarnings("unused")
        class C<T> {
            public List<String> f1;
            public Map<? extends String, ? super Integer> f2;
            public List<String>[] f3;
            public List<?> f4;
        }
        npe(() -> TypeUtil.findRawType(null), "type");
        assertThat(TypeUtil.findRawType(String.class)).isSameAs(String.class);
        assertThat(TypeUtil.findRawType(String[].class)).isSameAs(String[].class);

        assertThat(TypeUtil.findRawType(C.class.getField("f1").getGenericType())).isSameAs(List.class);
        assertThat(TypeUtil.findRawType(C.class.getField("f2").getGenericType())).isSameAs(Map.class);
        assertThat(TypeUtil.findRawType(C.class.getField("f3").getGenericType())).isSameAs(List[].class);
        assertThat(TypeUtil.findRawType(C.class.getField("f4").getGenericType())).isSameAs(List.class);

        WildcardType wt = (WildcardType) ((ParameterizedType) C.class.getField("f4").getGenericType()).getActualTypeArguments()[0];
        assertThat(TypeUtil.findRawType(wt)).isSameAs(Object.class);

        assertThat(TypeUtil.findRawType(C.class.getTypeParameters()[0])).isSameAs(Object.class);

        assertThatThrownBy(() -> TypeUtil.findRawType(new Type() {})).isExactlyInstanceOf(IllegalArgumentException.class);
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

    /** Tests {@link TypeUtil#isInnerOrLocalClass(Class)}. */
    @Test
    public void isInnerOrLocalClass() {
        assertThat(TypeUtil.isInnerOrLocalClass(TypeUtilTest.class)).isFalse();
        assertThat(TypeUtil.isInnerOrLocalClass(NestedStaticClass.class)).isFalse();

        class LocalClass {}
        assertThat(TypeUtil.isInnerOrLocalClass(LocalClass.class)).isTrue();
        assertThat(TypeUtil.isInnerOrLocalClass(NestedNonStaticClass.class)).isTrue();
        // TODO should we include anonymous class??
        // assertThat(TypeUtil.isInnerOrLocalClass(new Object() {}.getClass())).isTrue();
    }

    /** Tests {@link TypeUtil#isOptionalType(Class)}. */
    @Test
    public void isOptionalType() {
        assertThat(TypeUtil.isOptionalType(String.class)).isFalse();
        assertThat(TypeUtil.isOptionalType(null)).isFalse();
        assertThat(TypeUtil.isOptionalType(Optional.class)).isTrue();
        assertThat(TypeUtil.isOptionalType(OptionalLong.class)).isTrue();
        assertThat(TypeUtil.isOptionalType(OptionalInt.class)).isTrue();
        assertThat(TypeUtil.isOptionalType(OptionalDouble.class)).isTrue();
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

    class NestedNonStaticClass {}

    static class NestedStaticClass {}
}
