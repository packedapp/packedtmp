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

import java.lang.reflect.UndeclaredThrowableException;

import app.packed.hook.Hook;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public class UseIt2 {

    @SuppressWarnings("unchecked")
    public static <T extends Hook> T test(ClassProcessor cpHook, ClassProcessor cpTarget) {
        OnHookModel model = OnHookModel.newInstance(cpHook);
        HookTargetProcessor hc = new HookTargetProcessor(cpTarget, UncheckedThrowableFactory.ASSERTION_ERROR);
        HookRequest.Builder hb = new HookRequest.Builder(model, hc);

        try {

            hb.processMembers(cpTarget);
            hc.close();
            hb.compute();
        } catch (Throwable t) {
            ThrowableUtil.rethrowErrorOrRuntimeException(t);
            throw new UndeclaredThrowableException(t);
        }

        Object a = hb.array[0];
        return a == null ? null : (T) (((Hook.Builder<?>) a).build());
    }

}
