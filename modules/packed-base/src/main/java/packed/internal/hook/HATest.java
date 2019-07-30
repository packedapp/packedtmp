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
package packed.internal.hook;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import app.packed.component.ComponentConfiguration;
import app.packed.hook.AnnotatedFieldHook;
import app.packed.hook.AnnotatedMethodHook;
import app.packed.hook.OnHook;
import app.packed.lifecycle.Main;
import app.packed.lifecycle.OnStart;

/**
 *
 */
public class HATest {

    public static void main(String[] args) throws Exception {
        OnHookAggregator oha = OnHookAggregator.get(FFF.class);

        System.out.println(oha.resultType());

        System.out.println(oha.newAggregatorInstance());
        System.out.println(oha.newAggregatorInstance());

        // ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericInterfaces()[0];
        // Type[] typeArguments = parameterizedType.getActualTypeArguments();
        // Class<?> typeArgument = (Class<?>) typeArguments[0];
        // return typeArgument;
        Method m = HATest.class.getDeclaredMethod("foo", ComponentConfiguration.class, AnnotatedFieldHook.class);

        MethodHandle mh = MethodHandles.lookup().unreflect(m);
        System.out.println(mh.type().lastParameterType());
        System.out.println(mh.type().parameterType(0));

    }

    public static class MyComp {

        @Main
        public void foo() {

        }
    }

    public void foo(ComponentConfiguration cc, AnnotatedFieldHook<OnStart> h) {}

    static class FFF implements Supplier<String> {

        @OnHook
        public void foo(AnnotatedFieldHook<OnStart> h) {}

        @OnHook
        public void foos(AnnotatedMethodHook<OnStart> h) {}

        /** {@inheritDoc} */
        @Override
        public String get() {
            return null;
        }

    }
}
