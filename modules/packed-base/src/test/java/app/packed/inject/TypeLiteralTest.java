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

    static final TypeLiteral<Integer> TL_INT_OF = TypeLiteral.of(int.class);
    static final TypeLiteral<Integer> TL_INTEGER = new TypeLiteral<Integer>() {};
    static final TypeLiteral<Integer> TL_INTEGER_OF = TypeLiteral.of(Integer.class);
    static final TypeLiteral<?> TL_INTEGER_OFTYPE = TypeLiteral.of((Type) Integer.class);

    static final TypeLiteral<String> TL_STRING = new TypeLiteral<String>() {};
    static final TypeLiteral<List<String>> TL_LIST_STRING = new TypeLiteral<List<String>>() {};
    static final TypeLiteral<List<?>> TL_LIST_WILDCARD = new TypeLiteral<List<?>>() {};
    
    static final TypeLiteral<Integer[]> TL_INTEGER_ARRAY = TypeLiteral.of(Integer[].class);
    static final TypeLiteral<List<String>[]> TL_ARRAY_LIST_STRING = new TypeLiteral<List<String>[]>() {};
    static final TypeLiteral<Map<? extends String, ? super Integer>> TL_MAP = new TypeLiteral<Map<? extends String, ? super Integer>>() {};

    /** Tests that we can make a custom type literal to check that T is passed down to super classes. */
    @Test
    public void extendTypeLiterable() {

        /** A custom type literal to check that T is passed down to super classes. */
        class MyTypeLiteral<T> extends TypeLiteral<T> {}

        MyTypeLiteral<Integer> integer = new MyTypeLiteral<Integer>() {};

        // Type + Raw Type
        assertThat(integer.getType()).isSameAs(TL_INTEGER.getType()).isSameAs(Integer.class);
        assertThat(integer.getRawType()).isSameAs(TL_INTEGER.getRawType()).isSameAs(Integer.class);

        // equals
        assertThat(integer).isEqualTo(TL_INTEGER);

        // Hashcode
        assertThat(integer).hasSameHashCodeAs(TL_INTEGER);

        // ToString
        assertThat(integer).hasToString("java.lang.Integer");
        assertThat(TL_INTEGER).hasToString("java.lang.Integer");
    }

    /** Tests {@link TypeLiteral#getRawType()} */
    @Test
    public void getRawType() {
        assertThat(TL_INT_OF.getRawType()).isSameAs(int.class);
        assertThat(TL_INTEGER.getRawType()).isSameAs(Integer.class);
        assertThat(TL_INTEGER_OF.getRawType()).isSameAs(Integer.class);
        assertThat(TL_INTEGER_OFTYPE.getRawType()).isSameAs(Integer.class);
        assertThat(TL_STRING.getRawType()).isSameAs(String.class);
        assertThat(TL_LIST_STRING.getRawType()).isSameAs(List.class);
        assertThat(TL_LIST_WILDCARD.getRawType()).isSameAs(List.class);
        assertThat(TL_INTEGER_ARRAY.getRawType()).isSameAs(Integer[].class);
        assertThat(TL_ARRAY_LIST_STRING.getRawType()).isSameAs(List[].class);
        assertThat(TL_MAP.getRawType()).isSameAs(Map.class);
    }
    
    /** Tests {@link TypeLiteral#getType()} */
    @Test
    public void getType() throws Exception {
        assertThat(TL_INT_OF.getType()).isSameAs(int.class);
        assertThat(TL_INTEGER.getType()).isSameAs(Integer.class);
        assertThat(TL_INTEGER_OF.getType()).isSameAs(Integer.class);
        assertThat(TL_INTEGER_OFTYPE.getType()).isSameAs(Integer.class);
        assertThat(TL_STRING.getType()).isSameAs(String.class);
        
        @SuppressWarnings("unused")
        class Tmp {
            List<String> listString;
            List<?> listWildcard;
            Map<? extends String, ? super Integer> map;
            Integer[] intArray;
            List<String>[] listStringArray;
        }
        
        assertThat(TL_LIST_STRING.getType()).isEqualTo(Tmp.class.getDeclaredField("listString").getGenericType());
        assertThat(TL_LIST_WILDCARD.getType()).isEqualTo(Tmp.class.getDeclaredField("listWildcard").getGenericType());
        
        assertThat(TL_INTEGER_ARRAY.getType()).isEqualTo(Tmp.class.getDeclaredField("intArray").getGenericType());
        assertThat(TL_ARRAY_LIST_STRING.getType()).isEqualTo(Tmp.class.getDeclaredField("listStringArray").getGenericType());

        assertThat(TL_MAP.getType()).isEqualTo(Tmp.class.getDeclaredField("map").getGenericType());
    }

    @Test
    public void box() {
        assertThat(TL_INT_OF.box().getType()).isSameAs(Integer.class);
        assertThat(TL_INTEGER.box().getType()).isSameAs(Integer.class);
    }
    
    /** Tests type literals made from integers */
    @Test
    public void hashcode() {
        // Hashcode
        assertThat(TL_INTEGER).hasSameHashCodeAs(TL_INTEGER_OF).hasSameHashCodeAs(TL_INTEGER_OFTYPE);
        assertThat(TL_INTEGER_OF).hasSameHashCodeAs(TL_INTEGER_OFTYPE);
    }

    @Test
    public void equalsTest() {
        // equals
        assertThat(TL_INT_OF).isNotEqualTo(TL_INTEGER).isNotEqualTo(TL_INTEGER_OF).isNotEqualTo(TL_INTEGER_OFTYPE);
        assertThat(TL_INTEGER).isEqualTo(TL_INTEGER_OF).isEqualTo(TL_INTEGER_OFTYPE);
        assertThat(TL_INTEGER_OF).isEqualTo(TL_INTEGER_OFTYPE);
        assertThat(TL_INT_OF).isNotEqualTo(Long.class);
    }
    @Test
    public void UnknownTypeVariable() {
        assertThatThrownBy(() -> new TypeLiteral<Integer>()).hasNoCause();
        assertThatThrownBy(() -> new TypeLiteral<Integer>()).isExactlyInstanceOf(IllegalArgumentException.class);

        // new TypeLiteral<>() {}; Is this legal????? Its object but you know...
        assertThatThrownBy(() -> new TypeLiteral<>());
        // assertThatThrownBy(() -> new TypeLiteral<Integer>()).hasMessage(message)
        // Could not determine the type variable <T> of TypeLiteralOrKey<T> for app.packed.inject.TypeLiteral
        // TODO FIX this message
    }

    @Test
    public void listOfString() throws Exception {

        // RawType
        assertThat(TL_LIST_STRING.getRawType()).isSameAs(List.class);

        // Type
        class Tmp {
            @SuppressWarnings("unused")
            List<String> f;
        }
        Type fGenericType = Tmp.class.getDeclaredField("f").getGenericType();
        assertThat(TL_LIST_STRING.getType()).isEqualTo(fGenericType);

        // HashCode + Equals
        assertThat(TL_LIST_STRING).isEqualTo(TypeLiteral.of(fGenericType)).hasSameHashCodeAs(TypeLiteral.of(fGenericType));
    }

    /** Tests {@link TypeLiteral#toString()}. */
    @Test
    public void tooString() {
        assertThat(TL_INT_OF).hasToString("int");
        assertThat(TL_INTEGER).hasToString("java.lang.Integer");
        assertThat(TL_INTEGER_OF).hasToString("java.lang.Integer");
        assertThat(TL_INTEGER_OFTYPE).hasToString("java.lang.Integer");
        assertThat(TL_STRING).hasToString("java.lang.String");
        assertThat(TL_LIST_STRING).hasToString("java.util.List<java.lang.String>");
        assertThat(TL_LIST_WILDCARD).hasToString("java.util.List<?>");
        
        assertThat(TL_INTEGER_ARRAY).hasToString("java.lang.Integer[]");
        assertThat(TL_ARRAY_LIST_STRING).hasToString("java.util.List<java.lang.String>[]");
        
        assertThat(TL_MAP).hasToString("java.util.Map<? extends java.lang.String, ? super java.lang.Integer>");
    }
    /** Tests {@link TypeLiteral#toString()}. */
    @Test
    public void toShortString() {
        assertThat(TL_INT_OF.toShortString()).isEqualTo("int");
        assertThat(TL_INTEGER.toShortString()).isEqualTo("Integer");
        assertThat(TL_INTEGER_OF.toShortString()).isEqualTo("Integer");
        assertThat(TL_INTEGER_OFTYPE.toShortString()).isEqualTo("Integer");
        assertThat(TL_STRING.toShortString()).isEqualTo("String");
        assertThat(TL_LIST_STRING.toShortString()).isEqualTo("List<String>");
        assertThat(TL_LIST_WILDCARD.toShortString()).isEqualTo("List<?>");

        assertThat(TL_INTEGER_ARRAY.toShortString()).isEqualTo("Integer[]");
        assertThat(TL_ARRAY_LIST_STRING.toShortString()).isEqualTo("List<String>[]");

        assertThat(TL_MAP.toShortString()).isEqualTo("Map<? extends String, ? super Integer>");
    }

    @Test
    public void toStringsX() throws Exception {
        assertEquals("java.lang.String", TL_STRING.toString());
        assertEquals("java.util.List<?>", new TypeLiteral<List<?>>() {}.toString());
        assertEquals("java.util.Map<? extends java.lang.String, ?>", new TypeLiteral<Map<? extends String, ?>>() {}.toString());
    }
}
