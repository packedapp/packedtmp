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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static support.assertj.Assertions.npe;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

/** Tests {@link TypeLiteral}. */
public class TypeLiteralTest {

    /** Tests that we can make a custom type literal to check that T is passed down to super classes. */
    @Test
    public void tl_extendTypeLiterable() {

        /** A custom type literal to check that T is passed down to super classes. */
        class MyTypeLiteral<T> extends TypeLiteral<T> {}

        MyTypeLiteral<Integer> integerNew = new MyTypeLiteral<Integer>() {};

        assertThat(integerNew.box().getType()).isSameAs(Integer.class);

        assertThat(integerNew.getRawType()).isSameAs(Integer.class);
        assertThat(integerNew.getType()).isSameAs(Integer.class);

        assertThat(integerNew).hasSameHashCodeAs(Integer.class);
        assertThat(integerNew).hasSameHashCodeAs(TypeLiteral.of(Integer.class).hashCode());
        assertThat(integerNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(Integer.class).hashCode());

        assertThat(integerNew).isEqualTo(TypeLiteral.of(Integer.class));
        assertThat(integerNew).isEqualTo(TypeLiteral.fromJavaImplementationType(Integer.class));

        assertThat(integerNew).isNotEqualTo(Integer.class);
        assertThat(integerNew).isNotEqualTo(TypeLiteral.of(Long.class));

        assertThat(integerNew).hasToString("java.lang.Integer");
        assertThat(integerNew.toShortString()).isEqualTo("Integer");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_int() {
        TypeLiteral<Integer> intOf = TypeLiteral.of(int.class);

        assertThat(intOf.box().getType()).isSameAs(Integer.class);

        assertThat(intOf.getRawType()).isSameAs(int.class);
        assertThat(intOf.getType()).isSameAs(int.class);

        assertThat(intOf).hasSameHashCodeAs(int.class);
        assertThat(intOf).hasSameHashCodeAs(TypeLiteral.of(int.class).hashCode());

        assertThat(intOf).isEqualTo(TypeLiteral.of(int.class));

        assertThat(intOf).isNotEqualTo(int.class);
        assertThat(intOf).isNotEqualTo(TypeLiteral.of(long.class));

        assertThat(intOf).hasToString("int");
        assertThat(intOf.toShortString()).isEqualTo("int");
    }

    /** Tests {@code int[]} as a type literal. */
    @Test
    public void tl_intArray() {
        TypeLiteral<int[]> intArrayOf = TypeLiteral.of(int[].class);

        assertThat(intArrayOf.box().getType()).isSameAs(int[].class);

        assertThat(intArrayOf.getRawType()).isSameAs(int[].class);
        assertThat(intArrayOf.getType()).isSameAs(int[].class);

        assertThat(intArrayOf).hasSameHashCodeAs(int[].class);
        assertThat(intArrayOf).hasSameHashCodeAs(TypeLiteral.of(int[].class).hashCode());

        assertThat(intArrayOf).isEqualTo(TypeLiteral.of(int[].class));

        assertThat(intArrayOf).isNotEqualTo(int[].class);
        assertThat(intArrayOf).isNotEqualTo(TypeLiteral.of(long[].class));

        assertThat(intArrayOf).hasToString("int[]");
        assertThat(intArrayOf.toShortString()).isEqualTo("int[]");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_Integer() {
        TypeLiteral<Integer> integerNew = new TypeLiteral<Integer>() {};

        assertThat(integerNew.box().getType()).isSameAs(Integer.class);

        assertThat(integerNew.getRawType()).isSameAs(Integer.class);
        assertThat(integerNew.getType()).isSameAs(Integer.class);

        assertThat(integerNew).hasSameHashCodeAs(Integer.class);
        assertThat(integerNew).hasSameHashCodeAs(TypeLiteral.of(Integer.class).hashCode());
        assertThat(integerNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(Integer.class).hashCode());

        assertThat(integerNew).isEqualTo(TypeLiteral.of(Integer.class));
        assertThat(integerNew).isEqualTo(TypeLiteral.fromJavaImplementationType(Integer.class));

        assertThat(integerNew).isNotEqualTo(Integer.class);
        assertThat(integerNew).isNotEqualTo(TypeLiteral.of(Long.class));

        assertThat(integerNew).hasToString("java.lang.Integer");
        assertThat(integerNew.toShortString()).isEqualTo("Integer");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_IntegerArray() {
        TypeLiteral<Integer[]> integerNew = new TypeLiteral<Integer[]>() {};

        assertThat(integerNew.box().getType()).isSameAs(Integer[].class);

        assertThat(integerNew.getRawType()).isSameAs(Integer[].class);
        assertThat(integerNew.getType()).isSameAs(Integer[].class);

        assertThat(integerNew).hasSameHashCodeAs(Integer[].class);
        assertThat(integerNew).hasSameHashCodeAs(TypeLiteral.of(Integer[].class).hashCode());
        assertThat(integerNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(Integer[].class).hashCode());

        assertThat(integerNew).isEqualTo(TypeLiteral.of(Integer[].class));
        assertThat(integerNew).isEqualTo(TypeLiteral.fromJavaImplementationType(Integer[].class));

        assertThat(integerNew).isNotEqualTo(Integer[].class);
        assertThat(integerNew).isNotEqualTo(TypeLiteral.of(Long[].class));

        assertThat(integerNew).hasToString("java.lang.Integer[]");
        assertThat(integerNew.toShortString()).isEqualTo("Integer[]");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_IntegerArrayArray() {
        TypeLiteral<Integer[][]> integerArrayArrayNew = new TypeLiteral<Integer[][]>() {};

        assertThat(integerArrayArrayNew.box().getType()).isSameAs(Integer[][].class);

        assertThat(integerArrayArrayNew.getRawType()).isSameAs(Integer[][].class);
        assertThat(integerArrayArrayNew.getType()).isSameAs(Integer[][].class);

        assertThat(integerArrayArrayNew).hasSameHashCodeAs(Integer[][].class);
        assertThat(integerArrayArrayNew).hasSameHashCodeAs(TypeLiteral.of(Integer[][].class).hashCode());
        assertThat(integerArrayArrayNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(Integer[][].class).hashCode());

        assertThat(integerArrayArrayNew).isEqualTo(TypeLiteral.of(Integer[][].class));
        assertThat(integerArrayArrayNew).isEqualTo(TypeLiteral.fromJavaImplementationType(Integer[][].class));

        assertThat(integerArrayArrayNew).isNotEqualTo(Integer[][].class);

        assertThat(integerArrayArrayNew).hasToString("java.lang.Integer[][]");
        assertThat(integerArrayArrayNew.toShortString()).isEqualTo("Integer[][]");
    }

    /** Tests {@code List<String>}. */
    @Test
    public void tl_ListString() throws Exception {
        TypeLiteral<List<String>> listStringNew = new TypeLiteral<List<String>>() {};
        // Type
        class Tmpx {
            @SuppressWarnings("unused")
            List<String> f;
        }
        Type fGenericType = Tmpx.class.getDeclaredField("f").getGenericType();

        assertThat(listStringNew.box().getType()).isEqualTo(fGenericType);

        assertThat(listStringNew.getRawType()).isSameAs(List.class);

        assertThat(listStringNew.getType()).isEqualTo(fGenericType);

        assertThat(listStringNew).hasSameHashCodeAs(fGenericType);
        assertThat(listStringNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(fGenericType).hashCode());

        assertThat(listStringNew).isEqualTo(TypeLiteral.fromJavaImplementationType(fGenericType));
        assertThat(listStringNew).isNotEqualTo(List.class);

        assertThat(listStringNew).hasToString("java.util.List<java.lang.String>");
        assertThat(listStringNew.toShortString()).isEqualTo("List<String>");
    }

    /** Tests {@code List<String>}. */
    @Test
    public void tl_ListWildcard() throws Exception {
        TypeLiteral<List<?>> listWildcardNew = new TypeLiteral<List<?>>() {};
        // Type
        class Tmpx {
            @SuppressWarnings("unused")
            List<?> f;
        }
        Type fGenericType = Tmpx.class.getDeclaredField("f").getGenericType();

        assertThat(listWildcardNew.box().getType()).isEqualTo(fGenericType);

        assertThat(listWildcardNew.getRawType()).isSameAs(List.class);

        assertThat(listWildcardNew.getType()).isEqualTo(fGenericType);

        assertThat(listWildcardNew).hasSameHashCodeAs(fGenericType);
        assertThat(listWildcardNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(fGenericType).hashCode());

        assertThat(listWildcardNew).isEqualTo(TypeLiteral.fromJavaImplementationType(fGenericType));
        assertThat(listWildcardNew).isNotEqualTo(List.class);

        assertThat(listWildcardNew).hasToString("java.util.List<?>");
        assertThat(listWildcardNew.toShortString()).isEqualTo("List<?>");
    }

    /** Tests {@code Map<? extends String, ? super Integer>}. */
    @Test
    public void tl_mapItsComplicated() throws Exception {
        TypeLiteral<Map<? extends String, ? super Integer>> listStringNew = new TypeLiteral<Map<? extends String, ? super Integer>>() {};
        // Type
        class Tmpx {
            @SuppressWarnings("unused")
            Map<? extends String, ? super Integer> f;
        }
        Type fGenericType = Tmpx.class.getDeclaredField("f").getGenericType();

        assertThat(listStringNew.box().getType()).isEqualTo(fGenericType);

        assertThat(listStringNew.getRawType()).isSameAs(Map.class);

        assertThat(listStringNew.getType()).isEqualTo(fGenericType);

        assertThat(listStringNew).hasSameHashCodeAs(fGenericType);
        assertThat(listStringNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(fGenericType).hashCode());

        assertThat(listStringNew).isEqualTo(TypeLiteral.fromJavaImplementationType(fGenericType));
        assertThat(listStringNew).isNotEqualTo(Map.class);

        assertThat(listStringNew).hasToString("java.util.Map<? extends java.lang.String, ? super java.lang.Integer>");
        assertThat(listStringNew.toShortString()).isEqualTo("Map<? extends String, ? super Integer>");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_String() {
        TypeLiteral<String> stringNew = new TypeLiteral<String>() {};

        assertThat(stringNew.box().getType()).isSameAs(String.class);

        assertThat(stringNew.getRawType()).isSameAs(String.class);
        assertThat(stringNew.getType()).isSameAs(String.class);

        assertThat(stringNew).hasSameHashCodeAs(String.class);
        assertThat(stringNew).hasSameHashCodeAs(TypeLiteral.of(String.class).hashCode());
        assertThat(stringNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(String.class).hashCode());

        assertThat(stringNew).isEqualTo(TypeLiteral.of(String.class));
        assertThat(stringNew).isEqualTo(TypeLiteral.fromJavaImplementationType(String.class));

        assertThat(stringNew).isNotEqualTo(String.class);

        assertThat(stringNew).hasToString("java.lang.String");
        assertThat(stringNew.toShortString()).isEqualTo("String");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_void() {
        TypeLiteral<Void> intOf = TypeLiteral.of(void.class);

        assertThat(intOf.box().getType()).isSameAs(Void.class);

        assertThat(intOf.getRawType()).isSameAs(void.class);
        assertThat(intOf.getType()).isSameAs(void.class);

        assertThat(intOf).hasSameHashCodeAs(void.class);
        assertThat(intOf).hasSameHashCodeAs(TypeLiteral.of(void.class).hashCode());

        assertThat(intOf).isEqualTo(TypeLiteral.of(void.class));

        assertThat(intOf).isNotEqualTo(void.class);
        assertThat(intOf).isNotEqualTo(TypeLiteral.of(long.class));

        assertThat(intOf).hasToString("void");
        assertThat(intOf.toShortString()).isEqualTo("void");
    }

    /** Tests a type literal with a type variable (T) */
    @Test
    public void tl_withTypeVariable() throws Exception {
        // Type
        class Tmpx<T> {
            @SuppressWarnings("unused")
            Map<T, ?> f;
        }
        Type fGenericType = Tmpx.class.getDeclaredField("f").getGenericType();
        TypeLiteral<?> typeVariable = TypeLiteral.fromJavaImplementationType(fGenericType);

        assertThat(typeVariable.box().getType()).isEqualTo(fGenericType);

        assertThat(typeVariable.getRawType()).isSameAs(Map.class);

        assertThat(typeVariable.getType()).isEqualTo(fGenericType);

        assertThat(typeVariable).hasSameHashCodeAs(fGenericType);

        assertThat(typeVariable).isNotEqualTo(Map.class);

        assertThat(typeVariable).hasToString("java.util.Map<T, ?>");
        assertThat(typeVariable.toShortString()).isEqualTo("Map<T, ?>");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void UnknownTypeVariable() {
        assertThatThrownBy(() -> new TypeLiteral() {}).hasNoCause();
        assertThatThrownBy(() -> new TypeLiteral() {}).isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TypeLiteral() {})
                .hasMessageStartingWith("Cannot determine type variable <T> for TypeLiteral<T> on class app.packed.inject.TypeLiteralTest");

        /** A custom type literal to check that T is passed down to super classes. */
        class MyTypeLiteral<T> extends TypeLiteral<T> {}
        assertThatThrownBy(() -> new MyTypeLiteral() {})
                .hasMessageStartingWith("Cannot determine type variable <T> for TypeLiteral<T> on class app.packed.inject.TypeLiteralTest");

    }

    /** Tests {@link TypeLiteral#fromField(Field)}. */
    @Test
    public void fromField() throws Exception {
        class Tmpx<T> {
            @SuppressWarnings("unused")
            Integer f;
        }
        Field f = Tmpx.class.getDeclaredField("f");

        npe(TypeLiteral::fromField, f, "field");
        assertThat(TypeLiteral.of(Integer.class)).isEqualTo(TypeLiteral.fromField(f));
    }

    /** Tests {@link TypeLiteral#fromParameter(Parameter)}. */
    @Test
    public void fromParameter() throws Exception {
        class Tmpx<T> {
            @SuppressWarnings("unused")
            Tmpx(Integer f) {}
        }
        //Tmpx is a non-static class so first parameter is TypeLiteralTest
        Parameter p = Tmpx.class.getDeclaredConstructors()[0].getParameters()[1];

        npe(TypeLiteral::fromParameter, p, "parameter");
        assertThat(TypeLiteral.of(Integer.class)).isEqualTo(TypeLiteral.fromParameter(p));
    }

    public static <T> void npex(Class<T> cl, Consumer<T> c, String name) {
        assertThatNullPointerException().isThrownBy(() -> c.accept(null)).withMessage(name + " is null").withNoCause();
    }

    public static void npe2(Consumer<?> c, String name) {
        assertThatNullPointerException().isThrownBy(() -> c.accept(null)).withMessage(name + " is null").withNoCause();
    }
}
