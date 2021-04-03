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
import packed.internal.hooks.HookedMethodProvide;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProducer;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;
import packed.internal.util.ThrowableUtil;

/** Represents the use site of a field hook. */
public final class UseSiteFieldHookModel extends UseSiteMemberHookModel {

    /** A MethodHandle that can invoke {@link FieldHook.Bootstrap#bootstrap}. */
    private static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), FieldHook.Bootstrap.class, "bootstrap",
            void.class);

    /** A VarHandle that can access {@link FieldHook.Bootstrap#builder}. */
    private static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), FieldHook.Bootstrap.class, "builder",
            UseSiteFieldHookModel.Builder.class);

    /** A direct method handle to the field. */
    public final VarHandle varHandle;

    /** The modifiers of the field. */
    private final int modifiers;

    /** A model of the field hooks bootstrap. */
    private final FieldHookModel hook;

    final HookUseSite hus;

    @Nullable
    public RunAt runAt = RunAt.INITIALIZATION;

    UseSiteFieldHookModel(Builder builder, VarHandle mh) {
        super(builder, List.of());
        this.hus = requireNonNull(builder.hus);
        this.modifiers = requireNonNull(builder.field.getModifiers());
        this.hook = requireNonNull(builder.hook);
        // FieldDescriptor m = FieldDescriptor.from(method);
        // this.dependencies = Arrays.asList(DependencyDescriptor.fromField(m));
        this.varHandle = requireNonNull(mh);
    }

    @Override
    public DependencyProducer[] createProviders() {
        DependencyProducer[] providers = new DependencyProducer[Modifier.isStatic(modifiers) ? 0 : 1];
        // System.out.println("RESOLVING " + directMethodHandle);
        for (int i = 0; i < dependencies.size(); i++) {
            DependencyDescriptor d = dependencies.get(i);
            HookedMethodProvide dp = hook.keys.get(d.key());
            if (dp != null) {
                // System.out.println("MAtches for " + d.key());
                int index = i + (Modifier.isStatic(modifiers) ? 0 : 1);
                providers[index] = dp;
                // System.out.println("SEtting provider " + dp.dependencyAccessor());
            }
        }

        return providers;
    }

    /** {@inheritDoc} */
    @Override
    public int getModifiers() {
        return modifiers;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle methodHandle() {
        return MethodHandleUtil.getFromField(modifiers, varHandle);
    }

    static void processField(HookedClassModel.Builder source, Field field) {
        VarHandle varHandle = null;

        // Run through every annotation on the field, and see if we any hook that are activated
        for (Annotation a : field.getAnnotations()) {
            FieldHookModel hook = FieldHookModel.of(source.hus, null, a.annotationType());

            if (hook != null) {
                // We can have more than 1 sidecar attached to a method
                if (varHandle == null) {
                    varHandle = source.oc.unreflectVarHandle(field, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                }

                Builder builder = new Builder(source, hook, field);
                builder.invokeBootstrap();
                if (builder.buildtimeModel != null) {
                    UseSiteFieldHookModel smm = new UseSiteFieldHookModel(builder, varHandle);
                    source.fields.add(smm);
                }
            }
        }
    }

    /**
     * A builder for {@link UseSiteFieldHookModel}. Instances of this class are avilable via {@link FieldHook#bootstrap()}.
     */
    public static final class Builder extends UseSiteMemberHookModel.Builder {

        /** The field that activate the hook. */
        final Field field;

        final HookUseSite hus;

        final FieldHookModel hook;

        Builder(HookedClassModel.Builder source, FieldHookModel hook, Field field) {
            super(source, hook);
            this.hus = source.hus;
            this.hook = hook;
            this.field = field;
        }

        Builder(UseSiteClassHookModel.Builder classBuilder, FieldHookModel model, Field field) {
            super(classBuilder.source, model);
            this.hus = null;
            this.hook = model;
            this.field = field;
            this.managedBy = classBuilder;
        }

        public void checkWritable() {}

        private void invokeBootstrap() {
            // We perform a compare and exchange with configuration. Guarding against
            // concurrent usage of this assembly.
            // Don't think it makes sense to register
            Object instance = hook.newInstance();

            VH_FIELD_HOOK_BUILDER.set(instance, this);
            try {
                MH_FIELD_HOOK_BOOTSTRAP.invoke(instance); // Invokes FieldHook.Bootstrap#bootstrap()
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            } finally {
                VH_FIELD_HOOK_BUILDER.set(instance, null); // clears this builder
            }
        }

        public Field field() {
            return field;
        }

        Object initialize() {
            Object instance = hook.newInstance();
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
        private final HookedClassModel.Builder builder;

        private VarHandle varHandle;

        private Shared(HookedClassModel.Builder source, Field field) {
            this.builder = requireNonNull(source);
            this.fieldUnsafe = requireNonNull(field);
        }

        VarHandle direct() {
            if (varHandle == null) {
                varHandle = builder.oc.unreflectVarHandle(fieldUnsafe, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
            }
            return varHandle;
        }
    }
}
