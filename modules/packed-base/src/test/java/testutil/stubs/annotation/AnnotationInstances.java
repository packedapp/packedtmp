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
package testutil.stubs.annotation;

import java.util.Arrays;

/** Various instances of annotations that can be used for tests. */
public class AnnotationInstances {

    @NoValueQualifier
    public static final NoValueQualifier NO_VALUE_QUALIFIER = Arrays.stream(AnnotationInstances.class.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(NoValueQualifier.class)).findFirst().get().getAnnotation(NoValueQualifier.class);

    @Left
    public static final Left LEFT = Arrays.stream(AnnotationInstances.class.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Left.class)).findFirst()
            .get().getAnnotation(Left.class);

    @CharQualifier('X')
    public static final CharQualifier CHAR_QUALIFIER_X = Arrays.stream(AnnotationInstances.class.getDeclaredFields())
            .filter(e -> e.getName().equals("CHAR_QUALIFIER_X")).findFirst().get().getAnnotation(CharQualifier.class);

    @CharQualifier('Y')
    public static final CharQualifier CHAR_QUALIFIER_Y = Arrays.stream(AnnotationInstances.class.getDeclaredFields())
            .filter(e -> e.getName().equals("CHAR_QUALIFIER_Y")).findFirst().get().getAnnotation(CharQualifier.class);
}
