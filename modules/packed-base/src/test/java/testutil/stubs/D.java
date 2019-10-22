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
package testutil.stubs;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import testutil.stubs.annotation.CharQualifiers;

/**
 *
 */
public class D {

    public static void main(String[] args) {
        Optional<Field> findFirst = Arrays.stream(CharQualifiers.class.getDeclaredFields()).filter(e -> e.getName().equals("X")).findFirst();
        System.out.println(findFirst.get().getAnnotations().length);

        Object o = Arrays.stream(CharQualifiers.class.getDeclaredFields()).filter(e -> e.getName().equals("X")).findFirst().get().getAnnotations()[0];
        System.out.println(o);
    }
}
