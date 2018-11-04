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
package packed.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Array;

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
        class LocalClass {}
        String localClassName = LocalClass.class.getName();
        formatClass0(LocalClass.class, localClassName);

        System.out.println(Integer[][].class.getSimpleName());
        System.out.println(StaticInnerClass[][].class.getSimpleName());
        System.out.println(LocalClass[][].class.getSimpleName());

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

    public static class StaticInnerClass {}

    public static class InnerClass {}
}
