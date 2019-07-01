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
package packed.internal.inject2;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import app.packed.container.AnnotatedMethodHook;
import app.packed.container.NativeImage;
import app.packed.hook.OnHook;
import app.packed.util.IllegalAccessRuntimeException;
import packed.internal.util.StringFormatter;

/**
 *
 */
public class Foo {

    private Foo(Builder b) {

    }

    public void foo(Class<?> clazz) {

    }

    public static class Builder {
        private final Class<?> type;

        Builder(Class<?> type) {
            this.type = requireNonNull(type);
        }

        /**
         * Builds and returns a new descriptor.
         * 
         * @return a new descriptor
         */
        Foo build() {
            for (Class<?> c = type; c != Object.class; c = c.getSuperclass()) {
                for (Method method : c.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(OnHook.class)) {
                        Parameter[] ps = method.getParameters();
                        if (ps.length != 1) {
                            throw new RuntimeException();
                        }
                        Parameter p = ps[0];
                        Class<?> cl = p.getType();
                        if (cl == AnnotatedMethodHook.class) {
                            Class<?> pt = (Class<?>) p.getParameterizedType();
                            addAnnotatedMethodHook(method, pt);
                        } else {
                            throw new UnsupportedOperationException("Unknown type " + cl);
                        }
                        // p.getParameterizedType()
                        // method.
                    }
                }
            }
            return new Foo(this);
        }

        private void addAnnotatedMethodHook(Method method, Class<?> cl) {
            Lookup lookup = MethodHandles.lookup();
            MethodHandle mh;
            try {
                method.setAccessible(true);
                mh = lookup.unreflect(method);
            } catch (IllegalAccessException | InaccessibleObjectException e) {
                throw new IllegalAccessRuntimeException("In order to use the extension " + StringFormatter.format(type) + ", the module '"
                        + type.getModule().getName() + "' in which the extension is located must be 'open' to 'app.packed.base'", e);
            }
            NativeImage.registerMethod(method);
            System.out.println(mh);
        }
    }

    static class OnAnnotatedMethod {

    }

}
