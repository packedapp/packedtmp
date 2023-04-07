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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.BeanHook.AnnotatedMethodHook;
import app.packed.util.AnnotationList;
import app.packed.util.Key;
import testutil.MemberFinder;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AnnotatedMethodHook(extension = TckExtension.class)
public @interface AnnoOnMethod {

    public static class InstanceMethodNoParamsVoid {
        public static final Method FOO = MemberFinder.findMethod("foo");

        public static void validateFoo(AnnotationList hooks, BeanMethod m) {
            // validate annotations
            assertEquals(FOO, m.method());
            assertEquals(FOO.getModifiers(), m.modifiers());
            m.toKey(); // should fail
            assertEquals(Key.of(String.class), m.toKey());
        }

        @AnnoOnMethod
        void foo() {}
    }

}