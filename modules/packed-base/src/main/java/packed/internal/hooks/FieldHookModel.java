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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.InternalExtensionException;
import app.packed.hooks.FieldHook;
import app.packed.hooks.FieldHook.Bootstrap;
import app.packed.inject.Provide;
import app.packed.state.OnInitialize;
import packed.internal.container.ExtensionModel;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hooks.usesite.HookUseSite;

/** A model of a {@link Bootstrap field bootstrap} implementation. */
public final class FieldHookModel extends AbstractHookModel<FieldHook.Bootstrap> {

    /** A cache of any extensions a particular annotation activates. */
    static final ClassValue<FieldHookModel> ANNOTATION_ON_METHOD_SIDECARS = new ClassValue<>() {

        @Override
        protected FieldHookModel computeValue(Class<?> type) {
            FieldHook afs = type.getAnnotation(FieldHook.class);
            return afs == null ? null : new Builder(afs).build();
        }
    };

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

    @Nullable
    public static FieldHookModel of(HookUseSite useSite, ExtensionModel extension, Class<? extends Annotation> c) {
        requireNonNull(useSite);
        return switch (useSite) {
        case COMPONENT_SOURCE -> ANNOTATION_ON_METHOD_SIDECARS.get(c);
        case APPLICATION_SHELL -> throw new UnsupportedOperationException();
        case HOOK_CLASS -> throw new UnsupportedOperationException();
        };
    }

    public static FieldHookModel getModelForFake(Class<? extends FieldHook.Bootstrap> c) {
        return new Builder(c).build();
    }

    /** A builder for for a {@link FieldHookModel}. */
    private final static class Builder extends AbstractHookModel.Builder<FieldHook.Bootstrap> {

        private MethodHandle onInitialize;

        private final HashMap<Key<?>, HookedMethodProvide.Builder> providing = new HashMap<>();

        private Builder(Class<? extends FieldHook.Bootstrap> c) {
            super(c);
        }

        private Builder(FieldHook afs) {
            super(afs.bootstrap());
        }

        /** {@inheritDoc} */
        @Override
        protected FieldHookModel build() {
            scan(false, FieldHook.Bootstrap.class);
            return new FieldHookModel(this);
        }

        @Override
        protected void onMethod(Method method) {
            Provide ap = method.getAnnotation(Provide.class);
            if (ap != null) {
                MethodHandle mh = oc.unreflect(method, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
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
                MethodHandle mh = oc.unreflect(method, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                onInitialize = mh;
            }
        }
    }
}
