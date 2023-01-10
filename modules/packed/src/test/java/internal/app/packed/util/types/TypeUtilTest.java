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
package internal.app.packed.util.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static testutil.assertj.Assertions.npe;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Tests {@link TypeUtil}. */
public class TypeUtilTest {


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

    /** Tests {@link TypeUtil#rawTypeOf(Type)}. */
    @Test
    public void rawTypeOf() throws Exception {
        @SuppressWarnings("unused")
        class C<T> {
            public List<String> f1;
            public Map<? extends String, ? super Integer> f2;
            public List<String>[] f3;
            public List<?> f4;
        }
        npe(() -> TypeUtil.rawTypeOf(null), "type");
        assertThat(TypeUtil.rawTypeOf(String.class)).isSameAs(String.class);
        assertThat(TypeUtil.rawTypeOf(String[].class)).isSameAs(String[].class);

        assertThat(TypeUtil.rawTypeOf(C.class.getField("f1").getGenericType())).isSameAs(List.class);
        assertThat(TypeUtil.rawTypeOf(C.class.getField("f2").getGenericType())).isSameAs(Map.class);
        assertThat(TypeUtil.rawTypeOf(C.class.getField("f3").getGenericType())).isSameAs(List[].class);
        assertThat(TypeUtil.rawTypeOf(C.class.getField("f4").getGenericType())).isSameAs(List.class);

        WildcardType wt = (WildcardType) ((ParameterizedType) C.class.getField("f4").getGenericType()).getActualTypeArguments()[0];
        assertThat(TypeUtil.rawTypeOf(wt)).isSameAs(Object.class);

        assertThat(TypeUtil.rawTypeOf(C.class.getTypeParameters()[0])).isSameAs(Object.class);

        assertThatThrownBy(() -> TypeUtil.rawTypeOf(new Type() {})).isExactlyInstanceOf(IllegalArgumentException.class);
    }


    class NestedNonStaticClass {}

    static class NestedStaticClass {}
}
