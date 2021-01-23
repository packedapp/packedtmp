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
package packed.internal.hooks;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;

import app.packed.base.Key;
import app.packed.container.InternalExtensionException;
import app.packed.inject.Provide;
import app.packed.state.OnInitialize;
import packed.internal.classscan.OpenClass;
import packed.internal.errorhandling.UncheckedThrowableFactory;

/**
 *
 */
public final class MethodScan {

    final OpenClass oc;

    MethodScan(OpenClass oc) {
        this.oc = requireNonNull(oc);
    }

    private final HashMap<Key<?>, ContextMethodProvide.Builder> providing = new HashMap<>();

    private MethodHandle onInitialize;

    public void scan() {
        oc.findMethods(m -> {
            Provide ap = m.getAnnotation(Provide.class);
            if (ap != null) {
                MethodHandle mh = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                ContextMethodProvide.Builder b = new ContextMethodProvide.Builder(m, mh);
                if (providing.putIfAbsent(b.key, b) != null) {
                    throw new InternalExtensionException("Multiple methods on " + oc.type() + " that provide " + b.key);
                }
            }

            OnInitialize oi = m.getAnnotation(OnInitialize.class);
            if (oi != null) {
                if (onInitialize != null) {
                    throw new IllegalStateException(oc.type() + " defines more than one method annotated with " + OnInitialize.class.getSimpleName());
                }
                MethodHandle mh = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                onInitialize = mh;
            }
        });
    }
}
