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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.extension.InternalExtensionException;
import app.packed.hooks.OldFieldHook;
import app.packed.hooks.OldFieldHook.Bootstrap;
import app.packed.hooks.accessors.HookProvide;
import app.packed.lifecycle.OnInitialize;

/** A model of a {@link Bootstrap field bootstrap} implementation. */
public final class FieldHookModel extends AbstractHookModel<OldFieldHook.Bootstrap> {

    public final Map<Key<?>, HookedMethodProvide> keys;

    // Must take an invoker...
    public final MethodHandle onInitialize;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder
     */
    private FieldHookModel(Builder builder) {
        super(builder);
        this.onInitialize = builder.onInitialize;
        Map<Key<?>, HookedMethodProvide> tmp = new HashMap<>();
        builder.providing.forEach((k, v) -> tmp.put(k, v.build(this)));
        this.keys = builder.providing.size() == 0 ? null : Map.copyOf(tmp);
    }


    public static FieldHookModel getModelForFake(Class<? extends OldFieldHook.Bootstrap> c) {
        return new Builder(c).build();
    }

    /** A builder for for a {@link FieldHookModel}. */
    public final static class Builder extends AbstractHookModel.Builder<OldFieldHook.Bootstrap> {

        private MethodHandle onInitialize;

        private final HashMap<Key<?>, HookedMethodProvide.Builder> providing = new HashMap<>();

        private Builder(Class<? extends OldFieldHook.Bootstrap> c) {
            super(c);
        }

        public Builder(OldFieldHook afs) {
            super(afs.bootstrap());
        }

        /** {@inheritDoc} */
        @Override
        public FieldHookModel build() {
            scan(false, OldFieldHook.Bootstrap.class);
            return new FieldHookModel(this);
        }

        @Override
        protected void onMethod(Method method) {
            HookProvide ap = method.getAnnotation(HookProvide.class);
            if (ap != null) {
                MethodHandle mh = oc.unreflect(method);
                HookedMethodProvide.Builder b = new HookedMethodProvide.Builder(method, mh);
                if (providing.putIfAbsent(b.key, b) != null) {
                    throw new InternalExtensionException("Multiple methods on " + classToScan + " that provide " + b.key);
                }
            }

            OnInitialize oi = method.getAnnotation(OnInitialize.class);
            if (oi != null) {
                if (onInitialize != null) {
                    throw new IllegalStateException(classToScan + " defines more than one method annotated with " + OnInitialize.class.getSimpleName());
                }
                MethodHandle mh = oc.unreflect(method);
                onInitialize = mh;
            }
        }
    }
}
