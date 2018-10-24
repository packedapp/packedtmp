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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class KeyTest {
    static final Key<Integer> TL_INT_OF = Key.of(int.class);
    static final Key<Integer> TL_INTEGER = new Key<Integer>() {};
    static final Key<Integer> TL_INTEGER_OF = Key.of(Integer.class);
    static final Key<?> TL_INTEGER_OFTYPE = Key.of((Type) Integer.class);

    static final Key<String> TL_STRING = new Key<String>() {};
    static final Key<List<String>> TL_LIST_STRING = new Key<List<String>>() {};
    static final Key<List<?>> TL_LIST_WILDCARD = new Key<List<?>>() {};
    
    static final Key<Map<? extends String, ?>> TL_MAP = new Key<Map<? extends String, ?>>() {};
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.TYPE_USE })
    @Qualifier
    @interface Name {
        String value();
    }

    // Se ogsaa
    // https://github.com/leangen/geantyref/blob/master/src/main/java/io/leangen/geantyref/AnnotationInvocationHandler.java
    public static void main(String[] args) {
        Key<String> k = new Key<@Name("foo") String>() {};

        System.out.println(k);
        System.out.println(k.getQualifier());
    }
}
