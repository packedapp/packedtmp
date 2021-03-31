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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.InternalExtensionException;
import app.packed.hooks.MethodAccessor;
import app.packed.hooks.MethodHook;
import app.packed.hooks.MethodHook.Bootstrap;
import app.packed.hooks.RealMethodSidecarBootstrap;
import app.packed.inject.Provide;
import app.packed.state.OnInitialize;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hooks.usesite.UseSiteMethodHookModel;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** A model of a {@link Bootstrap} class. */
public final class MethodHookModel extends AbstractHookModel<RealMethodSidecarBootstrap> {

    /** A cache of any extensions a particular annotation activates. */
    private static final ClassValue<MethodHookModel> EXTENSION_METHOD_ANNOTATION = new ClassValue<>() {

        @Override
        protected MethodHookModel computeValue(Class<?> type) {
            MethodHook ams = type.getAnnotation(MethodHook.class);
            return ams == null ? null : new Builder(ams).build();
        }
    };

    @Nullable
    public static MethodHookModel getForAnnotatedMethod(Class<? extends Annotation> c) {
        return EXTENSION_METHOD_ANNOTATION.get(c);
    }

    /** A MethodHandle that can invoke {@link MethodHook.Bootstrap#bootstrap}. */
    private static final MethodHandle MH_METHOD_SIDECAR_CONFIGURE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), MethodHook.Bootstrap.class,
            "bootstrap", void.class);

    /** A VarHandle that can access {@link MethodHook.Bootstrap#builder}. */
    private static final VarHandle VH_METHOD_SIDECAR_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), MethodHook.Bootstrap.class,
            "builder", UseSiteMethodHookModel.Builder.class);

    public final Map<Key<?>, ContextMethodProvide> keys;

    // Must take an invoker...
    public final MethodHandle onInitialize;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder
     */
    private MethodHookModel(Builder builder) {
        super(builder);
        this.onInitialize = builder.onInitialize;
        Map<Key<?>, ContextMethodProvide> tmp = new HashMap<>();
        builder.providing.forEach((k, v) -> tmp.put(k, v.build(this)));
        this.keys = builder.providing.size() == 0 ? null : Map.copyOf(tmp);
    }

    public void clearBuilder(Object instance) {
        VH_METHOD_SIDECAR_CONFIGURATION.set(instance, null); // clears the configuration
    }

    public static MethodHookModel getModelForFake(Class<? extends MethodHook.Bootstrap> c) {
        return new Builder(c).build();
    }

    public MethodHook.Bootstrap configure(UseSiteMethodHookModel.Builder builder) {
        MethodHook.Bootstrap instance = (Bootstrap) newInstance();

        VH_METHOD_SIDECAR_CONFIGURATION.set(instance, builder);
        try {
            MH_METHOD_SIDECAR_CONFIGURE.invoke(instance); // Invokes sidecar#configure()
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return instance;
    }

    /** A builder for method sidecar. This class is public because it used from {@link MethodHook}. */
    public final static class Builder extends AbstractHookModel.Builder<RealMethodSidecarBootstrap> {

        Class<?> invoker;

        private MethodHandle onInitialize;

        private final HashMap<Key<?>, ContextMethodProvide.Builder> providing = new HashMap<>();

        Builder(MethodHook ams) {
            super(ams.bootstrap());
        }

        Builder(Class<?> c) {
            super(c);
        }

        /** {@inheritDoc} */
        @Override
        protected MethodHookModel build() {
            
            scan(false, MethodHook.Bootstrap.class);
            
            return new MethodHookModel(this);
        }

        public void provideInvoker() {
            if (invoker != null) {
                throw new IllegalStateException("Cannot provide more than 1 " + MethodAccessor.class.getSimpleName());
            }
            invoker = Object.class;
        }

        @Override
        protected void onMethod(Method method) {
            Provide ap = method.getAnnotation(Provide.class);
            if (ap != null) {
                MethodHandle mh = ib.oc().unreflect(method, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                ContextMethodProvide.Builder b = new ContextMethodProvide.Builder(method, mh);
                if (providing.putIfAbsent(b.key, b) != null) {
                    throw new InternalExtensionException("Multiple methods on " + ib.type() + " that provide " + b.key);
                }
            }

            OnInitialize oi = method.getAnnotation(OnInitialize.class);
            if (oi != null) {
                if (onInitialize != null) {
                    throw new IllegalStateException(ib.type() + " defines more than one method annotated with " + OnInitialize.class.getSimpleName());
                }
                MethodHandle mh = ib.oc().unreflect(method, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                onInitialize = mh;
            }
        }
    }
}
