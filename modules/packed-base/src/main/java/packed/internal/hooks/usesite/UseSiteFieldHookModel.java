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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.hooks.FieldHook;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hooks.FieldHookModel;
import packed.internal.hooks.ContextMethodProvide;
import packed.internal.inject.DependencyDescriptor;
import packed.internal.inject.DependencyProvider;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class UseSiteFieldHookModel extends UseSiteMemberHookModel {

    /** A MethodHandle that can invoke {@link FieldHook.Bootstrap#bootstrap}. */
    private static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), FieldHook.Bootstrap.class,
            "bootstrap", void.class);

    /** A VarHandle that can access {@link FieldHook.Bootstrap#builder}. */
    private static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), FieldHook.Bootstrap.class,
            "builder", UseSiteFieldHookModel.Builder.class);

    /** A direct method handle to the field. */
    public final VarHandle directMethodHandle;

    /** The field this model models. */
    private final Field field;

    /** A model of the field hooks bootstrap. */
    private final FieldHookModel model;

    @Nullable
    public RunAt runAt = RunAt.INITIALIZATION;

    UseSiteFieldHookModel(Builder builder, Field method, FieldHookModel model, VarHandle mh) {
        super(builder, List.of());
        this.field = requireNonNull(method);
        this.model = requireNonNull(model);
        // FieldDescriptor m = FieldDescriptor.from(method);
        // this.dependencies = Arrays.asList(DependencyDescriptor.fromField(m));
        this.directMethodHandle = requireNonNull(mh);
    }

    @Override
    public DependencyProvider[] createProviders() {
        DependencyProvider[] providers = new DependencyProvider[Modifier.isStatic(field.getModifiers()) ? 0 : 1];
        // System.out.println("RESOLVING " + directMethodHandle);
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyDescriptor d = dependencies.get(i);
            ContextMethodProvide dp = model.keys.get(d.key());
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

    static void process(HookedClassModel.Builder source, Field field) {
        VarHandle varHandle = null;
        for (Annotation a : field.getAnnotations()) {
            FieldHookModel model = FieldHookModel.getModelForAnnotatedMethod(a.annotationType());
            if (model != null) {
                // We can have more than 1 sidecar attached to a method
                if (varHandle == null) {
                    varHandle = source.oc.unreflectVarHandle(field, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                }
                Builder builder = new Builder(source, model, field);
                builder.configure();
                if (builder.buildtimeModel != null) {
                    UseSiteFieldHookModel smm = new UseSiteFieldHookModel(builder, field, model, varHandle);
                    source.fields.add(smm);
                }
            }
        }
    }

    public static final class Builder extends UseSiteMemberHookModel.Builder {
        final Field field;

        final FieldHookModel model;

        Builder(HookedClassModel.Builder source, FieldHookModel model, Field field) {
            super(source, model);
            this.model = model;
            this.field = field;
        }

        Builder(UseSiteClassHookModel.Builder classBuilder, FieldHookModel model, Field field) {
            super(classBuilder.source, model);
            this.model = model;
            this.field = field;
            this.managedBy = classBuilder;
        }

        public void checkWritable() {}

        private void configure() {
            // We perform a compare and exchange with configuration. Guarding against
            // concurrent usage of this assembly.
            // Don't think it makes sense to register
            Object instance = model.newInstance();

            VH_FIELD_HOOK_BUILDER.set(instance, this);
            try {
                MH_FIELD_HOOK_BOOTSTRAP.invoke(instance); // Invokes sidecar#configure()
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                VH_FIELD_HOOK_BUILDER.set(instance, null); // clears the configuration
            }
        }

        public Field field() {
            return field;
        }

        Object initialize() {
            Object instance = model.newInstance();
            VH_FIELD_HOOK_BUILDER.set(instance, this);
            try {
                MH_FIELD_HOOK_BOOTSTRAP.invoke(instance); // Invokes sidecar#configure()
                return instance;
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        public void provideAsService(boolean isConstant) {
            provideAsService(isConstant, Key.convertField(field()));
        }

        public void provideAsService(boolean isConstant, Key<?> key) {
            provideAsConstant = isConstant;
            provideAsKey = key;
        }

        public void set(Object argument) {}
    }

    public enum RunAt {
        INITIALIZATION;
    }

    /**
     * This class mainly exists because {@link Field} is mutable. We want to avoid situations where a method activates two
     * different sidecars. And both sidecars access the Method instance. And one of them may call
     * {@link Method#setAccessible(boolean)} which could then allow the other sidecar to unintentional have access to an
     * accessible method.
     */
    @SuppressWarnings("unused")
    private static class Shared {

        /** The method we are processing. */
        private final Field fieldUnsafe;

        /** Whether or not {@link #fieldUnsafe} has been exposed to users. */
        private boolean isFieldUsed;

        /** The source. */
        private final HookedClassModel.Builder source;

        private VarHandle varHandle;

        private Shared(HookedClassModel.Builder source, Field field) {
            this.source = requireNonNull(source);
            this.fieldUnsafe = requireNonNull(field);
        }

        VarHandle direct() {
            if (varHandle == null) {
                varHandle = source.oc.unreflectVarHandle(fieldUnsafe, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            }
            return varHandle;
        }
    }
}
