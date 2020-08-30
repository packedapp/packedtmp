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
package zeprecated.features.hook;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;

import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import testutil.stubs.annotation.Left;

/**
 *
 */
public class TypeVariableHook implements Hook {

    public static class Builder extends AbstractBuilder<Left> {}

    public static void main(String[] args) {
        Hook.Builder.test(MethodHandles.lookup(), TypeVariableHook.class, Foo.class);
    }

    public static class Foo {
        @Left
        public String foo = "ddd";
    }

    public static class AbstractBuilder<T extends Annotation> implements Hook.Builder<TypeVariableHook> {

        @OnHook
        public void on(AnnotatedFieldHook<T> hook) {
            System.out.println("Success");
        }

        /** {@inheritDoc} */
        @Override
        public TypeVariableHook build() {
            return new TypeVariableHook();
        }
    }
}
