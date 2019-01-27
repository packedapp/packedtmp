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

import static app.packed.util.TypeLiteralTest.TL_INTEGER;
import static app.packed.util.TypeLiteralTest.TL_LIST_WILDCARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static support.assertj.Assertions.npe;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.AbstractThrowableAssert;
import org.junit.jupiter.api.Test;

import support.stubs.annotation.CharQualifier;
import support.stubs.annotation.CharQualifiers;
import support.stubs.annotation.IntQualifier;

/** Tests {@link Key}. */
public class KeyTest {

    static final Key<Integer> KEY_INT_OF = Key.of(int.class);
    static final Key<Integer> KEY_INTEGER = new Key<Integer>() {};
    static final Key<Integer> KEY_INTEGER_OF = Key.of(Integer.class);

    static final Key<Integer> KEY_INTEGER_X = new Key<@CharQualifier('X') Integer>() {};
    static final Key<Integer> KEY_INTEGER_X_OF = Key.of(Integer.class, CharQualifiers.X);
    static final Key<Integer> KEY_INTEGER_Y = new Key<@CharQualifier('Y') Integer>() {};
    static final Key<Integer> KEY_INTEGER_Y_OF = Key.of(Integer.class, CharQualifiers.Y);

    static final Key<List<String>> KEY_LIST_STRING = new Key<List<String>>() {};
    static final Key<List<?>> KEY_LIST_WILDCARD = new Key<List<?>>() {};

    static final Key<List<?>> KEY_LIST_WILDCARD_X = new Key<@CharQualifier('X') List<?>>() {};
    static final Key<String> KEY_STRING = new Key<String>() {};

    static final Key<Map<? extends String, ?>> TL_MAP = new Key<Map<? extends String, ?>>() {};

