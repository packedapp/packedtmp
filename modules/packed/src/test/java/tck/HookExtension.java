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
package tck;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;

import app.packed.extension.BeanElement.BeanField;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.BeanHook.AnnotatedFieldHook;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.BeanWrappedVariable;
import app.packed.extension.Extension;
import app.packed.util.AnnotationList;
import app.packed.util.Nullable;
import testutil.MemberFinder;

/**
 *
 */
public class HookExtension extends Extension<HookExtension> {

    @Nullable
    private BiConsumer<? super AnnotationList, ? super BeanField> onAnnotatedField;

    @Nullable
    private BiConsumer<? super AnnotationList, ? super BeanMethod> onAnnotatedMethod;

    @Nullable
    private BiConsumer<? super Class<?>, ? super BeanWrappedVariable> onVariableType;

    HookExtension() {}

    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @Override
            public void hookOnAnnotatedField(AnnotationList hooks, BeanField field) {
                if (onAnnotatedField != null) {
                    onAnnotatedField.accept(hooks, field);
                } else {
                    super.hookOnAnnotatedField(hooks, field);
                }
            }

            @Override
            public void hookOnAnnotatedMethod(AnnotationList hooks, BeanMethod method) {
                if (onAnnotatedMethod != null) {
                    onAnnotatedMethod.accept(hooks, method);
                } else {
                    super.hookOnAnnotatedMethod(hooks, method);
                }
            }

            @Override
            public void hookOnVariableType(Class<?> hook, BeanWrappedVariable variable) {
                if (onVariableType != null) {
                    onVariableType.accept(hook, variable);
                } else {
                    super.hookOnVariableType(hook, variable);
                }
            }
        };
    }

    public HookExtension onAnnotatedField(BiConsumer<? super AnnotationList, ? super BeanField> consumer) {
        onAnnotatedField = consumer;
        return this;
    }

    public HookExtension onAnnotatedMethod(BiConsumer<? super AnnotationList, ? super BeanMethod> consumer) {
        onAnnotatedMethod = consumer;
        return this;
    }

    public HookExtension onVariableType(BiConsumer<? super Class<?>, ? super BeanWrappedVariable> onVariableType) {
        this.onVariableType = onVariableType;
        return this;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @AnnotatedFieldHook(extension = HookExtension.class)
    public @interface FieldHook {

        String name() default "main";

        /** Exposes a private instance field with {@link AnnoOnField}. */
        public static class FieldPrivateInstanceString {

            public static final Field FOO_FIELD = MemberFinder.findFieldOnThisClass("foo");

            @FieldHook
            private String foo = "instance";

            public String fieldValue() {
                return foo;
            }
        }

        public static class FieldPrivateStaticString {

            @FieldHook
            private static String foo = "static";
        }
    }
}
