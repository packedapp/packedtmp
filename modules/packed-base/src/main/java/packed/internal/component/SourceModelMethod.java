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
import app.packed.introspection.MethodDescriptor;
import app.packed.sidecar.MethodSidecar;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.methodhandle.LookupUtil;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarContextDependencyProvider;
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

    SourceModelMethod(Shared wrapper, MethodSidecarModel model) {
        this.method = requireNonNull(wrapper.methodUnsafe);
        this.model = requireNonNull(model);
        MethodDescriptor m = MethodDescriptor.from(method);
        this.dependencies = DependencyDescriptor.fromExecutable(m);
        this.directMethodHandle = requireNonNull(wrapper.direct());
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

    static void process(SourceModel.Builder builder, Method method) {
        Shared wrapper = null;
        for (Annotation a : method.getAnnotations()) {
            MethodSidecarModel model = MethodSidecarModel.getModelForAnnotatedMethod(a.annotationType());
            if (model != null) {
                if (wrapper == null) {
                    wrapper = new Shared(builder, method);
                }
                process0(wrapper, model);
            }
        }
    }

    private static void process0(Shared wrapper, MethodSidecarModel model) {
        SourceModelMethod smm = new SourceModelMethod(wrapper, model);

        Builder builder = new Builder(wrapper);
        try {
            MH_METHOD_SIDECAR_BOOTSTRAP.invoke(model.instance(), builder);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        //
        if (builder.disable) {
            return;
        }

        smm.provideAsConstant = builder.provideAsConstant;
        smm.provideAskey = builder.provideAsKey;
        wrapper.source.methods.add(smm);
    }

    public static final class Builder extends SourceModelMember.Builder implements MethodSidecar.BootstrapContext {

        public boolean disable;

        private Method method;

        private final Shared shared;

        Builder(Shared shared) {
            this.shared = requireNonNull(shared);
        }

        /** {@inheritDoc} */
        @Override
        public void disable() {
            this.disable = true;
        }

        /** {@inheritDoc} */
        @Override
        public Method method() {
            Method m = method;
            if (m == null) {
                // We want to avoid sharing method instances between multiple sidecar methods
                Method unsafe = shared.methodUnsafe;
                if (!shared.mustSafetyClone) {
                    shared.mustSafetyClone = true;
                    m = method = unsafe;
                } else {
                    try {
                        m = method = unsafe.getDeclaringClass().getDeclaredMethod(unsafe.getName(), unsafe.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            return m;
        }

        @Override
        public void registerAsService(boolean isConstant) {
            registerAsService(isConstant, Key.fromMethodReturnType(shared.methodUnsafe));
        }

        @Override
        public void registerAsService(boolean isConstant, Key<?> key) {
            provideAsConstant = isConstant;
            provideAsKey = key;
        }
    }

    public enum RunAt {
        INITIALIZATION;
    }

    /**
     * This class mainly exists because {@link Method} is mutable. We want to avoid situations where a method activates two
     * different sidecars. And both sidecars tries to access the Method instance. And one of them may call
     * {@link Method#setAccessible(boolean)} which could then allow the other sidecar to unintentional have access.
     */
    private static class Shared {

        /** The source. */
        private final SourceModel.Builder source;

        MethodHandle directMethodHandle;
        private final Method methodUnsafe;
        private boolean mustSafetyClone;

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