    @Test
    public void canonicalize() {
        Key<Integer> key = Key.of(Integer.class, CharQualifiers.X);

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

    /** Tests {@link Key#fromField(Field)}. */
    @Test
    public void fromField() throws Exception {
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

        npe(() -> Key.fromField(null), "field");

        Field f = Tmpx.class.getDeclaredField("ok");
        assertThat(Key.fromField(f).typeLiteral()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(Key.fromField(f).hasQualifier()).isFalse();
        assertThat(Key.fromField(f).qualifier().isPresent()).isFalse();

        f = Tmpx.class.getDeclaredField("okQualified");
        assertThat(Key.fromField(f).typeLiteral()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(Key.fromField(f).hasQualifier()).isTrue();
        assertThat(Key.fromField(f).qualifier().get()).isEqualTo(CharQualifiers.X);

        f = Tmpx.class.getDeclaredField("primitive");
        assertThat(Key.fromField(f).typeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(Key.fromField(f).hasQualifier()).isFalse();
        assertThat(Key.fromField(f).qualifier().isPresent()).isFalse();

        f = Tmpx.class.getDeclaredField("primitiveQualified");
        assertThat(Key.fromField(f).typeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(Key.fromField(f).hasQualifier()).isTrue();
        assertThat(Key.fromField(f).qualifier().get()).isEqualTo(CharQualifiers.X);

        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> Key.fromField(Tmpx.class.getDeclaredField("notTypeParameterFree")));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO test msg

        a = assertThatThrownBy(() -> Key.fromField(Tmpx.class.getDeclaredField("optional")));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO test msg

        a = assertThatThrownBy(() -> Key.fromField(Tmpx.class.getDeclaredField("multipleQualifier")));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO test msg
    }

    /** Tests {@link Key#fromField(Field)}. */
    @Test
    public void fromMethodReturnType() throws Exception {
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

        npe(() -> Key.fromMethodReturnType(null), "method");

        Method m = Tmpx.class.getDeclaredMethod("ok");
        assertThat(Key.fromMethodReturnType(m).typeLiteral()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(Key.fromMethodReturnType(m).hasQualifier()).isFalse();
        assertThat(Key.fromMethodReturnType(m).qualifier().isPresent()).isFalse();

        m = Tmpx.class.getDeclaredMethod("okQualified");
        assertThat(Key.fromMethodReturnType(m).typeLiteral()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(Key.fromMethodReturnType(m).hasQualifier()).isTrue();
        assertThat(Key.fromMethodReturnType(m).qualifier().get()).isEqualTo(CharQualifiers.X);

        m = Tmpx.class.getDeclaredMethod("primitive");
        assertThat(Key.fromMethodReturnType(m).typeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(Key.fromMethodReturnType(m).hasQualifier()).isFalse();
        assertThat(Key.fromMethodReturnType(m).qualifier().isPresent()).isFalse();

        m = Tmpx.class.getDeclaredMethod("primitiveQualified");
        assertThat(Key.fromMethodReturnType(m).typeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(Key.fromMethodReturnType(m).hasQualifier()).isTrue();
        assertThat(Key.fromMethodReturnType(m).qualifier().get()).isEqualTo(CharQualifiers.X);

        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(() -> Key.fromMethodReturnType(Tmpx.class.getDeclaredMethod("voidReturnType")));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();

        a = assertThatThrownBy(() -> Key.fromMethodReturnType(Tmpx.class.getDeclaredMethod("notTypeParameterFree")));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO test msg

        a = assertThatThrownBy(() -> Key.fromMethodReturnType(Tmpx.class.getDeclaredMethod("optional")));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO test msg

        a = assertThatThrownBy(() -> Key.fromMethodReturnType(Tmpx.class.getDeclaredMethod("multipleQualifier")));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO test msg
    }

    @Test
    public void getQualifier() {
        assertThat(KEY_INTEGER.qualifier()).isEmpty();
        assertThat(KEY_INTEGER_X.qualifier().get()).isEqualTo(CharQualifiers.X);
    }

    /** Tests {@link Key#getTypeLiteral()}. */
    @Test
    public void getTypeLiteral() {
        assertThat(KEY_INT_OF.typeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(KEY_INTEGER.typeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(KEY_INTEGER_X.typeLiteral()).isEqualTo(TL_INTEGER);
        assertThat(KEY_LIST_WILDCARD.typeLiteral()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(KEY_LIST_WILDCARD_X.typeLiteral()).isEqualTo(TL_LIST_WILDCARD);
    }

    @Test
    public void hasQualifier() {
        assertThat(KEY_INTEGER.hasQualifier()).isFalse();
        assertThat(KEY_INTEGER_X.hasQualifier()).isTrue();
    }

    /** Tests {@link Key#isQualifiedWith(Class)}. */
    @Test
    public void isQualifiedWith() {
        npe(() -> KEY_INTEGER.isQualifiedWith(null), "qualifierType");

        assertThat(KEY_INTEGER.isQualifiedWith(CharQualifier.class)).isFalse();
        assertThat(KEY_INTEGER_X.isQualifiedWith(CharQualifier.class)).isTrue();
        assertThat(KEY_INTEGER_X.isQualifiedWith(IntQualifier.class)).isFalse();
    }

    @Test
    public void toString$() {
        assertThat(KEY_INT_OF.toString()).isEqualTo(TL_INTEGER.toString());
        assertThat(KEY_INTEGER.toString()).isEqualTo(TL_INTEGER.toString());
        assertThat(KEY_INTEGER_X.toString()).isEqualTo("@" + CharQualifier.class.getName() + "(value='X') " + TL_INTEGER.toString());
        assertThat(KEY_LIST_WILDCARD.toString()).isEqualTo(TL_LIST_WILDCARD.toString());
        assertThat(KEY_LIST_WILDCARD_X.toString()).isEqualTo("@" + CharQualifier.class.getName() + "(value='X') " + TL_LIST_WILDCARD.toString());
    }

    @Test
    public void toStringSimple() {
        assertThat(KEY_INT_OF.toStringSimple()).isEqualTo(TL_INTEGER.toStringSimple());
        assertThat(KEY_INTEGER.toStringSimple()).isEqualTo(TL_INTEGER.toStringSimple());
        assertThat(KEY_INTEGER_X.toStringSimple()).isEqualTo("@" + CharQualifier.class.getSimpleName() + "(value='X') " + TL_INTEGER.toStringSimple());
        assertThat(KEY_LIST_WILDCARD.toStringSimple()).isEqualTo(TL_LIST_WILDCARD.toStringSimple());
        assertThat(KEY_LIST_WILDCARD_X.toStringSimple())
                .isEqualTo("@" + CharQualifier.class.getSimpleName() + "(value='X') " + TL_LIST_WILDCARD.toStringSimple());
    }

    @Test
    public void withQualifier() throws Exception {
        npe(() -> KEY_INTEGER.withQualifier((Annotation) null), "qualifier");
        assertThat(KEY_INTEGER.withQualifier(CharQualifiers.X)).isEqualTo(KEY_INTEGER_X);
        assertThat(KEY_INTEGER_X.withQualifier(CharQualifiers.Y)).isEqualTo(KEY_INTEGER_Y);

        // Tests that the annotation has a qualifier annotation.
        AbstractThrowableAssert<?, ? extends Throwable> a = assertThatThrownBy(
                () -> KEY_INTEGER.withQualifier(KeyTest.class.getDeclaredMethod("withQualifier").getAnnotations()[0]));
        a.isExactlyInstanceOf(InvalidDeclarationException.class).hasNoCause();
        // TODO check message
    }

    /** Tests {@link Key#withNoQualifier()}. */
    @Test
    public void withNoQualifier() {
        npe(() -> KEY_INTEGER.withQualifier((Annotation) null), "qualifier");
        assertThat(KEY_INTEGER.withNoQualifier()).isSameAs(KEY_INTEGER);
        assertThat(KEY_INTEGER_X.withNoQualifier()).isEqualTo(KEY_INTEGER);
    }

    @Test
    public void typeParameters() {
        assertThat(KEY_LIST_WILDCARD.typeLiteral()).isEqualTo(TL_LIST_WILDCARD);
        assertThat(KEY_LIST_WILDCARD_X.typeLiteral()).isEqualTo(TL_LIST_WILDCARD);
    }
}
