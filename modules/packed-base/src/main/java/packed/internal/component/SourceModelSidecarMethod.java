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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

import app.packed.base.Key;
import app.packed.introspection.MethodDescriptor;
import app.packed.sidecar.MethodSidecar;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.methodhandle.LookupUtil;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarDependencyProvider;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// run on initialize
// run on start
// run on stop

// En per annotering

// Altsaa alle source metoder skal jo resolves paa assembly time

public class SourceModelSidecarMethod extends SourceModelSidecarMember {

    /** A MethodHandle that can invoke MethodSidecar#configure. */
    private static final MethodHandle MH_METHOD_SIDECAR_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), MethodSidecar.class, "bootstrap",
            void.class, MethodSidecar.BootstrapContext.class);

    /** A direct method handle to the method. */
    public final MethodHandle directMethodHandle;

    public final Method method;

    public final MethodSidecarModel model;

    SourceModelSidecarMethod(Method method, MethodSidecarModel model, MethodHandle mh) {
        this.method = requireNonNull(method);
        this.model = requireNonNull(model);
        MethodDescriptor m = MethodDescriptor.from(method);
        this.dependencies = DependencyDescriptor.fromExecutable(m);
        this.directMethodHandle = requireNonNull(mh);
    }

    public boolean isInstanceMethod() {
        return !Modifier.isStatic(method.getModifiers());
    }

    public enum RunAt {
        INITIALIZATION;
    }

    public DependencyProvider[] createProviders() {
        DependencyProvider[] providers = new DependencyProvider[directMethodHandle.type().parameterCount()];
        // System.out.println("RESOLVING " + directMethodHandle);
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyDescriptor d = dependencies.get(i);
            SidecarDependencyProvider dp = model.keys.get(d.key());
            if (dp != null) {
                // System.out.println("MAtches for " + d.key());
                int index = i + directMethodHandle.type().parameterCount() == dependencies.size() ? 0 : 1;
                providers[index] = dp;
                // System.out.println("SEtting provider " + dp.dependencyAccessor());
            }
        }

        return providers;
    }

    /**
     * 
     */
    public void bootstrap(SourceModel.Builder b) {
        MethodSidecarBootstrapContext c = new MethodSidecarBootstrapContext();
        try {
            MH_METHOD_SIDECAR_BOOTSTRAP.invoke(model.instance(), c);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        if (c.disable) {
            return;
        }
        this.provideAsConstant = c.provideAsConstant;
        this.provideAskey = c.provideAsKey;

        b.methods.add(this);
        Map<Key<?>, SidecarDependencyProvider> keys = model.keys;
        if (keys != null) {
            b.globalServices.putAll(keys);
        }
    }

    public final class MethodSidecarBootstrapContext implements MethodSidecar.BootstrapContext {

        public boolean disable;

        @Override
        public void provideAsService(boolean isConstant) {
            provideAsService(isConstant, Key.fromMethodReturnType(method));
        }

        @Override
        public void provideAsService(boolean isConstant, Key<?> key) {
            provideAsConstant = isConstant;
            provideAsKey = key;
        }

        public Key<?> provideAsKey;

        public boolean provideAsConstant;

        /** {@inheritDoc} */
        @Override
        public void disable() {
            this.disable = true;
        }

        /** {@inheritDoc} */
        @Override
        public Method method() {
            return method;
        }
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
}
