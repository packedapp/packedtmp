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
package packed.internal.hook.model;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.atomic.AtomicReference;

import app.packed.hook.Hook;
import app.packed.hook.Hook.Builder;
import packed.internal.container.access.ClassProcessor;
import packed.internal.hook.model.OnHookContainerModelBuilder.OnHookEntry;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class UseIt {

    public static <T extends Hook> T test(Lookup caller, Class<T> hookType, Class<?> target) {
        requireNonNull(caller, "caller is null");
        requireNonNull(hookType, "hookType is null");
        requireNonNull(target, "target is null");
        ClassProcessor cp = new ClassProcessor(caller, hookType, false);

        OnHookContainerModelBuilder ohs = new OnHookContainerModelBuilder(cp);
        ohs.process();

        AtomicReference<Hook.Builder<?>> ar = new AtomicReference<>();
        cp.findMethodsAndFields(c -> {}, f -> {
            if (ohs.onHookAnnotatedFields != null) {
                for (Annotation a : f.getAnnotations()) {
                    OnHookEntry e = ohs.onHookAnnotatedFields.get(a.annotationType());
                    while (e != null) {
                        Hook.Builder<?> builder = (Builder<?>) invoke(e.builder.constructor);
                        ar.set(builder);
                        try {
                            e.methodHandle.invoke(builder);
                        } catch (Throwable e1) {

                        }
                    }
                }
            }
        });

        return null;
    }

    private static Object invoke(MethodHandle mh, Object... vars) {
        try {
            return mh.invoke(vars);
        } catch (Throwable e) {
            ThrowableUtil.rethrowErrorOrRuntimeException(e);
            throw new UndeclaredThrowableException(e);
        }
    }
}
