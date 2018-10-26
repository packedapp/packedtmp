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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 *
 */
public class TypeStubs {
    public static final ParameterizedType LIST_STRING = fromField("LIST_STRING$");

    @SuppressWarnings("unused")
    private static List<String> LIST_STRING$;

    public static final ParameterizedType LIST_WILDCARD = fromField("LIST_WILDCARD$");
    
    @SuppressWarnings("unused")
    private static List<?> LIST_WILDCARD$;

    public static void main(String[] args) {
        System.out.println(LIST_STRING);
    }
    @SuppressWarnings("unchecked")
    private static <T extends Type> T fromField(String name) {
        try {
            return (T) TypeStubs.class.getDeclaredField(name).getGenericType();
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }
}
