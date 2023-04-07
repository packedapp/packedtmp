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
package app.packed.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static testutil.Assertions.npe;
import static testutil.MemberFinder.findField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import internal.app.packed.util.types.GenericType;
import testutil.stubs.Qualifiers;
import testutil.stubs.Qualifiers.CharQualifier;
import testutil.stubs.Qualifiers.IntQualifier;

/** Tests {@link Key}. */
public class KeyTest {

    static final Type TL_INTEGER = new GenericType<Integer>() {}.type();
    static final Type TL_LIST_WILDCARD = new GenericType<List<Object>>() {}.type(); // wildcards are filtered

    static final Key<Integer> KEY_INT_OF = Key.of(int.class);
    static final Key<Integer> KEY_INTEGER = new Key<>() {};
    static final Key<Integer> KEY_INTEGER_OF = Key.of(Integer.class);

    static final Key<Integer> KEY_INTEGER_X = new Key<@CharQualifier('X') Integer>() {};
    static final Key<Integer> KEY_INTEGER_X_OF = Key.of(Integer.class).withQualifier(Qualifiers.CHAR_QUALIFIER_X);
    static final Key<Integer> KEY_INTEGER_Y = new Key<@CharQualifier('Y') Integer>() {};
    static final Key<Integer> KEY_INTEGER_Y_OF = Key.of(Integer.class).withQualifier(Qualifiers.CHAR_QUALIFIER_Y);

    static final Key<List<String>> KEY_LIST_STRING = new Key<>() {};
    static final Key<List<?>> KEY_LIST_WILDCARD = new Key<>() {};

    static final Key<List<?>> KEY_LIST_WILDCARD_X = new Key<@CharQualifier('X') List<?>>() {};
    static final Key<String> KEY_STRING = new Key<>() {};

    static final Key<Map<? extends String, ?>> TL_MAP = new Key<>() {};

    @Test
    public void canonicalize() {
        Key<Integer> key = Key.of(Integer.class).withQualifier(Qualifiers.CHAR_QUALIFIER_X);

        assertThat(key).isEqualTo(KEY_INTEGER_X);

        assertThat(key).isSameAs(key.canonicalize());
        assertThat(KEY_INTEGER_X).isNotSameAs(KEY_INTEGER_X.canonicalize());
    }

    @Test
    public void equalsHashCode() {
        assertThat(KEY_INT_OF).isEqualTo(KEY_INT_OF).isEqualTo(KEY_INTEGER).isEqualTo(KEY_INTEGER_OF);
        assertThat(KEY_INTEGER).isEqualTo(KEY_INTEGER_OF).isEqualTo(new Key<Integer>() {});
        assertThat(KEY_INTEGER).isNotEqualTo(null).isNotEqualTo(Integer.class);

        assertThat(KEY_INTEGER).hasSameHashCodeAs(KEY_INTEGER).hasSameHashCodeAs(KEY_INTEGER_OF);
        assertThat(KEY_INTEGER).hasSameHashCodeAs(KEY_INTEGER_OF).hasSameHashCodeAs(new Key<Integer>() {});

        // WithQualifiers
        assertThat(KEY_INTEGER_X).isEqualTo(KEY_INTEGER_X_OF).isEqualTo(new Key<@CharQualifier('X') Integer>() {});
        assertThat(KEY_INTEGER_Y).isEqualTo(KEY_INTEGER_Y_OF).isEqualTo(new Key<@CharQualifier('Y') Integer>() {});

        assertThat(KEY_INTEGER_X).hasSameHashCodeAs(KEY_INTEGER_X_OF).hasSameHashCodeAs(new Key<@CharQualifier('X') Integer>() {});
        assertThat(KEY_INTEGER_Y).hasSameHashCodeAs(KEY_INTEGER_Y_OF).hasSameHashCodeAs(new Key<@CharQualifier('Y') Integer>() {});

        assertThat(KEY_INTEGER_X).isNotEqualTo(KEY_INTEGER).isNotEqualTo(KEY_INTEGER_Y).isNotEqualTo((new Key<@CharQualifier('X') Long>() {}));
    }

