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

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.hook.Hook;
import packed.internal.reflect.ClassProcessor;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
public class UseIt2 {

    @SuppressWarnings("unchecked")
    public static <T extends Hook> T test(Lookup caller, Class<T> hookType, Class<?> target) {
        requireNonNull(caller, "caller is null");
        requireNonNull(hookType, "hookType is null");
        requireNonNull(target, "target is null");
        ClassProcessor cp = new ClassProcessor(caller, hookType, false);

        OnHookContainerModelBuilder ohs = new OnHookContainerModelBuilder(cp);
        ohs.findAllHooks();

        OnHookContainerModel m = new OnHookContainerModel(ohs);
        ClassProcessor cpTarget = new ClassProcessor(caller, target, false);

        return (T) m.process(null, cpTarget, UncheckedThrowableFactory.ASSERTION_ERROR);
    }

}
