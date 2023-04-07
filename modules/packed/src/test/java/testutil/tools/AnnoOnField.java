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
package testutil.tools;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import app.packed.extension.BeanHook.AnnotatedFieldHook;
import testutil.MemberFinder;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedFieldHook(extension = TckExtension.class)
public @interface AnnoOnField {

    String name() default "main";

    /** Exposes a private instance field with {@link AnnoOnField}. */
    public static class FieldPrivateInstanceString {

        public static final Field FOO_FIELD = MemberFinder.findFieldOnThisClass("foo");

        @AnnoOnField
        private String foo = "instance";

        public String fieldValue() {
            return foo;
        }
    }

    public static class FieldPrivateStaticString {

        @AnnoOnField
        private static String foo = "static";
    }
}