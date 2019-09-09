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
package packed.internal.container.extension.hook.other;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import packed.internal.container.model.ComponentLookup;

/**
 *
 */
// Ideen er at wrappe ComponentLook og saa give den til PackedFieldAnnotated + MethodAnnotated

public class HookBoss {
    private ComponentLookup lookup;

    HookBoss(ComponentLookup lookup) {
        this.lookup = requireNonNull(lookup);
    }

    void disable() {
        lookup = null;
    }

    private ComponentLookup lookup() {
        ComponentLookup l = lookup;
        if (l == null) {
            throw new IllegalStateException();
        }
        return l;
    }

    public MethodHandle unreflect(Method method) {
        return lookup().unreflect(method);
    }

    public MethodHandle unreflectGetter(Field field) {
        return lookup().unreflectGetter(field);
    }

    public MethodHandle unreflectSetter(Field field) {
        return lookup().unreflectSetter(field);
    }

    public VarHandle unreflectVarhandle(Field field) {
        return lookup().unreflectVarhandle(field);
    }
}
