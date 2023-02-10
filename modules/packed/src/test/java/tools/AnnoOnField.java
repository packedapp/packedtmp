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
package tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import app.packed.bean.BeanElement.BeanField;
import app.packed.bean.BeanHook.AnnotatedFieldHook;
import app.packed.util.AnnotationList;
import app.packed.util.Key;
import app.packed.util.Variable;
import testutil.util.MemberFinder;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedFieldHook(extension = HExtension.class)
public @interface AnnoOnField {

    public static class InstanceField {
        public static final Field FOO = MemberFinder.findField("foo");

        public static void validateFoo(AnnotationList hooks, BeanField b) {
            // validate annotations
            assertEquals(FOO, b.field());
            assertEquals(FOO.getModifiers(), b.modifiers());
            assertEquals(Key.of(String.class), b.toKey());
            assertEquals(Variable.of(String.class), b.variable());
        }

        @AnnoOnField
        private String foo = "instance";
    }

    public static class StaticField {

        @AnnoOnField
        private static String foo = "static";
    }
}