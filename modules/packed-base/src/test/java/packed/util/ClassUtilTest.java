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
package packed.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Tests {@link ClassUtil}. */
public class ClassUtilTest {

    /** Tests {@link ClassUtil#boxClass(Class)}. */
    @Test
    public void boxClass() {
        assertThat(ClassUtil.boxClass(String.class)).isSameAs(String.class);
        assertThat(ClassUtil.boxClass(boolean.class)).isSameAs(Boolean.class);
        assertThat(ClassUtil.boxClass(byte.class)).isSameAs(Byte.class);
        assertThat(ClassUtil.boxClass(char.class)).isSameAs(Character.class);
        assertThat(ClassUtil.boxClass(double.class)).isSameAs(Double.class);
        assertThat(ClassUtil.boxClass(float.class)).isSameAs(Float.class);
        assertThat(ClassUtil.boxClass(int.class)).isSameAs(Integer.class);
        assertThat(ClassUtil.boxClass(long.class)).isSameAs(Long.class);
        assertThat(ClassUtil.boxClass(short.class)).isSameAs(Short.class);
    }

    /** Tests {@link ClassUtil#unboxClass(Class)}. */
    @Test
    public void unboxClass() {
        assertThat(ClassUtil.unboxClass(String.class)).isSameAs(String.class);
        assertThat(ClassUtil.unboxClass(Boolean.class)).isSameAs(boolean.class);
        assertThat(ClassUtil.unboxClass(Byte.class)).isSameAs(byte.class);
        assertThat(ClassUtil.unboxClass(Character.class)).isSameAs(char.class);
        assertThat(ClassUtil.unboxClass(Double.class)).isSameAs(double.class);
        assertThat(ClassUtil.unboxClass(Float.class)).isSameAs(float.class);
        assertThat(ClassUtil.unboxClass(Integer.class)).isSameAs(int.class);
        assertThat(ClassUtil.unboxClass(Long.class)).isSameAs(long.class);
        assertThat(ClassUtil.unboxClass(Short.class)).isSameAs(short.class);
    }
}
