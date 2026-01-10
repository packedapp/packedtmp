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
package internal.app.packed.util;

import static internal.app.packed.util.StringFormatter.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static testutil.Assertions.npe;
import static testutil.stubs.TypeStubs.LIST_STRING;
import static testutil.stubs.TypeStubs.LIST_STRING_ARRAY;
import static testutil.stubs.TypeStubs.LIST_STRING_ARRAY_ARRAY;
import static testutil.stubs.TypeStubs.LIST_WILDCARD;
import static testutil.stubs.TypeStubs.MAP_EXTENDSSTRING_SUPERINTEGER;
import static testutil.stubs.TypeStubs.MAP_STRING_INTEGER;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

import org.junit.jupiter.api.Test;

/**
 *
 */
public class StringFormatterTest {
    static final String BASE = StringFormatterTest.class.getCanonicalName();

    @Test
    public void formatClass() {
        formatClass0(int.class, "int");
        formatClass0(Integer.class, "java.lang.Integer");
        formatClass0(StaticInnerClass.class, BASE + "$" + StaticInnerClass.class.getSimpleName());
        formatClass0(InnerClass.class, BASE + "$" + InnerClass.class.getSimpleName());

        // Local Class
        class LocalClass {
        }
        String localClassName = LocalClass.class.getName();
        formatClass0(LocalClass.class, localClassName);

        // Anonymous class
        // Class<?> anonymous = new Object() {}.getClass();
        // TODO test anonoymous
    }

    void formatClass0(Class<?> cl, String expectedName) {
        assertThat(format(cl)).isEqualTo(expectedName);
        Class<?> a = Array.newInstance(cl, 0).getClass();
        assertThat(format(a)).isEqualTo(expectedName + "[]");
        Class<?> b = Array.newInstance(a, 0).getClass();
        assertThat(format(b)).isEqualTo(expectedName + "[][]");
    }

    /** Tests {@link StringFormatter#formatSimple(java.lang.reflect.Type)}. */
    @Test
    public void toShortString() {
        npe(() -> StringFormatter.formatSimple((Type) null), "type");
        assertThat(StringFormatter.formatSimple(String.class)).isEqualTo("String");
        assertThat(StringFormatter.formatSimple(LIST_STRING)).isEqualTo("List<String>");
        assertThat(StringFormatter.formatSimple(LIST_STRING_ARRAY)).isEqualTo("List<String>[]");
        assertThat(StringFormatter.formatSimple(LIST_STRING_ARRAY_ARRAY)).isEqualTo("List<String>[][]");
        assertThat(StringFormatter.formatSimple(MAP_STRING_INTEGER)).isEqualTo("Map<String, Integer>");

        class Y<T> {
        }
        assertThat(StringFormatter.formatSimple(Y.class.getTypeParameters()[0])).isEqualTo("T");
        assertThat(StringFormatter.formatSimple(LIST_WILDCARD)).isEqualTo("List<?>");
        assertThat(StringFormatter.formatSimple(MAP_EXTENDSSTRING_SUPERINTEGER)).isEqualTo("Map<? extends String, ? super Integer>");

        assertThatThrownBy(() -> StringFormatter.formatSimple(new Type() {
        })).isExactlyInstanceOf(IllegalArgumentException.class);
    }

    public static class StaticInnerClass {
    }

    public static class InnerClass {
    }
}
