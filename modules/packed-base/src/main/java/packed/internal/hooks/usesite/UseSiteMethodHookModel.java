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
package packed.internal.hooks.usesite;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.hooks.MethodHook;
import packed.internal.component.ComponentSetup;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hooks.OldMethodHookModel;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.hooks.ContextMethodProvide;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * We create an
 */
public final class UseSiteMethodHookModel extends UseSiteMemberHookModel {

    /** A VarHandle that can access {@link MethodHook.Bootstrap#builder}. */
    private static final VarHandle VH_METHOD_SIDECAR_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), MethodHook.Bootstrap.class,
            "builder", UseSiteMethodHookModel.Builder.class);

    /** A model of the bootstrap class. */
    public final OldMethodHookModel bootstrapModel;

    /** A direct method handle to the method. */
    private final MethodHandle directMethodHandle;

    /** The method we are creating a model for. */
    private final Method method;

    private UseSiteMethodHookModel(Builder builder) {
        super(builder, DependencyDescriptor.fromExecutable(builder.shared.methodUnsafe));
        this.method = requireNonNull(builder.shared.methodUnsafe);
        this.bootstrapModel = requireNonNull(builder.model);
        this.directMethodHandle = requireNonNull(builder.shared.direct());
    }

    /** A MethodHandle that can invoke {@link MethodHook.Bootstrap#bootstrap}. */
    private static final MethodHandle MH_METHOD_SIDECAR_CONFIGURE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), MethodHook.Bootstrap.class,
            "bootstrap", void.class);

    @Override
    public DependencyProvider[] createProviders() {
        DependencyProvider[] providers = new DependencyProvider[directMethodHandle.type().parameterCount()];
        // System.out.println("RESOLVING " + directMethodHandle);
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyDescriptor d = dependencies.get(i);
            ContextMethodProvide dp = bootstrapModel.keys.get(d.key());
            if (dp != null) {
                // System.out.println("MAtches for " + d.key());
                int index = i + directMethodHandle.type().parameterCount() == dependencies.size() ? 0 : 1;
                providers[index] = dp;
                // System.out.println("SEtting provider " + dp.dependencyAccessor());
            }
        }

        return providers;
    }

    /** {@inheritDoc} */
    @Override
    public int getModifiers() {
        return method.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle methodHandle() {
        return directMethodHandle;
    }

    public static void process(HookedClassModel.Builder source, Method method) {
        Shared shared = null;
        for (Annotation a : method.getAnnotations()) {
            OldMethodHookModel model = OldMethodHookModel.getForAnnotatedMethod(a.annotationType());
            if (model != null) {
                if (shared == null) {
                    shared = new Shared(source, method);
                }
                Builder builder = new Builder(model, shared);

                MethodHook.Bootstrap bootstrap = model.bootstrap(builder);
                if (builder.managedBy == null) {
                    model.clearBuilder(bootstrap);
                }
                if (builder.buildtimeModel != null) {
                    UseSiteMethodHookModel smm = new UseSiteMethodHookModel(builder);
                    shared.source.methods.add(smm);
                }
            }
        }
    }

    /** A builder. */
    public static final class Builder extends UseSiteMemberHookModel.Builder {

        /** The method, if exposed to end-users. */
        @Nullable
        private Method exposedMethod;

        /** A model of the bootstrap class. */
        private final OldMethodHookModel model;

        /** The shared context. */
        private final Shared shared;

        private final Method unsafeMethod;

        Builder(OldMethodHookModel model, Shared shared) {
            super(shared.source, model);
            this.shared = requireNonNull(shared);
            this.model = requireNonNull(model);
            this.unsafeMethod = shared.methodUnsafe;
        }

        Builder(HookedClassModel.Builder source, OldMethodHookModel model, Method method) {
            this(model, new Shared(source, method));
        }

        Object initialize() {
            Object instance = model.newInstance();
            VH_METHOD_SIDECAR_CONFIGURATION.set(instance, this);
            try {
                MH_METHOD_SIDECAR_CONFIGURE.invoke(instance); // Invokes sidecar#configure()
                return instance;
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return unsafeMethod.getAnnotation(annotationClass);
        }

        public MethodHandle methodHandle() {
            return shared.direct();
        }

        /**
         * Returns a method that can safely be shared with end-users.
         * 
         * @return a method that can safely be shared with end-users
         */
        public Method methodSafe() {
            Method m = exposedMethod;
            if (m == null) {
                // We want to avoid sharing the method instance between multiple sidecars
                // Because one sidecar might invoke #setAccessible(true) which would allow
                // another sidecar unchecked access to invoke the method.
                // Internally we never call #setAccessible(true) so we can share the method
                // freely with exactly one sidecar. However, is the method requested by more
                // than 1 sidecar we need to create a new method instance.
                if (shared.isMethodUsed) {
                    try {
                        m = exposedMethod = unsafeMethod.getDeclaringClass().getDeclaredMethod(unsafeMethod.getName(), unsafeMethod.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    m = exposedMethod = unsafeMethod;
                    shared.isMethodUsed = true;
                }
            }
            return m;
        }

        /**
         * Returns a method that should not be shared with end-users.
         * 
         * @return a method that should not be shared with end-users
         */
        public Method methodUnsafe() {
            return unsafeMethod;
        }

        public void serviceRegister(boolean isConstant) {
            provideAsConstant = isConstant;
            provideAsKey = Key.convertMethodReturnType(unsafeMethod);
        }

        public void serviceRegister(boolean isConstant, Key<?> key) {
            provideAsConstant = isConstant;
            // Check assignable.
            provideAsKey = key;
        }

        public static void registerProcessor(MethodHook.Bootstrap sidecar, Consumer<? super ComponentSetup> processor) {
            Builder b = (Builder) VH_METHOD_SIDECAR_CONFIGURATION.get(sidecar);
            b.processor = processor;
        }

        /**
         * @param b
         */
        public void buildWith(Object b) {}
    }

    public enum RunAt {
        INITIALIZATION;
    }

    /**
     * This class mainly exists because {@link Method} is mutable. We want to avoid situations where a method activates two
     * different sidecars. And both sidecars access the Method instance. And one of them may call
     * {@link Method#setAccessible(boolean)} which could then allow the other sidecar to unintentional have access to an
     * accessible method.
     */
    private static class Shared {

        /** A direct method handle to the method (lazily computed). */
        @Nullable
        private MethodHandle directMethodHandle;

        /** Keeps track of whether or not we have exposed {@link #methodUnsafe} to an end-user. */
        private boolean isMethodUsed;

        /** The method we are processing. */
        private final Method methodUnsafe;

        /** The source. */
        private final HookedClassModel.Builder source;

        private Shared(HookedClassModel.Builder source, Method method) {
            this.source = requireNonNull(source);
            this.methodUnsafe = requireNonNull(method);
        }

        MethodHandle direct() {
            if (directMethodHandle == null) {
                directMethodHandle = source.oc.unreflect(methodUnsafe, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            }
            return directMethodHandle;
        }
    }
}
