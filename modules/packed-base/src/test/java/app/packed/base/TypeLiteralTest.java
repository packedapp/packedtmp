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
package app.packed.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static testutil.assertj.Assertions.npe;
import static testutil.stubs.TypeStubs.LIST_STRING;
import static testutil.stubs.TypeStubs.LIST_WILDCARD;
import static testutil.util.TestMemberFinder.findField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import app.packed.base.TypeToken.CanonicalizedTypeLiteral;
import testutil.stubs.annotation.AnnotationInstances;

/** Tests {@link TypeToken}. */
public class TypeLiteralTest {

    static final TypeToken<Integer> TL_INTEGER = new TypeToken<Integer>() {};
    static final TypeToken<List<?>> TL_LIST_WILDCARD = new TypeToken<List<?>>() {};

    @Test
    public void canonicalize() {
        TypeToken<Integer> tl1 = TypeToken.of(Integer.class);

        assertThat(tl1).isEqualTo(TL_INTEGER);

        assertThat(tl1).isSameAs(tl1.canonicalize());
        assertThat(TL_INTEGER).isNotSameAs(TL_INTEGER.canonicalize());
    }

    /** Tests {@link TypeToken#fromField(Field)}. */
    @Test
    public void fromField() throws Exception {
        @SuppressWarnings("unused")
        class Tmpx<T> {
            Integer f;
            List<?> fq;
        }
        Field f = findField(Tmpx.class, "f");
        npe(TypeToken::fromField, f, "field");

        assertThat(TypeToken.of(Integer.class)).isEqualTo(TypeToken.fromField(f));

        assertThat(LIST_WILDCARD).isEqualTo(TypeToken.fromField(findField(Tmpx.class, "fq")).type());
    }

    /** Tests {@link TypeToken#fromMethodReturnType(Method)}. */
    @Test
    public void fromMethodReturnType() throws Exception {
        class Tmpx<T> {
            @SuppressWarnings("unused")
            public List<?> foo() {
                throw new UnsupportedOperationException();
            }
        }
        Method m = Tmpx.class.getMethod("foo");
        npe(TypeToken::fromMethodReturnType, m, "method");
        assertThat(LIST_WILDCARD).isEqualTo(TypeToken.fromMethodReturnType(m).type());
    }

//  /** Tests {@link TypeToken#fromParameter(Parameter)}. */
//  @Test
//  public void fromParameter() throws Exception {
//    class Tmpx<T> {
//      @SuppressWarnings("unused")
//      Tmpx(Integer f, List<?> l) {}
//    }
//    // Tmpx is a non-static class so first parameter is TypeLiteralTest
//    Parameter p = Tmpx.class.getDeclaredConstructors()[0].getParameters()[1];
//
//    npe(TypeToken::fromParameter, p, "parameter");
//    assertThat(TypeToken.of(Integer.class)).isEqualTo(TypeToken.fromParameter(p));
//    assertThat(LIST_WILDCARD).isEqualTo(TypeToken.fromParameter(Tmpx.class.getDeclaredConstructors()[0].getParameters()[2]).type());
//  }

