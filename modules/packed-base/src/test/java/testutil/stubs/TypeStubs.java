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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import testutil.util.TestMemberFinder;

/**
 *
 */
public class TypeStubs {

    /** A type representing {@code List<String>} */
    public static final ParameterizedType LIST_STRING = fromField("LIST_STRING$");

    /** A type representing {@code List<String>} */
    public static final GenericArrayType LIST_STRING_ARRAY = fromField("LIST_STRING_ARRAY$");

    /** A type representing {@code List<String>} */
    public static final GenericArrayType LIST_STRING_ARRAY_ARRAY = fromField("lIST_STRING_ARRAY_ARRAY$");

    @SuppressWarnings("unused")
    private static List<String>[][] lIST_STRING_ARRAY_ARRAY$;

    @SuppressWarnings("unused")
    private static List<String>[] LIST_STRING_ARRAY$;

    @SuppressWarnings("unused")
    private static List<String> LIST_STRING$;

    /** A type representing {@code List<?>} */
    public static final ParameterizedType LIST_WILDCARD = fromField("LIST_WILDCARD$");

    @SuppressWarnings("unused")
    private static List<?> LIST_WILDCARD$;

    /** A type representing {@code List<String>} */
    public static final ParameterizedType MAP_STRING_INTEGER = fromField("MAP_STRING_INTEGER$");

    @SuppressWarnings("unused")
    private static Map<String, Integer> MAP_STRING_INTEGER$;

    /** A type representing {@code List<String>} */
    public static final ParameterizedType MAP_EXTENDSSTRING_SUPERINTEGER = fromField("MAP_EXTENDSSTRING_SUPERINTEGER$");

    @SuppressWarnings("unused")
    private static Map<? extends String, ? super Integer> MAP_EXTENDSSTRING_SUPERINTEGER$;

    @SuppressWarnings("unchecked")
    private static <T extends Type> T fromField(String name) {
        return (T) TestMemberFinder.findField(TypeStubs.class, name).getGenericType();

    }
}
