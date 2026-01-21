/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.build.hook;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import internal.app.packed.build.hooks.BuildHook;

/**
 *
 */

public class StaticBuildHookMap implements BuildHookMap {

    private final Map<Class<? extends BuildHook>, List<BuildHook>> hooks;
    private final BuildHookMap parent = null;

    public StaticBuildHookMap(Map<Class<? extends BuildHook>, List<BuildHook>> hooks) {
        hooks.replaceAll((_, v) -> List.copyOf(v));
        this.hooks = Map.copyOf(hooks);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends BuildHook> void forEach(Class<T> hookType, Consumer<? super T> action) {
        if (parent != null) {
            parent.forEach(hookType, action);
        }

        // Try and find a list of hooks of the specified type
        @SuppressWarnings("unchecked")
        List<T> list = (List<T>) hooks.get(hookType);
        if (list != null) {
            for (T t : list) {
                action.accept(t);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T extends BuildHook> void forEachReversed(Class<T> hookType, Consumer<? super T> action) {
        // Try and find a list of hooks of the specified type and run in reverse order
        @SuppressWarnings("unchecked")
        List<T> list = (List<T>) hooks.get(hookType);
        if (list != null) {
            for (int i = list.size() - 1; i >= 0; i--) {
                T element = list.get(i);
                action.accept(element);
            }
        }

        if (parent != null) {
            parent.forEachReversed(hookType, action);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BuildHook> List<T> models(Class<T> hookType) {
        List<T> list = (List<T>) hooks.get(hookType);
        return list == null ? List.of() : list;
    }
}
