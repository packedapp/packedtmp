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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableFactory;

/**
 *
 */
public class HookController implements AutoCloseable {

    private final ClassProcessor cp;

    private boolean isClosed;

    private final ThrowableFactory<? extends RuntimeException> tf;

    @SuppressWarnings("unchecked")
    public HookController(ClassProcessor cp, ThrowableFactory<?> tf) {
        this.cp = requireNonNull(cp);
        // A hack to allow us to throw AssertionError, as we have no way to indicate
        // Error || RuntimeException
        this.tf = (ThrowableFactory<? extends RuntimeException>) tf;
    }

    public void checkActive() {
        if (isClosed) {
            throw tf.newThrowable("No longer active");
        }
    }

    @Override
    public void close() {
        isClosed = true;
    }

    public ThrowableFactory<? extends RuntimeException> tf() {
        return tf;
    }

    public MethodHandle unreflect(Method method) {
        checkActive();
        return cp.unreflect(method, tf);
    }

    public MethodHandle unreflectConstructor(Constructor<?> constructor) {
        checkActive();
        return cp.unreflectConstructor(constructor, tf);
    }

    public MethodHandle unreflectGetter(Field field) {
        checkActive();
        return cp.unreflectGetter(field, tf);
    }

    public MethodHandle unreflectSetter(Field field) {
        checkActive();
        return cp.unreflectSetter(field, tf);
    }

    public VarHandle unreflectVarhandle(Field field) {
        checkActive();
        return cp.unreflectVarhandle(field, tf);
    }
}
