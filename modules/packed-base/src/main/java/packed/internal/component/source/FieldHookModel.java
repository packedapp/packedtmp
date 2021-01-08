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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.FieldHook;
import packed.internal.bundle.extension.FieldHookBootstrapModel;
import packed.internal.bundle.extension.SidecarContextDependencyProvider;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.inject.DependencyDescriptor;
import packed.internal.inject.DependencyProvider;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// run on initialize
// run on start
// run on stop

// En per annotering

// Altsaa alle source metoder skal jo resolves paa assembly time

public final class FieldHookModel extends MemberHookModel {

    /** A MethodHandle that can invoke MethodSidecar#configure. */
    private static final MethodHandle MH_FIELD_SIDECAR_CONFIGURE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), FieldHook.Bootstrap.class,
            "bootstrap", void.class);

    /** A VarHandle that can access MethodSidecar#configuration. */
    private static final VarHandle VH_FIELD_SIDECAR_CONFIGURATION = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), FieldHook.Bootstrap.class,
            "builder", FieldHookModel.Builder.class);

    /** A direct method handle to the method. */
    public final VarHandle directMethodHandle;

    public final Field field;

    public final FieldHookBootstrapModel model;

    @Nullable
    public RunAt runAt = RunAt.INITIALIZATION;

    FieldHookModel(Builder builder, Field method, FieldHookBootstrapModel model, VarHandle mh) {
        super(builder, List.of());
        this.field = requireNonNull(method);
        this.model = requireNonNull(model);
        // FieldDescriptor m = FieldDescriptor.from(method);
        // this.dependencies = Arrays.asList(DependencyDescriptor.fromField(m));
        this.directMethodHandle = requireNonNull(mh);
    }

    static void process(SourceModel.Builder source, Field field) {
        VarHandle varHandle = null;
        for (Annotation a : field.getAnnotations()) {
            FieldHookBootstrapModel model = FieldHookBootstrapModel.getModelForAnnotatedMethod(a.annotationType());
            if (model != null) {
                // We can have more than 1 sidecar attached to a method
                if (varHandle == null) {
                    varHandle = source.cp.unreflectVarHandle(field, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                }
                Builder builder = new Builder(source, model, field);
                builder.configure();
                if (builder.buildtimeModel != null) {
                    FieldHookModel smm = new FieldHookModel(builder, field, model, varHandle);
                    source.fields.add(smm);
                }
            }
        }
    }

    @Override
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

    public static final class Builder extends MemberHookModel.Builder {
        final Field field;

        final FieldHookBootstrapModel model;

        Builder(ClassHookModel.Builder classBuilder, FieldHookBootstrapModel model, Field field) {
            super(classBuilder.source, model);
            this.model = model;
            this.field = field;
            this.managedBy = classBuilder;
        }

        Builder(SourceModel.Builder source, FieldHookBootstrapModel model, Field field) {
            super(source, model);
            this.model = model;
            this.field = field;
        }

        Object initialize() {
            Object instance = model.newInstance();
            VH_FIELD_SIDECAR_CONFIGURATION.set(instance, this);
            try {
                MH_FIELD_SIDECAR_CONFIGURE.invoke(instance); // Invokes sidecar#configure()
                return instance;
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        private void configure() {
            // We perform a compare and exchange with configuration. Guarding against
            // concurrent usage of this bundle.
            // Don't think it makes sense to register
            Object instance = model.newInstance();

            VH_FIELD_SIDECAR_CONFIGURATION.set(instance, this);
            try {
                MH_FIELD_SIDECAR_CONFIGURE.invoke(instance); // Invokes sidecar#configure()
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                VH_FIELD_SIDECAR_CONFIGURATION.set(instance, null); // clears the configuration
            }
        }

        public Field field() {
            return field;
        }

        public void provideAsService(boolean isConstant) {
            provideAsService(isConstant, Key.convertField(field()));
        }

        public void provideAsService(boolean isConstant, Key<?> key) {
            provideAsConstant = isConstant;
            provideAsKey = key;
        }

        public void set(Object argument) {}

        public void checkWritable() {}
    }

    /**
     * This class mainly exists because {@link Method} is mutable. We want to avoid situations where a method activates two
     * different sidecars. And both sidecars access the Method instance. And one of them may call
     * {@link Method#setAccessible(boolean)} which could then allow the other sidecar to unintentional have access to an
     * accessible method.
     */
    @SuppressWarnings("unused")
    private static class Shared {

        private VarHandle varHandle;

        /** Whether or not {@link #fieldUnsafe} has been exposed to users. */
        private boolean isFieldUsed;

        /** The method we are processing. */
        private final Field fieldUnsafe;

        /** The source. */
        private final SourceModel.Builder source;

        private Shared(SourceModel.Builder source, Field field) {
            this.source = requireNonNull(source);
            this.fieldUnsafe = requireNonNull(field);
        }

        VarHandle direct() {
            if (varHandle == null) {
                varHandle = source.cp.unreflectVarHandle(fieldUnsafe, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            }
            return varHandle;
        }
    }

    public enum RunAt {
        INITIALIZATION;
    }
}
