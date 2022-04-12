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
package packed.internal.bean.hooks;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.bean.hooks.OldBeanField;
import app.packed.bean.hooks.OldBeanFieldHook;

/** A model of a {@link OldBeanField field bootstrap} implementation. */
public final class FieldHookModel extends AbstractHookModel<OldBeanField> {

    public final Map<Key<?>, HookedMethodProvide> keys;

    // Must take an invoker...
   // public final MethodHandle onInitialize;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder
     */
    private FieldHookModel(Builder builder) {
        super(builder);
        //this.onInitialize = builder.onInitialize;
        Map<Key<?>, HookedMethodProvide> tmp = new HashMap<>();
        builder.providing.forEach((k, v) -> tmp.put(k, v.build(this)));
        this.keys = builder.providing.size() == 0 ? null : Map.copyOf(tmp);
    }


    public static FieldHookModel getModelForFake(Class<? extends OldBeanField> c) {
        return new Builder(c).build();
    }

    /** A builder for for a {@link FieldHookModel}. */
    public final static class Builder extends AbstractHookModel.Builder<OldBeanField> {

     //   private MethodHandle onInitialize;

        private final HashMap<Key<?>, HookedMethodProvide.Builder> providing = new HashMap<>();

        private Builder(Class<? extends OldBeanField> c) {
            super(c);
        }

        public Builder(OldBeanFieldHook afs) {
            super(afs.processor());
        }

        /** {@inheritDoc} */
        @Override
        public FieldHookModel build() {
            scan(false, OldBeanField.class);
            return new FieldHookModel(this);
        }

        @Override
        protected void onMethod(Method method) {
//            ScopedProvide ap = method.getAnnotation(ScopedProvide.class);
//            if (ap != null) {
//                MethodHandle mh = oc.unreflect(method);
//                HookedMethodProvide.Builder b = new HookedMethodProvide.Builder(method, mh);
//                if (providing.putIfAbsent(b.key, b) != null) {
//                    throw new InternalExtensionException("Multiple methods on " + classToScan + " that provide " + b.key);
//                }
//            }

//            OnInitialize oi = method.getAnnotation(OnInitialize.class);
//            if (oi != null) {
//                if (onInitialize != null) {
//                    throw new IllegalStateException(classToScan + " defines more than one method annotated with " + OnInitialize.class.getSimpleName());
//                }
//                MethodHandle mh = oc.unreflect(method);
//                onInitialize = mh;
//            }
        }
    }
}
