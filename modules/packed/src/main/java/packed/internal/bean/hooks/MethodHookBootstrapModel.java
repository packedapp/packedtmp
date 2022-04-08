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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.bean.hooks.OldBeanMethod;
import app.packed.bean.hooks.sandbox.MethodAccessor;
import app.packed.bean.hooks.sandbox.RealMethodSidecarBootstrap;
import app.packed.bean.hooks.BeanMethodHook;
import app.packed.bean.hooks.scrap.ScopedProvide;
import app.packed.extension.InternalExtensionException;
import packed.internal.bean.hooks.usesite.UseSiteMethodHookModel;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** A model of a {@link OldBeanMethod} class. */
public final class MethodHookBootstrapModel extends AbstractHookModel<RealMethodSidecarBootstrap> {

    /** A MethodHandle that can invoke {@link OldBeanMethod#bootstrap}. */
    private static final MethodHandle MH_METHOD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OldBeanMethod.class,
            "bootstrap", void.class);

    /** A VarHandle that can access {@link OldBeanMethod#processor}. */
    private static final VarHandle VH_METHOD_SIDECAR_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), OldBeanMethod.class,
            "builder", UseSiteMethodHookModel.Builder.class);

    public final Map<Key<?>, HookedMethodProvide> keys;

    // Must take an invoker...
  //  public final MethodHandle onInitialize;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder
     */
    private MethodHookBootstrapModel(Builder builder) {
        super(builder);
      //  this.onInitialize = builder.onInitialize;
        Map<Key<?>, HookedMethodProvide> tmp = new HashMap<>();
        builder.providing.forEach((k, v) -> tmp.put(k, v.build(this)));
        this.keys = builder.providing.size() == 0 ? null : Map.copyOf(tmp);
    }

    public OldBeanMethod bootstrap(UseSiteMethodHookModel.Builder builder) {
        OldBeanMethod instance = (OldBeanMethod) newInstance();

        VH_METHOD_SIDECAR_CONFIGURATION.set(instance, builder);
        try {
            MH_METHOD_HOOK_BOOTSTRAP.invoke(instance); // Invokes Bootstrap#bootstrap()
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return instance;
    }

    public void clearBuilder(Object instance) {
        VH_METHOD_SIDECAR_CONFIGURATION.set(instance, null); // clears the configuration
    }

    public static MethodHookBootstrapModel getModelForFake(Class<? extends OldBeanMethod> c) {
        return new Builder(c).build();
    }

    /** A builder for method sidecar. This class is public because it used from {@link OldMeHok}. */
    public final static class Builder extends AbstractHookModel.Builder<RealMethodSidecarBootstrap> {

        Class<?> invoker;

       // private MethodHandle onInitialize;

        private final HashMap<Key<?>, HookedMethodProvide.Builder> providing = new HashMap<>();

        Builder(Class<?> c) {
            super(c);
        }

        public Builder(BeanMethodHook ams) {
            super(ams.bootstrap());
        }

        /** {@inheritDoc} */
        @Override
        public MethodHookBootstrapModel build() {

            scan(false, OldBeanMethod.class);

            return new MethodHookBootstrapModel(this);
        }

        @Override
        protected void onMethod(Method method) {
            ScopedProvide ap = method.getAnnotation(ScopedProvide.class);
            if (ap != null) {
                MethodHandle mh = oc.unreflect(method);
                HookedMethodProvide.Builder b = new HookedMethodProvide.Builder(method, mh);
                if (providing.putIfAbsent(b.key, b) != null) {
                    throw new InternalExtensionException("Multiple methods on " + classToScan + " that provide " + b.key);
                }
            }

//            OnInitialize oi = method.getAnnotation(OnInitialize.class);
//            if (oi != null) {
//                if (onInitialize != null) {
//                    throw new IllegalStateException(classToScan + " defines more than one method annotated with " + OnInitialize.class.getSimpleName());
//                }
//                MethodHandle mh = oc.unreflect(method);
//                onInitialize = mh;
//            }
        }

        public void provideInvoker() {
            if (invoker != null) {
                throw new IllegalStateException("Cannot provide more than 1 " + MethodAccessor.class.getSimpleName());
            }
            invoker = Object.class;
        }
    }
}
