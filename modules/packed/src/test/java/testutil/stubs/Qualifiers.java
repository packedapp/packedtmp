/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

import app.packed.binding.Qualifier;

/** Various instances of annotations that can be used for tests. */
public class Qualifiers {

    @NoValueQualifier
    public static final NoValueQualifier NO_VALUE_QUALIFIER = Arrays.stream(Qualifiers.class.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(NoValueQualifier.class)).findFirst().get().getAnnotation(NoValueQualifier.class);

    @Left
    public static final Left LEFT = Arrays.stream(Qualifiers.class.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Left.class)).findFirst()
            .get().getAnnotation(Left.class);

    @CharQualifier('X')
    public static final CharQualifier CHAR_QUALIFIER_X = Arrays.stream(Qualifiers.class.getDeclaredFields())
            .filter(e -> e.getName().equals("CHAR_QUALIFIER_X")).findFirst().get().getAnnotation(CharQualifier.class);

    @CharQualifier('Y')
    public static final CharQualifier CHAR_QUALIFIER_Y = Arrays.stream(Qualifiers.class.getDeclaredFields())
            .filter(e -> e.getName().equals("CHAR_QUALIFIER_Y")).findFirst().get().getAnnotation(CharQualifier.class);

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    public @interface Left {}

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    public @interface NoValueQualifier {}

    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    public @interface Right {}


    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
    public @interface IntQualifier {
        int value() default '0';
    }
    /**
    *
    */
   @Retention(RetentionPolicy.RUNTIME)
   @Qualifier
   @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
   public @interface CharQualifier {
       char value() default '?';
   }
   @Retention(RetentionPolicy.RUNTIME)
   @Qualifier
   @Target({ ElementType.TYPE_USE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER })
   public @interface StringQualifier {
       String value() default "";
   }
    /**
    *
    */
   @Retention(RetentionPolicy.RUNTIME)
   @Qualifier
   @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
   public @interface LetterStringAnnotation {
       Class<? extends Letters> letter() default Letters.class;

       String string() default "";
   }

}
