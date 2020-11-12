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
package packed.internal.component.source;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bundle.Extension;
import app.packed.sidecar.MethodSidecar;
import packed.internal.component.ComponentBuild;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.inject.DependencyDescriptor;
import packed.internal.inject.DependencyProvider;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarContextDependencyProvider;
import packed.internal.util.LookupUtil;
import packed.internal.util.ReflectionUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class SourceModelMethod extends SourceModelMember {

    /** A MethodHandle that can invoke MethodSidecar#configure. */
    private static final MethodHandle MH_METHOD_SIDECAR_CONFIGURE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), MethodSidecar.class, "configure",
            void.class);

    /** A VarHandle that can access MethodSidecar#configuration. */
    private static final VarHandle VH_METHOD_SIDECAR_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), MethodSidecar.class,
            "configuration", SourceModelMethod.Builder.class);

    /** A direct method handle to the method. */
    private final MethodHandle directMethodHandle;

    private final Method method;

    public final MethodSidecarModel model;

    private SourceModelMethod(Builder builder) {
        super(builder, DependencyDescriptor.fromExecutable(builder.shared.methodUnsafe));
        this.method = requireNonNull(builder.shared.methodUnsafe);
        this.model = requireNonNull(builder.model);
        this.directMethodHandle = requireNonNull(builder.shared.direct());
    }

    @Override
    public DependencyProvider[] createProviders() {
        DependencyProvider[] providers = new DependencyProvider[directMethodHandle.type().parameterCount()];
        // System.out.println("RESOLVING " + directMethodHandle);
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyDescriptor d = dependencies.get(i);
            SidecarContextDependencyProvider dp = model.keys.get(d.key());
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

    static void process(SourceModel.Builder source, Method method) {
        Shared shared = null;
        for (Annotation a : method.getAnnotations()) {
            MethodSidecarModel model = MethodSidecarModel.getModelForAnnotatedMethod(a.annotationType());
            if (model != null) {
                if (shared == null) {
                    shared = new Shared(source, method);
                }
                Builder builder = new Builder(model, shared);

                builder.configure();

                if (!builder.disable) {
                    SourceModelMethod smm = new SourceModelMethod(builder);
                    shared.source.methods.add(smm);
                }
            }
        }
    }

    /** A builder. */
    public static final class Builder extends SourceModelMember.Builder {

        /** The method, if exposed to end-users. */
        @Nullable
        private Method exposedMethod;

        /** The sidecar model. */
        private final MethodSidecarModel model;

        /** The shared context. */
        private final Shared shared;

        private final Method unsafeMethod;

        Builder(MethodSidecarModel model, Shared shared) {
            this.shared = requireNonNull(shared);
            this.model = requireNonNull(model);
            this.unsafeMethod = shared.methodUnsafe;
        }

        private void configure() {
            // We perform a compare and exchange with configuration. Guarding against
            // concurrent usage of this bundle.
            // Don't think it makes sense to register
            Object instance = model.newSidecar();

            VH_METHOD_SIDECAR_CONFIGURATION.set(instance, this);
            try {
                MH_METHOD_SIDECAR_CONFIGURE.invoke(instance); // Invokes sidecar#configure()
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                VH_METHOD_SIDECAR_CONFIGURATION.set(instance, null); // clears the configuration
            }
        }

        public Optional<Class<? extends Extension>> extensionMember() {
            return Optional.ofNullable(shared.source.extensionType);
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return unsafeMethod.getAnnotation(annotationClass);
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
                    m = exposedMethod = ReflectionUtil.copy(unsafeMethod);
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

        public MethodHandle methodHandle() {
            return shared.direct();
        }

        public static void registerProcessor(MethodSidecar sidecar, Consumer<? super ComponentBuild> processor) {
            Builder b = (Builder) VH_METHOD_SIDECAR_CONFIGURATION.get(sidecar);
            b.processor = processor;
        }
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
        private final SourceModel.Builder source;

        private Shared(SourceModel.Builder source, Method method) {
            this.source = requireNonNull(source);
            this.methodUnsafe = requireNonNull(method);
        }

        MethodHandle direct() {
            if (directMethodHandle == null) {
                directMethodHandle = source.cp.unreflect(methodUnsafe, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            }
            return directMethodHandle;
        }
    }
}
