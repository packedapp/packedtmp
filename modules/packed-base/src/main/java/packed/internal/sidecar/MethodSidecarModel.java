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
package packed.internal.sidecar;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.InternalExtensionException;
import app.packed.inject.Provide;
import app.packed.sidecar.ActivateMethodSidecar;
import app.packed.sidecar.Invoker;
import app.packed.sidecar.MethodSidecar;
import app.packed.statemachine.OnInitialize;
import packed.internal.classscan.OpenClass;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.methodhandle.LookupUtil;

/** A model of a {@link MethodSidecar}. */
public final class MethodSidecarModel extends SidecarModel<MethodSidecar> {

    /** A cache of any extensions a particular annotation activates. */
    static final ClassValue<MethodSidecarModel> ANNOTATION_ON_METHOD_SIDECARS = new ClassValue<>() {

        @Override
        protected MethodSidecarModel computeValue(Class<?> type) {
            ActivateMethodSidecar ams = type.getAnnotation(ActivateMethodSidecar.class);
            return ams == null ? null : new Builder(ams).build();
        }
    };

    /** A MethodHandle that can invoke MethodSidecar#configure. */
    private static final MethodHandle MH_METHOD_SIDECAR_CONFIGURE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), MethodSidecar.class, "configure",
            void.class);

    /** A VarHandle that can access MethodSidecar#configuration. */
    private static final VarHandle VH_METHOD_SIDECAR_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), MethodSidecar.class, "builder",
            MethodSidecarModel.Builder.class);

    public final Map<Key<?>, SidecarContextDependencyProvider> keys;

    // Must take an invoker...
    public final MethodHandle onInitialize;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            the builder
     */
    private MethodSidecarModel(Builder builder) {
        super(builder);
        this.onInitialize = builder.onInitialize;
        Map<Key<?>, SidecarContextDependencyProvider> tmp = new HashMap<>();
        builder.providing.forEach((k, v) -> tmp.put(k, v.build(this)));
        this.keys = builder.providing.size() == 0 ? null : Map.copyOf(tmp);
    }

    @Nullable
    public static MethodSidecarModel getModelForAnnotatedMethod(Class<? extends Annotation> c) {
        return ANNOTATION_ON_METHOD_SIDECARS.get(c);
    }

    /** A builder for method sidecar. This class is public because it used from {@link MethodSidecar}. */
    public final static class Builder extends SidecarModel.Builder<MethodSidecar> {

        Class<?> invoker;

        private MethodHandle onInitialize;

        private final HashMap<Key<?>, SidecarContextDependencyProvider.Builder> providing = new HashMap<>();

        Builder(ActivateMethodSidecar ams) {
            super(VH_METHOD_SIDECAR_CONFIGURATION, MH_METHOD_SIDECAR_CONFIGURE, ams.sidecar());
        }

        /** {@inheritDoc} */
        @Override
        protected MethodSidecarModel build() {
            super.configure();
            OpenClass oc = ib.oc();
            oc.findMethods(m -> {
                Provide ap = m.getAnnotation(Provide.class);
                if (ap != null) {
                    MethodHandle mh = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    if (!Modifier.isStatic(m.getModifiers())) {
                        mh = mh.bindTo(instance);
                    }
                    SidecarContextDependencyProvider.Builder b = new SidecarContextDependencyProvider.Builder(m, mh);
                    if (providing.putIfAbsent(b.key, b) != null) {
                        throw new InternalExtensionException("Multiple methods on " + instance.getClass() + " that provide " + b.key);
                    }
                }

                OnInitialize oi = m.getAnnotation(OnInitialize.class);
                if (oi != null) {
                    if (onInitialize != null) {
                        throw new IllegalStateException(oc.type() + " defines more than one method annotated with " + OnInitialize.class.getSimpleName());
                    }
                    MethodHandle mh = oc.unreflect(m, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    if (!Modifier.isStatic(m.getModifiers())) {
                        mh = mh.bindTo(instance);
                    }

                    onInitialize = mh;
                }
            });

            return new MethodSidecarModel(this);
        }

        public void provideInvoker() {
            if (invoker != null) {
                throw new IllegalStateException("Cannot provide more than 1 " + Invoker.class.getSimpleName());
            }
            invoker = Object.class;
        }
    }

}
