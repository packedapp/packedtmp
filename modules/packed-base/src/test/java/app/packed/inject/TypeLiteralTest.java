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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static support.assertj.Assertions.npe;
import static support.stubs.TypeStubs.LIST_STRING;
import static support.stubs.TypeStubs.LIST_WILDCARD;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.junit.jupiter.api.Test;

import app.packed.util.InvalidDeclarationException;
import support.stubs.annotation.AnnotationInstances;

/** Tests {@link TypeLiteral}. */
public class TypeLiteralTest {

    static final TypeLiteral<Integer> TL_INTEGER = new TypeLiteral<Integer>() {};

    @Test
    public <S> void toKey() {
        TypeLiteral<Integer> tl1 = TypeLiteral.of(Integer.class);

        Key<Integer> k1 = tl1.toKey();
        Key<Integer> k2 = TL_INTEGER.toKey();

        assertThat(k1.getTypeLiteral()).isSameAs(tl1);
        assertThat(k2.getTypeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(k2.getTypeLiteral()).isNotSameAs(TL_INTEGER);

        assertThat(k1.hasQualifier()).isFalse();
        assertThat(k2.hasQualifier()).isFalse();

        // Optional
        assertThatThrownBy(() -> new TypeLiteral<Optional<Integer>>() {}.toKey()).isExactlyInstanceOf(InvalidDeclarationException.class)
                .hasMessage("Cannot convert an optional type (Optional<Integer>) to a Key, as keys cannot be optional");
        assertThatThrownBy(() -> new TypeLiteral<OptionalInt>() {}.toKey()).isExactlyInstanceOf(InvalidDeclarationException.class)
                .hasMessage("Cannot convert an optional type (OptionalInt) to a Key, as keys cannot be optional");
        assertThatThrownBy(() -> new TypeLiteral<OptionalLong>() {}.toKey()).isExactlyInstanceOf(InvalidDeclarationException.class)
                .hasMessage("Cannot convert an optional type (OptionalLong) to a Key, as keys cannot be optional");
        assertThatThrownBy(() -> new TypeLiteral<OptionalDouble>() {}.toKey()).isExactlyInstanceOf(InvalidDeclarationException.class)
                .hasMessage("Cannot convert an optional type (OptionalDouble) to a Key, as keys cannot be optional");

        // We need to use this old fashion way because of
        try {
            new TypeLiteral<List<S>>() {}.toKey();
            fail("should have failed");
        } catch (InvalidDeclarationException e) {
            assertThat(e).hasMessage("Can only convert type literals that are free from type variables to a Key, however TypeVariable<List<S>> defined: [S]");
        }
    }

    @Test
    public <S> void toKeyAnnotation() {
        npe(() -> TL_INTEGER.toKey(null), "qualifier");

        Annotation nonQualified = Arrays.stream(TypeLiteralTest.class.getDeclaredMethods()).filter(m -> m.getName().equals("toKeyAnnotation")).findFirst().get()
                .getAnnotations()[0];
        assertThatThrownBy(() -> TL_INTEGER.toKey(nonQualified)).isExactlyInstanceOf(InvalidDeclarationException.class)
                .hasMessage("@org.junit.jupiter.api.Test is not a valid qualifier. The annotation must be annotated with @Qualifier");

        Key<Integer> key = TL_INTEGER.toKey(AnnotationInstances.NO_VALUE_QUALIFIER);
        assertThat(key.getTypeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(key.getQualifier()).hasValue(AnnotationInstances.NO_VALUE_QUALIFIER);
    }

    @Test
    public void canonicalize() {
        TypeLiteral<Integer> tl1 = TypeLiteral.of(Integer.class);

        assertThat(tl1).isEqualTo(TL_INTEGER);

        assertThat(tl1).isSameAs(tl1.canonicalize());
        assertThat(TL_INTEGER).isNotSameAs(TL_INTEGER.canonicalize());
    }

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
        assertThat(integerNew).isEqualTo(integerNew.canonicalize());
        assertThat(integerNew).isNotSameAs(integerNew.canonicalize());
        assertThat(integerNew).isEqualTo(TypeLiteral.fromJavaImplementationType(Integer.class));

        assertThat(integerNew).isNotEqualTo(Integer.class);
        assertThat(integerNew).isNotEqualTo(TypeLiteral.of(Long.class));

        assertThat(integerNew).hasToString("java.lang.Integer");
        assertThat(integerNew.toStringSimple()).isEqualTo("Integer");
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
        assertThat(intOf).isEqualTo(intOf.canonicalize());

        assertThat(intOf).isNotEqualTo(int.class);
        assertThat(intOf).isNotEqualTo(TypeLiteral.of(long.class));

        assertThat(intOf).hasToString("int");
        assertThat(intOf.toStringSimple()).isEqualTo("int");
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
        assertThat(intArrayOf).isEqualTo(intArrayOf.canonicalize());

        assertThat(intArrayOf).isNotEqualTo(int[].class);
        assertThat(intArrayOf).isNotEqualTo(TypeLiteral.of(long[].class));

        assertThat(intArrayOf).hasToString("int[]");
        assertThat(intArrayOf.toStringSimple()).isEqualTo("int[]");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_Integer() {
        assertThat(TL_INTEGER.box().getType()).isSameAs(Integer.class);

        assertThat(TL_INTEGER.getRawType()).isSameAs(Integer.class);
        assertThat(TL_INTEGER.getType()).isSameAs(Integer.class);

        assertThat(TL_INTEGER).hasSameHashCodeAs(Integer.class);
        assertThat(TL_INTEGER).hasSameHashCodeAs(TypeLiteral.of(Integer.class).hashCode());
        assertThat(TL_INTEGER).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(Integer.class).hashCode());

        assertThat(TL_INTEGER).isEqualTo(TypeLiteral.of(Integer.class));
        assertThat(TL_INTEGER).isEqualTo(TypeLiteral.fromJavaImplementationType(Integer.class));
        assertThat(TL_INTEGER).isEqualTo(TL_INTEGER.canonicalize());

        assertThat(TL_INTEGER).isNotEqualTo(Integer.class);
        assertThat(TL_INTEGER).isNotEqualTo(TypeLiteral.of(Long.class));

        assertThat(TL_INTEGER).hasToString("java.lang.Integer");
        assertThat(TL_INTEGER.toStringSimple()).isEqualTo("Integer");
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
        assertThat(integerNew).isEqualTo(integerNew.canonicalize());

        assertThat(integerNew).isNotEqualTo(Integer[].class);
        assertThat(integerNew).isNotEqualTo(TypeLiteral.of(Long[].class));

        assertThat(integerNew).hasToString("java.lang.Integer[]");
        assertThat(integerNew.toStringSimple()).isEqualTo("Integer[]");
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
        assertThat(integerArrayArrayNew).isEqualTo(integerArrayArrayNew.canonicalize());

        assertThat(integerArrayArrayNew).isNotEqualTo(Integer[][].class);

        assertThat(integerArrayArrayNew).hasToString("java.lang.Integer[][]");
        assertThat(integerArrayArrayNew.toStringSimple()).isEqualTo("Integer[][]");
    }

    /** Tests {@code List<String>}. */
    @Test
    public void tl_ListString() throws Exception {
        TypeLiteral<List<String>> listStringNew = new TypeLiteral<List<String>>() {};

        assertThat(listStringNew.box().getType()).isEqualTo(LIST_STRING);

        assertThat(listStringNew.getRawType()).isSameAs(List.class);

        assertThat(listStringNew.getType()).isEqualTo(LIST_STRING);

        assertThat(listStringNew).hasSameHashCodeAs(LIST_STRING);
        assertThat(listStringNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(LIST_STRING).hashCode());

        assertThat(listStringNew).isEqualTo(TypeLiteral.fromJavaImplementationType(LIST_STRING));
        assertThat(listStringNew).isNotEqualTo(List.class);
        assertThat(listStringNew).isEqualTo(listStringNew.canonicalize());

        assertThat(listStringNew).hasToString("java.util.List<java.lang.String>");
        assertThat(listStringNew.toStringSimple()).isEqualTo("List<String>");
    }

    /** Tests {@code List<?>}. */
    @Test
    public void tl_ListWildcard() throws Exception {
        TypeLiteral<List<?>> listWildcardNew = new TypeLiteral<List<?>>() {};

        assertThat(listWildcardNew.box().getType()).isEqualTo(LIST_WILDCARD);

        assertThat(listWildcardNew.getRawType()).isSameAs(List.class);

        assertThat(listWildcardNew.getType()).isEqualTo(LIST_WILDCARD);

        assertThat(listWildcardNew).hasSameHashCodeAs(LIST_WILDCARD);
        assertThat(listWildcardNew).hasSameHashCodeAs(TypeLiteral.fromJavaImplementationType(LIST_WILDCARD).hashCode());

        assertThat(listWildcardNew).isEqualTo(TypeLiteral.fromJavaImplementationType(LIST_WILDCARD));
        assertThat(listWildcardNew).isNotEqualTo(List.class);
        assertThat(listWildcardNew).isEqualTo(listWildcardNew.canonicalize());

        assertThat(listWildcardNew).hasToString("java.util.List<?>");
        assertThat(listWildcardNew.toStringSimple()).isEqualTo("List<?>");
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
        assertThat(listStringNew).isEqualTo(listStringNew.canonicalize());
        assertThat(listStringNew).isNotEqualTo(Map.class);

        assertThat(listStringNew).hasToString("java.util.Map<? extends java.lang.String, ? super java.lang.Integer>");
        assertThat(listStringNew.toStringSimple()).isEqualTo("Map<? extends String, ? super Integer>");
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

        assertThat(stringNew).isEqualTo(stringNew.canonicalize());
        assertThat(stringNew).isEqualTo(TypeLiteral.of(String.class));
        assertThat(stringNew).isEqualTo(TypeLiteral.fromJavaImplementationType(String.class));

        assertThat(stringNew).isNotEqualTo(String.class);

        assertThat(stringNew).hasToString("java.lang.String");
        assertThat(stringNew.toStringSimple()).isEqualTo("String");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_void() {
        TypeLiteral<Void> voidOf = TypeLiteral.of(void.class);

        assertThat(voidOf.box().getType()).isSameAs(Void.class);

        assertThat(voidOf.getRawType()).isSameAs(void.class);
        assertThat(voidOf.getType()).isSameAs(void.class);

        assertThat(voidOf).hasSameHashCodeAs(void.class);
        assertThat(voidOf).hasSameHashCodeAs(TypeLiteral.of(void.class).hashCode());

        assertThat(voidOf).isEqualTo(TypeLiteral.of(void.class));
        assertThat(voidOf).isEqualTo(voidOf.canonicalize());

        assertThat(voidOf).isNotEqualTo(void.class);
        assertThat(voidOf).isNotEqualTo(TypeLiteral.of(long.class));

        assertThat(voidOf).hasToString("void");
        assertThat(voidOf.toStringSimple()).isEqualTo("void");
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

        assertThat(typeVariable).isEqualTo(typeVariable.canonicalize());
        assertThat(typeVariable).isNotEqualTo(Map.class);

        assertThat(typeVariable).hasToString("java.util.Map<T, ?>");
        assertThat(typeVariable.toStringSimple()).isEqualTo("Map<T, ?>");
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
        @SuppressWarnings("unused")
        class Tmpx<T> {
            Integer f;
            List<?> fq;
        }
        Field f = Tmpx.class.getDeclaredField("f");
        npe(TypeLiteral::fromField, f, "field");

        assertThat(TypeLiteral.of(Integer.class)).isEqualTo(TypeLiteral.fromField(f));

        assertThat(LIST_WILDCARD).isEqualTo(TypeLiteral.fromField(Tmpx.class.getDeclaredField("fq")).getType());
    }

    /** Tests {@link TypeLiteral#fromParameter(Parameter)}. */
    @Test
    public void fromParameter() throws Exception {
        class Tmpx<T> {
            @SuppressWarnings("unused")
            Tmpx(Integer f, List<?> l) {}
        }
        // Tmpx is a non-static class so first parameter is TypeLiteralTest
        Parameter p = Tmpx.class.getDeclaredConstructors()[0].getParameters()[1];

        npe(TypeLiteral::fromParameter, p, "parameter");
        assertThat(TypeLiteral.of(Integer.class)).isEqualTo(TypeLiteral.fromParameter(p));
        assertThat(LIST_WILDCARD).isEqualTo(TypeLiteral.fromParameter(Tmpx.class.getDeclaredConstructors()[0].getParameters()[2]).getType());
    }

    /** Tests {@link TypeLiteral#fromMethodReturnType(Method)}. */
    @Test
    public void fromMethodReturnType() throws Exception {
        class Tmpx<T> {
            @SuppressWarnings("unused")
            public List<?> foo() {
                throw new UnsupportedOperationException();
            }
        }
        Method m = Tmpx.class.getMethod("foo");
        npe(TypeLiteral::fromMethodReturnType, m, "method");
        assertThat(LIST_WILDCARD).isEqualTo(TypeLiteral.fromMethodReturnType(m).getType());
    }
}
