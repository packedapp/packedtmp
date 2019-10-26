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
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.hook.Hook;
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

    /** A list of custom hook callbacks for the particular extension. */
    final TinyPair<Hook, MethodHandle> customHooksCallback;

    @Nullable
    final Tiny<DelayedAnnotatedMember> delayedMembers;

    final ClassProcessor delayedProcessor;

    protected HookRequest(HookRequestBuilder builder) throws Throwable {
        this.customHooksCallback = builder.compute();
        this.delayedMembers = builder.delayedMembers;
        this.delayedProcessor = builder.hookProcessor.cp;
    }

    public void invokeIt(Object target, Object additional) throws Throwable {
        // TODO support static....

        for (TinyPair<Hook, MethodHandle> c = customHooksCallback; c != null; c = c.next) {
            invokeHook(c.element2, c.element1, target, additional);
        }
        // Invoke OnHook methods on the Bundle or Extension that takes a base hook
        if (delayedMembers != null) {
            try (HookTargetProcessor hp = new HookTargetProcessor(delayedProcessor.copy(), UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {
                for (Tiny<DelayedAnnotatedMember> t = delayedMembers; t != null; t = t.next) {
                    invokeHook(t.element.mh, t.element.toHook(hp), target, additional);
                }
            }
        }
    }

    private void invokeHook(MethodHandle mh, Hook hook, Object target, Object additional) throws Throwable {
        if (mh.type().parameterCount() == 2) {
            mh.invoke(target, hook);
        } else {
            mh.invoke(target, hook, additional);
        }
    }

    /// This is necessary because we can only fields and methods once. Without scanning everything again
    static class DelayedAnnotatedMember {
        private final Annotation annotation;
        private final Object member;
        private final MethodHandle mh;

        DelayedAnnotatedMember(Object member, Annotation annotation, MethodHandle mh) {
            this.member = requireNonNull(member);
            this.annotation = annotation; // Null for AssignableTo
            this.mh = requireNonNull(mh);
        }

        private Hook toHook(HookTargetProcessor hp) {
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