    /** Tests {@link Key#convertField(Field)}. */
    @Test
    public void convertField() {
        @SuppressWarnings("unused")
        class Tmpx<T> {

            @CharQualifier('X')
            @IntQualifier
            List<?> multipleQualifier;

            //////// Invalid field types
            List<T> notTypeParameterFree;

            List<?> ok;

            @CharQualifier('X')
            List<?> okQualified;

            Optional<String> optional;

            int primitive;

            @CharQualifier('X')
            int primitiveQualified;
        }

        npe(() -> Key.fromField((Field) null), "field");

        Field f = findField(Tmpx.class, "ok");
        assertThat(Key.fromField(f).type()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(Key.fromField(f).isQualified()).isFalse();
        assertThat(Key.fromField(f).qualifiers()).isEmpty();

        f = findField(Tmpx.class, "okQualified");
        assertThat(Key.fromField(f).type()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(Key.fromField(f).isQualified()).isTrue();
        assertThat(Key.fromField(f).qualifiers()).containsExactly(Qualifiers.CHAR_QUALIFIER_X);

        f = findField(Tmpx.class, "primitive");
        assertThat(Key.fromField(f).type()).isEqualTo(TL_INTEGER);
        assertThat(Key.fromField(f).isQualified()).isFalse();
        assertThat(Key.fromField(f).qualifiers()).isEmpty();

        f = findField(Tmpx.class, "primitiveQualified");
        assertThat(Key.fromField(f).type()).isEqualTo(TL_INTEGER);
        assertThat(Key.fromField(f).isQualified()).isTrue();
        assertThat(Key.fromField(f).qualifiers()).containsExactly(Qualifiers.CHAR_QUALIFIER_X);

        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> Key.fromField(findField(Tmpx.class, "notTypeParameterFree")));
        a.isExactlyInstanceOf(InvalidKeyException.class).hasNoCause();
        // TODO test msg

        a = assertThatThrownBy(() -> Key.fromField(findField(Tmpx.class, "optional")));
        a.isExactlyInstanceOf(InvalidKeyException.class).hasNoCause();
        // TODO test msg

//        a = assertThatThrownBy(() -> Key.fromField(findField(Tmpx.class, "multipleQualifier")));
//        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO test msg
    }

    /** Tests {@link Key#convertField(Field)}. */
    @Test
    public void convertMethodReturnType() throws Exception {
        @SuppressWarnings("unused")
        class Tmpx<T> {

            @CharQualifier('X')
            @IntQualifier
            List<?> multipleQualifier() {
                throw new AssertionError();
            }

            List<T> notTypeParameterFree() {
                throw new AssertionError();
            }

            List<?> ok() {
                throw new AssertionError();
            }

            @CharQualifier('X')
            List<?> okQualified() {
                throw new AssertionError();
            }

            //////// Invalid method types

            Optional<String> optional() {
                throw new AssertionError();
            }

            int primitive() {
                throw new AssertionError();
            }

            @CharQualifier('X')
            int primitiveQualified() {
                throw new AssertionError();
            }

            void voidReturnType() {
                throw new AssertionError();
            }
        }

        npe(() -> Key.fromMethodReturnType((Method) null), "method");

        Method m = Tmpx.class.getDeclaredMethod("ok");
        assertThat(Key.fromMethodReturnType(m).type()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(Key.fromMethodReturnType(m).isQualified()).isFalse();
        assertThat(Key.fromMethodReturnType(m).qualifiers()).isEmpty();

        m = Tmpx.class.getDeclaredMethod("okQualified");
        assertThat(Key.fromMethodReturnType(m).type()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(Key.fromMethodReturnType(m).isQualified()).isTrue();
        assertThat(Key.fromMethodReturnType(m).qualifiers()).containsExactly(Qualifiers.CHAR_QUALIFIER_X);

        m = Tmpx.class.getDeclaredMethod("primitive");
        assertThat(Key.fromMethodReturnType(m).type()).isEqualTo(TL_INTEGER);
        assertThat(Key.fromMethodReturnType(m).isQualified()).isFalse();
        assertThat(Key.fromMethodReturnType(m).qualifiers()).isEmpty();

        m = Tmpx.class.getDeclaredMethod("primitiveQualified");
        assertThat(Key.fromMethodReturnType(m).type()).isEqualTo(TL_INTEGER);
        assertThat(Key.fromMethodReturnType(m).isQualified()).isTrue();
        assertThat(Key.fromMethodReturnType(m).qualifiers()).containsExactly(Qualifiers.CHAR_QUALIFIER_X);

        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(
                () -> Key.fromMethodReturnType(Tmpx.class.getDeclaredMethod("voidReturnType")));
        a.isExactlyInstanceOf(InvalidKeyException.class).hasNoCause();

        a = assertThatThrownBy(() -> Key.fromMethodReturnType(Tmpx.class.getDeclaredMethod("notTypeParameterFree")));
        a.isExactlyInstanceOf(InvalidKeyException.class).hasNoCause();
        // TODO test msg

        a = assertThatThrownBy(() -> Key.fromMethodReturnType(Tmpx.class.getDeclaredMethod("optional")));
        a.isExactlyInstanceOf(InvalidKeyException.class).hasNoCause();
        // TODO test msg

//        a = assertThatThrownBy(() -> convertMethodReturnType(Tmpx.class.getDeclaredMethod("multipleQualifier")));
//        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO test msg
    }

    @Test
    public void getQualifier() {
        assertThat(KEY_INTEGER.qualifiers()).isEmpty();
        assertThat(KEY_INTEGER_X.qualifiers()).containsExactly(Qualifiers.CHAR_QUALIFIER_X);
    }

    /** Tests {@link Key#typeToken()}. */
    @Test
    public void getTypeLiteral() {
        assertThat(KEY_INT_OF.type()).isEqualTo(TL_INTEGER);
        assertThat(KEY_INTEGER.type()).isEqualTo(TL_INTEGER);
        assertThat(KEY_INTEGER_X.type()).isEqualTo(TL_INTEGER);
        assertThat(KEY_LIST_WILDCARD.type()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(KEY_LIST_WILDCARD_X.type()).isEqualTo(TL_LIST_WILDCARD);

        assert KEY_INT_OF.type().equals(TL_INTEGER);
        assert KEY_INTEGER.type().equals(TL_INTEGER);
        assert KEY_INTEGER_X.type().equals(TL_INTEGER);
        assert KEY_LIST_WILDCARD.type().equals(TL_LIST_WILDCARD);
        assert KEY_LIST_WILDCARD_X.type().equals(TL_LIST_WILDCARD);
    }

    @Test
    public void isQualified() {
        assertThat(KEY_INTEGER.isQualified()).isFalse();
        assertThat(KEY_INTEGER_X.isQualified()).isTrue();

        assert !KEY_INTEGER.isQualified();
        assert KEY_INTEGER_X.isQualified();
    }

    @Test
    public <S> void toKey() {
//        GenericType<Integer> tl1 = GenericType.of(Integer.class);
//
//        Key<Integer> k1 = Key.convertTypeLiteral(tl1);
//        Key<Integer> k2 = Key.convertTypeLiteral(TL_INTEGER);
//
//        assertThat(k1.typeToken()).isEqualTo(tl1);
//        assertThat(k2.typeToken()).isEqualTo(TL_INTEGER);
//        assertThat(k2.typeToken()).isNotSameAs(TL_INTEGER);
//
//        assertThat(k1.isQualified()).isFalse();
//        assertThat(k2.isQualified()).isFalse();
//
//        // Optional
//        assertThatThrownBy(() -> Key.convertTypeLiteral(new GenericType<Optional<Integer>>() {})).isExactlyInstanceOf(RuntimeException.class)
//                .hasMessage("Cannot convert an optional type (Optional<Integer>) to a Key, as keys cannot be optional");
//        assertThatThrownBy(() -> Key.convertTypeLiteral(new GenericType<OptionalInt>() {})).isExactlyInstanceOf(RuntimeException.class)
//                .hasMessage("Cannot convert an optional type (OptionalInt) to a Key, as keys cannot be optional");
//        assertThatThrownBy(() -> Key.convertTypeLiteral(new GenericType<OptionalLong>() {})).isExactlyInstanceOf(RuntimeException.class)
//                .hasMessage("Cannot convert an optional type (OptionalLong) to a Key, as keys cannot be optional");
//        assertThatThrownBy(() -> Key.convertTypeLiteral(new GenericType<OptionalDouble>() {})).isExactlyInstanceOf(RuntimeException.class)
//                .hasMessage("Cannot convert an optional type (OptionalDouble) to a Key, as keys cannot be optional");
////
//        // We need to use this old fashion way because of
//        try {
//            Key.convertTypeLiteral(new GenericType<List<S>>() {});
//            fail("should have failed");
//        } catch (RuntimeException e) {
//            assertThat(e).hasMessage("Can only convert type literals that are free from type variables to a Key, however TypeVariable<List<S>> defined: [S]");
//        }
    }

    /** Tests {@link Key#hasQualifier(Class)}. */
    @Test
    public void hasQualifierWith() {
        npe(() -> KEY_INTEGER.isQualifiedWithType((Class<? extends Annotation>) null), "qualifierType");

        assertThat(KEY_INTEGER.isQualifiedWithType(CharQualifier.class)).isFalse();
        assertThat(KEY_INTEGER_X.isQualifiedWithType(CharQualifier.class)).isTrue();
        assertThat(KEY_INTEGER_X.isQualifiedWithType(IntQualifier.class)).isFalse();
    }

    @Test
    public void toStringLong() {
        assertThat(KEY_INT_OF.toStringLong()).isEqualTo("Key<java.lang.Integer>");
        assertThat(KEY_INTEGER.toStringLong()).isEqualTo("Key<java.lang.Integer>");

    }

    @Test
    public void toStringSimple() {
        assertThat(KEY_INT_OF.toString()).isEqualTo("Key<Integer>");
        assertThat(KEY_INTEGER.toString()).isEqualTo("Key<Integer>");
    }

    @Test
    public void withQualifier() throws Exception {
        npe(() -> KEY_INTEGER.withQualifier((Annotation) null), "qualifier");
        assertThat(KEY_INTEGER.withQualifier(Qualifiers.CHAR_QUALIFIER_X)).isEqualTo(KEY_INTEGER_X);
        assertThat(KEY_INTEGER_X.withQualifier(Qualifiers.CHAR_QUALIFIER_Y)).isEqualTo(KEY_INTEGER_Y);

        // Tests that the annotation has a qualifier annotation.
        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(
                () -> KEY_INTEGER.withQualifier(KeyTest.class.getDeclaredMethod("withQualifier").getAnnotations()[0]));
        a.isExactlyInstanceOf(IllegalArgumentException.class).hasNoCause();
        // TODO check message
    }

    /** Tests {@link Key#withoutQualifiers()}. */
    @Test
    public void withNoQualifier() {
        npe(() -> KEY_INTEGER.withQualifier((Annotation) null), "qualifier");
        assertThat(KEY_INTEGER.withoutQualifiers()).isSameAs(KEY_INTEGER);
        assertThat(KEY_INTEGER_X.withoutQualifiers()).isEqualTo(KEY_INTEGER);
    }

    @Test
    public void typeParameters() {
        assertThat(KEY_LIST_WILDCARD.type()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(KEY_LIST_WILDCARD_X.type()).isEqualTo(TL_LIST_WILDCARD);
    }

}
