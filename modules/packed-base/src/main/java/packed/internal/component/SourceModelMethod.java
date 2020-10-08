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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.sidecar.MethodSidecar;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.inject.DependencyDescriptor;
import packed.internal.inject.DependencyProvider;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarContextDependencyProvider;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// run on initialize
// run on start
// run on stop

// En per annotering

// Altsaa alle source metoder skal jo resolves paa assembly time

public class SourceModelMethod extends SourceModelMember {

    /** A MethodHandle that can invoke MethodSidecar#configure. */
    private static final MethodHandle MH_METHOD_SIDECAR_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), MethodSidecar.class, "bootstrap",
            void.class, MethodSidecar.BootstrapContext.class);

    /** A direct method handle to the method. */
    public final MethodHandle directMethodHandle;

    public final Method method;

    public final MethodSidecarModel model;

    private SourceModelMethod(Builder builder) {
        super(builder, DependencyDescriptor.fromExecutable(builder.shared.methodUnsafe));
        this.method = requireNonNull(builder.shared.methodUnsafe);
        this.model = requireNonNull(builder.model);
        this.directMethodHandle = requireNonNull(builder.shared.direct());
    }

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
                try {
                    MH_METHOD_SIDECAR_BOOTSTRAP.invoke(model.instance(), builder);
                } catch (Throwable e) {
                    throw ThrowableUtil.orUndeclared(e);
                }
                if (!builder.disable) {
                    SourceModelMethod smm = new SourceModelMethod(builder);
                    shared.source.methods.add(smm);
                }
            }
        }
    }

    /** A builder. */
    private static final class Builder extends SourceModelMember.Builder implements MethodSidecar.BootstrapContext {

        /** The method, if exposed to end-users. */
        @Nullable
        private Method exposedMethod;

        private final Method unsafeMethod;

        /** The sidecar model. */
        private final MethodSidecarModel model;

        /** The shared context. */
        private final Shared shared;

        Builder(MethodSidecarModel model, Shared shared) {
            this.shared = requireNonNull(shared);
            this.model = requireNonNull(model);
            this.unsafeMethod = shared.methodUnsafe;
        }

        /** {@inheritDoc} */
        @Override
        public Method method() {
            Method m = exposedMethod;
            if (m == null) {
                // We want to avoid sharing method instances between multiple sidecar methods
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

        /** {@inheritDoc} */
        @Override
        public void registerAsService(boolean isConstant) {
            provideAsConstant = isConstant;
            provideAsKey = Key.fromMethodReturnType(unsafeMethod);
        }

        /** {@inheritDoc} */
        @Override
        public void registerAsService(boolean isConstant, Key<?> key) {
            provideAsConstant = isConstant;
            // Check assignable.
            provideAsKey = key;
        }

        /** {@inheritDoc} */
        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return unsafeMethod.getAnnotation(annotationClass);
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

        private MethodHandle directMethodHandle;

        /** Whether or not {@link #methodUnsafe} has been exposed to users. */
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
