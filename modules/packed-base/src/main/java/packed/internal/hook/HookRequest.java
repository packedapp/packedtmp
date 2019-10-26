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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.hook.AssignableToHook;
import app.packed.hook.Hook;
import app.packed.hook.OnHook;
import app.packed.lang.Nullable;
import packed.internal.moduleaccess.ModuleAccess;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.Tiny;
import packed.internal.util.TinyPair;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public final class HookRequest {

    @Nullable
    private final Tiny<BaseHookCallback> baseHooksCallback;

    /** A list of custom hook callbacks for the particular extension. */
    private final TinyPair<Hook, MethodHandle> customHooksCallback;

    /** Used for creating {@link MethodHandle} and {@link VarHandle} for base hook callbacks. */
    private final ClassProcessor delayedProcessor;

    HookRequest(HookRequestBuilder builder) throws Throwable {
        this.customHooksCallback = builder.compute();
        this.baseHooksCallback = builder.baseHooksCallback;
        this.delayedProcessor = builder.hookProcessor.cp;
    }

    public void invoke(Object target, Object additional) throws Throwable {
        // First we process all hooks implemented by users.
        //
        for (TinyPair<Hook, MethodHandle> c = customHooksCallback; c != null; c = c.next) {
            invokeHook(c.element2, c.element1, target, additional);
        }
        // Invoke OnHook methods on the Bundle or Extension that takes a base hook
        if (baseHooksCallback != null) {
            try (UnreflectGate hp = new UnreflectGate(delayedProcessor.copy(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
                for (Tiny<BaseHookCallback> t = baseHooksCallback; t != null; t = t.next) {
                    invokeHook(t.element.mh, t.element.toHook(hp), target, additional);
                }
            }
        }
    }

    private void invokeHook(MethodHandle mh, Hook hook, Object target, Object additional) throws Throwable {
        // Its somewhat of a hack, but will do for now.
        // As neither ContainerSource or Bundle can implement Hook (Dobbeltcheck)
        boolean isStatic = Hook.class.isAssignableFrom(mh.type().parameterType(0));
        if (isStatic) {
            if (mh.type().parameterCount() == 1) {
                mh.invoke(hook);
            } else {
                mh.invoke(hook, additional);
            }
        } else {
            if (mh.type().parameterCount() == 2) {
                mh.invoke(target, hook);
            } else {
                mh.invoke(target, hook, additional);
            }
        }
    }

    /// This is necessary because we can only fields and methods once. Without scanning everything again
    static class BaseHookCallback {

        /** Is null for {@link AssignableToHook}. */
        @Nullable
        private final Annotation annotation;

        private final Object member;

        /** The method handle to method annotated with {@link OnHook}. */
        private final MethodHandle mh;

        BaseHookCallback(Object member, Annotation annotation, MethodHandle mh) {
            this.member = requireNonNull(member);
            this.annotation = annotation; // Null for AssignableTo
            this.mh = requireNonNull(mh);
        }

        private Hook toHook(UnreflectGate hp) {
            if (annotation == null) {
                return ModuleAccess.hook().newAssignableToHook(hp, (Class<?>) member);
            } else if (member instanceof Field) {
                return ModuleAccess.hook().newAnnotatedFieldHook(hp, (Field) member, annotation);
            } else if (member instanceof Method) {
                return ModuleAccess.hook().newAnnotatedMethodHook(hp, (Method) member, annotation);
            } else {
                return ModuleAccess.hook().newAnnotatedTypeHook(hp, (Class<?>) member, annotation);
            }
        }
    }

}
