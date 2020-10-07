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
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.sidecar.FieldSidecar;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.methodhandle.LookupUtil;
import packed.internal.methodhandle.MethodHandleUtil;
import packed.internal.sidecar.FieldSidecarModel;
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

public class SourceModelField extends SourceModelMember {

    /** A MethodHandle that can invoke MethodSidecar#configure. */
    private static final MethodHandle MH_FIELD_SIDECAR_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), FieldSidecar.class, "bootstrap",
            void.class, FieldSidecar.BootstrapContext.class);

    /** A direct method handle to the method. */
    public final VarHandle directMethodHandle;

    public final Field field;

    public final FieldSidecarModel model;

    @Nullable
    public RunAt runAt = RunAt.INITIALIZATION;

    SourceModelField(Field method, FieldSidecarModel model, VarHandle mh) {
        this.field = requireNonNull(method);
        this.model = requireNonNull(model);
        // FieldDescriptor m = FieldDescriptor.from(method);
        // this.dependencies = Arrays.asList(DependencyDescriptor.fromField(m));
        this.dependencies = Arrays.asList();
        this.directMethodHandle = requireNonNull(mh);
    }

    /**
     * 
     */
    public void bootstrap(SourceModel.Builder b) {
        Builder c = new Builder();
        try {
            MH_FIELD_SIDECAR_BOOTSTRAP.invoke(model.instance(), c);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        if (c.disable) {
            return;
        }
        this.provideAsConstant = c.provideAsConstant;
        this.provideAskey = c.provideAsKey;

        b.fields.add(this);
        Map<Key<?>, SidecarContextDependencyProvider> keys = model.keys;
        if (keys != null) {
            b.sourceContexts.putAll(keys);
        }
    }

    public DependencyProvider[] createProviders() {
        DependencyProvider[] providers = new DependencyProvider[Modifier.isStatic(field.getModifiers()) ? 0 : 1];
        // System.out.println("RESOLVING " + directMethodHandle);
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyDescriptor d = dependencies.get(i);
            SidecarContextDependencyProvider dp = model.keys.get(d.key());
            if (dp != null) {
                // System.out.println("MAtches for " + d.key());
                int index = i + (Modifier.isStatic(field.getModifiers()) ? 0 : 1);
                providers[index] = dp;
                // System.out.println("SEtting provider " + dp.dependencyAccessor());
            }
        }

        return providers;
    }

    /** {@inheritDoc} */
    @Override
    public int getModifiers() {
        return field.getModifiers();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle methodHandle() {
        return MethodHandleUtil.getFromField(field, directMethodHandle);
    }

    private final class Builder extends SourceModelMember.Builder implements FieldSidecar.BootstrapContext {

        /** {@inheritDoc} */
        @Override
        public Field field() {
            return field;
        }

        @Override
        public void provideAsService(boolean isConstant) {
            provideAsService(isConstant, Key.fromField(field()));
        }

        @Override
        public void provideAsService(boolean isConstant, Key<?> key) {
            provideAsConstant = isConstant;
            provideAsKey = key;
        }
    }

    public enum RunAt {
        INITIALIZATION;
    }
}