    /** Tests that we can make a custom type literal to check that T is passed down to super classes. */
    @Test
    public void tl_extendTypeLiterable() {

        /** A custom type literal to check that T is passed down to super classes. */
        class MyTypeLiteral<T> extends TypeToken<T> {}

        MyTypeLiteral<Integer> integerNew = new MyTypeLiteral<Integer>() {};

        assertThat(integerNew.wrap().type()).isSameAs(Integer.class);

        assertThat(integerNew.rawType()).isSameAs(Integer.class);
        assertThat(integerNew.type()).isSameAs(Integer.class);

        assertThat(integerNew).hasSameHashCodeAs(Integer.class);
        assertThat(integerNew).hasSameHashCodeAs(TypeToken.of(Integer.class).hashCode());
        assertThat(integerNew).hasSameHashCodeAs(new CanonicalizedTypeLiteral<>(Integer.class).hashCode());

        assertThat(integerNew).isEqualTo(TypeToken.of(Integer.class));
        assertThat(integerNew).isEqualTo(integerNew.canonicalize());
        assertThat(integerNew).isNotSameAs(integerNew.canonicalize());
        assertThat(integerNew).isEqualTo(new CanonicalizedTypeLiteral<>(Integer.class));

        assertThat(integerNew).isNotEqualTo(Integer.class);
        assertThat(integerNew).isNotEqualTo(TypeToken.of(Long.class));

        assertThat(integerNew).hasToString("java.lang.Integer");
        assertThat(integerNew.toStringSimple()).isEqualTo("Integer");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_int() {
        TypeToken<Integer> intOf = TypeToken.of(int.class);

        assertThat(intOf.wrap().type()).isSameAs(Integer.class);

        assertThat(intOf.rawType()).isSameAs(int.class);
        assertThat(intOf.type()).isSameAs(int.class);

        assertThat(intOf).hasSameHashCodeAs(int.class);
        assertThat(intOf).hasSameHashCodeAs(TypeToken.of(int.class).hashCode());

        assertThat(intOf).isEqualTo(TypeToken.of(int.class));
        assertThat(intOf).isEqualTo(intOf.canonicalize());

        assertThat(intOf).isNotEqualTo(int.class);
        assertThat(intOf).isNotEqualTo(TypeToken.of(long.class));

        assertThat(intOf).hasToString("int");
        assertThat(intOf.toStringSimple()).isEqualTo("int");
    }

    /** Tests {@code int[]} as a type literal. */
    @Test
    public void tl_intArray() {
        TypeToken<int[]> intArrayOf = TypeToken.of(int[].class);

        assertThat(intArrayOf.wrap().type()).isSameAs(int[].class);

        assertThat(intArrayOf.rawType()).isSameAs(int[].class);
        assertThat(intArrayOf.type()).isSameAs(int[].class);

        assertThat(intArrayOf).hasSameHashCodeAs(int[].class);
        assertThat(intArrayOf).hasSameHashCodeAs(TypeToken.of(int[].class).hashCode());

        assertThat(intArrayOf).isEqualTo(TypeToken.of(int[].class));
        assertThat(intArrayOf).isEqualTo(intArrayOf.canonicalize());

        assertThat(intArrayOf).isNotEqualTo(int[].class);
        assertThat(intArrayOf).isNotEqualTo(TypeToken.of(long[].class));

        assertThat(intArrayOf).hasToString("int[]");
        assertThat(intArrayOf.toStringSimple()).isEqualTo("int[]");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_Integer() {
        assertThat(TL_INTEGER.wrap().type()).isSameAs(Integer.class);

        assertThat(TL_INTEGER.rawType()).isSameAs(Integer.class);
        assertThat(TL_INTEGER.type()).isSameAs(Integer.class);

        assertThat(TL_INTEGER).hasSameHashCodeAs(Integer.class);
        assertThat(TL_INTEGER).hasSameHashCodeAs(TypeToken.of(Integer.class).hashCode());
        assertThat(TL_INTEGER).hasSameHashCodeAs(new CanonicalizedTypeLiteral<>(Integer.class).hashCode());

        assertThat(TL_INTEGER).isEqualTo(TypeToken.of(Integer.class));
        assertThat(TL_INTEGER).isEqualTo(new CanonicalizedTypeLiteral<>(Integer.class));
        assertThat(TL_INTEGER).isEqualTo(TL_INTEGER.canonicalize());

        assertThat(TL_INTEGER).isNotEqualTo(Integer.class);
        assertThat(TL_INTEGER).isNotEqualTo(TypeToken.of(Long.class));

        assertThat(TL_INTEGER).hasToString("java.lang.Integer");
        assertThat(TL_INTEGER.toStringSimple()).isEqualTo("Integer");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_IntegerArray() {
        TypeToken<Integer[]> integerNew = new TypeToken<Integer[]>() {};

        assertThat(integerNew.wrap().type()).isSameAs(Integer[].class);

        assertThat(integerNew.rawType()).isSameAs(Integer[].class);
        assertThat(integerNew.type()).isSameAs(Integer[].class);

        assertThat(integerNew).hasSameHashCodeAs(Integer[].class);
        assertThat(integerNew).hasSameHashCodeAs(TypeToken.of(Integer[].class).hashCode());
        assertThat(integerNew).hasSameHashCodeAs(new CanonicalizedTypeLiteral<>(Integer[].class).hashCode());

        assertThat(integerNew).isEqualTo(TypeToken.of(Integer[].class));
        assertThat(integerNew).isEqualTo(new CanonicalizedTypeLiteral<>(Integer[].class));
        assertThat(integerNew).isEqualTo(integerNew.canonicalize());

        assertThat(integerNew).isNotEqualTo(Integer[].class);
        assertThat(integerNew).isNotEqualTo(TypeToken.of(Long[].class));

        assertThat(integerNew).hasToString("java.lang.Integer[]");
        assertThat(integerNew.toStringSimple()).isEqualTo("Integer[]");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_IntegerArrayArray() {
        TypeToken<Integer[][]> integerArrayArrayNew = new TypeToken<Integer[][]>() {};

        assertThat(integerArrayArrayNew.wrap().type()).isSameAs(Integer[][].class);

        assertThat(integerArrayArrayNew.rawType()).isSameAs(Integer[][].class);
        assertThat(integerArrayArrayNew.type()).isSameAs(Integer[][].class);

        assertThat(integerArrayArrayNew).hasSameHashCodeAs(Integer[][].class);
        assertThat(integerArrayArrayNew).hasSameHashCodeAs(TypeToken.of(Integer[][].class).hashCode());
        assertThat(integerArrayArrayNew).hasSameHashCodeAs(new CanonicalizedTypeLiteral<>(Integer[][].class).hashCode());

        assertThat(integerArrayArrayNew).isEqualTo(TypeToken.of(Integer[][].class));
        assertThat(integerArrayArrayNew).isEqualTo(new CanonicalizedTypeLiteral<>(Integer[][].class));
        assertThat(integerArrayArrayNew).isEqualTo(integerArrayArrayNew.canonicalize());

        assertThat(integerArrayArrayNew).isNotEqualTo(Integer[][].class);

        assertThat(integerArrayArrayNew).hasToString("java.lang.Integer[][]");
        assertThat(integerArrayArrayNew.toStringSimple()).isEqualTo("Integer[][]");
    }

    /** Tests {@code List<String>}. */
    @Test
    public void tl_ListString() throws Exception {
        TypeToken<List<String>> listStringNew = new TypeToken<List<String>>() {};

        assertThat(listStringNew.wrap().type()).isEqualTo(LIST_STRING);

        assertThat(listStringNew.rawType()).isSameAs(List.class);

        assertThat(listStringNew.type()).isEqualTo(LIST_STRING);

        assertThat(listStringNew).hasSameHashCodeAs(LIST_STRING);
        assertThat(listStringNew).hasSameHashCodeAs(new CanonicalizedTypeLiteral<>(LIST_STRING).hashCode());

        assertThat(listStringNew).isEqualTo(new CanonicalizedTypeLiteral<>(LIST_STRING));
        assertThat(listStringNew).isNotEqualTo(List.class);
        assertThat(listStringNew).isEqualTo(listStringNew.canonicalize());

        assertThat(listStringNew).hasToString("java.util.List<java.lang.String>");
        assertThat(listStringNew.toStringSimple()).isEqualTo("List<String>");
    }

    /** Tests {@code List<?>}. */
    @Test
    public void tl_ListWildcard() throws Exception {
        assertThat(TL_LIST_WILDCARD.wrap().type()).isEqualTo(LIST_WILDCARD);

        assertThat(TL_LIST_WILDCARD.rawType()).isSameAs(List.class);

        assertThat(TL_LIST_WILDCARD.type()).isEqualTo(LIST_WILDCARD);

        assertThat(TL_LIST_WILDCARD).hasSameHashCodeAs(LIST_WILDCARD);
        assertThat(TL_LIST_WILDCARD).hasSameHashCodeAs(new CanonicalizedTypeLiteral<>(LIST_WILDCARD).hashCode());

        assertThat(TL_LIST_WILDCARD).isEqualTo(new CanonicalizedTypeLiteral<>(LIST_WILDCARD));
        assertThat(TL_LIST_WILDCARD).isNotEqualTo(List.class);
        assertThat(TL_LIST_WILDCARD).isEqualTo(TL_LIST_WILDCARD.canonicalize());

        assertThat(TL_LIST_WILDCARD).hasToString("java.util.List<?>");
        assertThat(TL_LIST_WILDCARD.toStringSimple()).isEqualTo("List<?>");
    }

    /** Tests {@code Map<? extends String, ? super Integer>}. */
    @Test
    public void tl_mapItsComplicated() throws Exception {
        TypeToken<Map<? extends String, ? super Integer>> listStringNew = new TypeToken<Map<? extends String, ? super Integer>>() {};
        // Type
        class Tmpx {
            @SuppressWarnings("unused")
            Map<? extends String, ? super Integer> f;
        }
        Type fGenericType = findField(Tmpx.class, "f").getGenericType();

        assertThat(listStringNew.wrap().type()).isEqualTo(fGenericType);

        assertThat(listStringNew.rawType()).isSameAs(Map.class);

        assertThat(listStringNew.type()).isEqualTo(fGenericType);

        assertThat(listStringNew).hasSameHashCodeAs(fGenericType);
        assertThat(listStringNew).hasSameHashCodeAs(new CanonicalizedTypeLiteral<>(fGenericType).hashCode());

        assertThat(listStringNew).isEqualTo(new CanonicalizedTypeLiteral<>(fGenericType));
        assertThat(listStringNew).isEqualTo(listStringNew.canonicalize());
        assertThat(listStringNew).isNotEqualTo(Map.class);

        assertThat(listStringNew).hasToString("java.util.Map<? extends java.lang.String, ? super java.lang.Integer>");
        assertThat(listStringNew.toStringSimple()).isEqualTo("Map<? extends String, ? super Integer>");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_String() {
        TypeToken<String> stringNew = new TypeToken<String>() {};

        assertThat(stringNew.wrap().type()).isSameAs(String.class);

        assertThat(stringNew.rawType()).isSameAs(String.class);
        assertThat(stringNew.type()).isSameAs(String.class);

        assertThat(stringNew).hasSameHashCodeAs(String.class);
        assertThat(stringNew).hasSameHashCodeAs(TypeToken.of(String.class).hashCode());
        assertThat(stringNew).hasSameHashCodeAs(new CanonicalizedTypeLiteral<>(String.class).hashCode());

        assertThat(stringNew).isEqualTo(stringNew.canonicalize());
        assertThat(stringNew).isEqualTo(TypeToken.of(String.class));
        assertThat(stringNew).isEqualTo(new CanonicalizedTypeLiteral<>(String.class));

        assertThat(stringNew).isNotEqualTo(String.class);

        assertThat(stringNew).hasToString("java.lang.String");
        assertThat(stringNew.toStringSimple()).isEqualTo("String");
    }

    /** Tests an primitive int type literal. */
    @Test
    public void tl_void() {
        TypeToken<Void> voidOf = TypeToken.of(void.class);

        assertThat(voidOf.wrap().type()).isSameAs(Void.class);

        assertThat(voidOf.rawType()).isSameAs(void.class);
        assertThat(voidOf.type()).isSameAs(void.class);

        assertThat(voidOf).hasSameHashCodeAs(void.class);
        assertThat(voidOf).hasSameHashCodeAs(TypeToken.of(void.class).hashCode());

        assertThat(voidOf).isEqualTo(TypeToken.of(void.class));
        assertThat(voidOf).isEqualTo(voidOf.canonicalize());

        assertThat(voidOf).isNotEqualTo(void.class);
        assertThat(voidOf).isNotEqualTo(TypeToken.of(long.class));

        assertThat(voidOf).hasToString("void");
        assertThat(voidOf.toStringSimple()).isEqualTo("void");
    }

    /** Tests a type literal with a type variable (T) */
    @Test
    public void tl_withTypeVariable() {
        // Type
        class Tmpx<T> {
            @SuppressWarnings("unused")
            Map<T, ?> f;
        }
        Type fGenericType = findField(Tmpx.class, "f").getGenericType();
        TypeToken<?> typeVariable = new CanonicalizedTypeLiteral<>(fGenericType);

        assertThat(typeVariable.wrap().type()).isEqualTo(fGenericType);

        assertThat(typeVariable.rawType()).isSameAs(Map.class);

        assertThat(typeVariable.type()).isEqualTo(fGenericType);

        assertThat(typeVariable).hasSameHashCodeAs(fGenericType);

        assertThat(typeVariable).isEqualTo(typeVariable.canonicalize());
        assertThat(typeVariable).isNotEqualTo(Map.class);

        assertThat(typeVariable).hasToString("java.util.Map<T, ?>");
        assertThat(typeVariable.toStringSimple()).isEqualTo("Map<T, ?>");
    }

    @Test
    public <S> void toKeyAnnotation() {
        npe(() -> Key.fromTypeLiteral(TL_INTEGER, null), "qualifier");

        Annotation nonQualified = Arrays.stream(TypeLiteralTest.class.getDeclaredMethods()).filter(m -> m.getName().equals("toKeyAnnotation")).findFirst().get()
                .getAnnotations()[0];
        assertThatThrownBy(() -> Key.fromTypeLiteral(TL_INTEGER, nonQualified)).isExactlyInstanceOf(InvalidDeclarationException.class)
                .hasMessage("@org.junit.jupiter.api.Test is not a valid qualifier. The annotation must be annotated with @Qualifier");

        Key<Integer> key = Key.fromTypeLiteral(TL_INTEGER, AnnotationInstances.NO_VALUE_QUALIFIER);
        assertThat(key.typeToken()).isEqualTo(TL_INTEGER);
        assertThat(key.qualifiers()).containsExactly(AnnotationInstances.NO_VALUE_QUALIFIER);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void UnknownTypeVariable() {
        assertThatThrownBy(() -> new TypeToken() {}).hasNoCause();
        assertThatThrownBy(() -> new TypeToken() {}).isExactlyInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TypeToken() {})
                .hasMessageStartingWith("Cannot determine type variable <T> for TypeToken<T> on class " + TypeLiteralTest.class.getCanonicalName());

        /** A custom type literal to check that T is passed down to super classes. */
        class MyTypeLiteral<T> extends TypeToken<T> {}
        assertThatThrownBy(() -> new MyTypeLiteral() {})
                .hasMessageStartingWith("Cannot determine type variable <T> for TypeToken<T> on class " + TypeLiteralTest.class.getCanonicalName());
    }
}
