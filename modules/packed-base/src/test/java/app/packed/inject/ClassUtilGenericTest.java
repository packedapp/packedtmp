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

import static app.packed.inject.GenericsUtil.getTypeOfArgument;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.junit.jupiter.api.Test;

/**
 *
 */
@SuppressWarnings("rawtypes")
public class ClassUtilGenericTest {

    @Test
    public void getClasz() {
        assertSame(A.class, GenericsUtil.getClass(A.class));
    }

    @Test
    public void test() {
        assertNull(getTypeOfArgument(A.class, AA1.class, 0));
        assertSame(Integer.class, getTypeOfArgument(A.class, AA2.class, 0));
        assertNull(getTypeOfArgument(A.class, AA3.class, 0));
        assertNull(getTypeOfArgument(A.class, AA4.class, 0));
        assertSame(Integer[].class, getTypeOfArgument(A.class, AA5.class, 0));
        assertSame(int[][].class, getTypeOfArgument(A.class, AA6.class, 0));

        assertSame(String.class, getTypeOfArgument(A.class, B1.class, 0));
        assertSame(List.class, getTypeOfArgument(A.class, B2.class, 0));

        assertSame(SortedSet.class, getTypeOfArgument(A.class, C1.class, 0));
    }

    class A<T> {}

    class AA1<T> extends A<T> {}

    class AA2 extends A<Integer> {}

    class AA3 extends A {}

    class AA4<T extends Collection> extends A<T> {}

    class AA5 extends A<Integer[]> {}

    class AA6 extends A<int[][]> {}

    class AA7<S> extends A<S[]> {}

    class B1 extends AA1<String> {}

    class B2 extends AA4<List> {}

    class B3<T extends Set> extends AA4<T> {}

    class B4 extends AA7<String> {}

    class C1 extends B3<SortedSet> {}

    public static void main(String[] args) {

    }
}
