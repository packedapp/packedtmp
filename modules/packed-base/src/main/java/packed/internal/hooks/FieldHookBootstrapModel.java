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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.InternalExtensionException;
import app.packed.hooks.FieldHook;
import app.packed.hooks.FieldHook.Bootstrap;
import app.packed.inject.Provide;
import app.packed.state.OnInitialize;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.invoke.ClassMemberAccessor;

/** A model of a {@link Bootstrap field bootstrap} implementation. */
public final class FieldHookBootstrapModel extends AbstractHookBootstrapModel<FieldHook.Bootstrap> {

    /** A cache of any extensions a particular annotation activates. */
    static final ClassValue<FieldHookBootstrapModel> ANNOTATION_ON_METHOD_SIDECARS = new ClassValue<>() {

        @Override
        protected FieldHookBootstrapModel computeValue(Class<?> type) {
            FieldHook afs = type.getAnnotation(FieldHook.class);
            return afs == null ? null : new Builder(afs).build();
        }
    };

    public final Map<Key<?>, ContextMethodProvide> keys;

    // Must take an invoker...
    public final MethodHandle onInitialize;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder
     */
    private FieldHookBootstrapModel(Builder builder) {
        super(builder);
        this.onInitialize = builder.onInitialize;
        Map<Key<?>, ContextMethodProvide> tmp = new HashMap<>();
        builder.providing.forEach((k, v) -> tmp.put(k, v.build(this)));
        this.keys = builder.providing.size() == 0 ? null : Map.copyOf(tmp);
    }

    @Nullable
    public static FieldHookBootstrapModel getModelForAnnotatedMethod(Class<? extends Annotation> c) {
        return ANNOTATION_ON_METHOD_SIDECARS.get(c);
    }

    public static FieldHookBootstrapModel getModelForFake(Class<? extends FieldHook.Bootstrap> c) {
        return new Builder(c).build();
    }

    /** A builder for for a {@link FieldHookBootstrapModel}. */
    private final static class Builder extends AbstractHookBootstrapModel.Builder<FieldHook.Bootstrap> {

        private MethodHandle onInitialize;

        private final HashMap<Key<?>, ContextMethodProvide.Builder> providing = new HashMap<>();

        private Builder(Class<? extends FieldHook.Bootstrap> c) {
            super(c);
        }

        private Builder(FieldHook afs) {
            super(afs.bootstrap());
        }

        /** {@inheritDoc} */
        @Override
        protected FieldHookBootstrapModel build() {
            ClassMemberAccessor oc = ib.oc();
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

            return new FieldHookBootstrapModel(this);
        }
    }
}
