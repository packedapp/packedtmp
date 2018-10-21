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

import app.packed.inject.Key;
import app.packed.inject.Qualifier;

/**
 *
 */
public class KeyTest {

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

        System.out.println(k.getQualifier());
    }
}
