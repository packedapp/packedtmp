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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Tests {@link TypeLiteral}. */
public class TypeLiteralTest {

    /** Tests type literals made from integers */
    @Test
    public void integer() {
        TypeLiteral<Integer> intOf = TypeLiteral.of(int.class);
        TypeLiteral<Integer> integer = new TypeLiteral<Integer>() {};
        TypeLiteral<Integer> integerOf = TypeLiteral.of(Integer.class);
        TypeLiteral<?> integerOfType = TypeLiteral.of((Type) Integer.class);

        // Type
        assertThat(intOf.getType()).isSameAs(int.class);
        assertThat(integer.getType()).isSameAs(integerOf.getType()).isSameAs(integerOfType.getType()).isSameAs(Integer.class);

        // Type + Raw type
        assertThat(intOf.getType()).isSameAs((intOf.getRawType())).isSameAs(int.class);
        assertThat(integer.getType()).isSameAs((integer.getRawType())).isSameAs(Integer.class);
        assertThat(integerOf.getType()).isSameAs((integerOf.getRawType())).isSameAs(Integer.class);
        assertThat(integerOfType.getType()).isSameAs((integerOfType.getRawType())).isSameAs(Integer.class);

        // equals
        assertThat(intOf).isNotEqualTo(integer).isNotEqualTo(integerOf).isNotEqualTo(integerOfType);
        assertThat(integer).isEqualTo(integerOf).isEqualTo(integerOfType);
        assertThat(integerOf).isEqualTo(integerOfType);

        // Hashcode
        assertThat(integer).hasSameHashCodeAs(integerOf).hasSameHashCodeAs(integerOfType);
        assertThat(integerOf).hasSameHashCodeAs(integerOfType);

        // ToString
        assertThat(intOf).hasToString("int");
        assertThat(integer).hasToString("java.lang.Integer");
        assertThat(integerOf).hasToString("java.lang.Integer");
        assertThat(integerOfType).hasToString("java.lang.Integer");
    }

    @Test
    public void integerNoCapture() {
        assertThatThrownBy(() -> new TypeLiteral<Integer>()).hasNoCause();
        assertThatThrownBy(() -> new TypeLiteral<Integer>()).isExactlyInstanceOf(IllegalArgumentException.class);
        //assertThatThrownBy(() -> new TypeLiteral<Integer>()).hasMessage(message)
        //Could not determine the type variable <T> of TypeLiteralOrKey<T> for app.packed.inject.TypeLiteral
        //TODO FIX this message
    }

    @Test
    public void listOfString() throws Exception {
        TypeLiteral<List<String>> los = new TypeLiteral<>() {};

        // RawType
        assertThat(los.getRawType()).isSameAs(List.class);

        // Type
        class Tmp {
            @SuppressWarnings("unused")
            List<String> f;
        }
        Type fGenericType = Tmp.class.getDeclaredField("f").getGenericType();
        assertThat(los.getType()).isEqualTo(fGenericType);

        // HashCode + Equals
        assertThat(los).isEqualTo(TypeLiteral.of(fGenericType)).hasSameHashCodeAs(TypeLiteral.of(fGenericType));

        assertThat(los).hasToString("java.util.List<java.lang.String>");
    }

    @Test
    public void toStrings() throws Exception {
        assertThat(new TypeLiteral<String>() {}).hasToString("java.lang.String");
        assertThat(new TypeLiteral<List<?>>() {}).hasToString("java.util.List<?>");
        assertThat(new TypeLiteral<Map<? extends String, ?>>() {}).hasToString("java.util.Map<? extends java.lang.String, ?>");
    }

    @Test
    public void toStringsX() throws Exception {
        assertEquals("java.lang.String", new TypeLiteral<String>() {}.toString());
        assertEquals("java.util.List<?>", new TypeLiteral<List<?>>() {}.toString());
        assertEquals("java.util.Map<? extends java.lang.String, ?>", new TypeLiteral<Map<? extends String, ?>>() {}.toString());
    }

    /** Tests that we can make a custom type literal to check that T is passed down to super classes. */
    @Test
    public void extendAnotherLiterable() {
        TypeLiteral<Integer> myInteger = new TypeLiteral<Integer>() {};
        MyTypeLiteral<Integer> integer = new MyTypeLiteral<Integer>() {};

        // Type + Raw Type
        assertThat(integer.getType()).isSameAs(myInteger.getType()).isSameAs(Integer.class);
        assertThat(integer.getRawType()).isSameAs(myInteger.getRawType()).isSameAs(Integer.class);

        // equals
        assertThat(integer).isEqualTo(myInteger);

        // Hashcode
        assertThat(integer).hasSameHashCodeAs(myInteger);

        // ToString
        assertThat(integer).hasToString("java.lang.Integer");
        assertThat(myInteger).hasToString("java.lang.Integer");
    }

    /** A custom type literal to check that T is passed down to super classes. */
    static class MyTypeLiteral<T> extends TypeLiteral<T> {}
}
