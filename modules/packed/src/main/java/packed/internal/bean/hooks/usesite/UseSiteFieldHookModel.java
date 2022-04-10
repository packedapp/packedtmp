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
package packed.internal.bean.hooks.usesite;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import app.packed.base.Key;
import app.packed.bean.hooks.OldBeanField;
import app.packed.bean.hooks.BeanFieldHook;
import packed.internal.bean.hooks.FieldHookModel;
import packed.internal.bean.hooks.HookedMethodProvide;
import packed.internal.inject.factory.bean.DependencyProducer;
import packed.internal.inject.factory.bean.InternalDependency;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;
import packed.internal.util.ThrowableUtil;

/** Represents the use site of a field hook. */
public final class UseSiteFieldHookModel extends UseSiteMemberHookModel {

    /** A MethodHandle that can invoke {@link OldBeanField#bootstrap}. */
    private static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OldBeanField.class, "bootstrap",
            void.class);

    /** A VarHandle that can access {@link OldBeanField#processor}. */
    private static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), OldBeanField.class, "builder",
            UseSiteFieldHookModel.Builder.class);

    /** A MethodHandle that can invoke {@link OldBeanField#bootstrap}. */
    private static final MethodHandle MH_FIELD_HOOK_BUILDER = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OldBeanField.class, "builder",
            UseSiteFieldHookModel.Builder.class);
    
    /** A direct method handle to the field. */
    public final VarHandle varHandle;

    /** The modifiers of the field. */
    private final int modifiers;

    /** A model of the field hooks bootstrap. */
    private final FieldHookModel hook;

    public static UseSiteFieldHookModel.Builder getBuilder(OldBeanField field) {
        try {
            return (UseSiteFieldHookModel.Builder) MH_FIELD_HOOK_BUILDER.invokeExact(field);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }
    UseSiteFieldHookModel(Builder builder, VarHandle mh) {
        super(builder, List.of());
        this.modifiers = requireNonNull(builder.field.getModifiers());
        this.hook = requireNonNull(builder.hook);
        this.varHandle = requireNonNull(mh);
    }

    @Override
    public DependencyProducer[] createProviders() {
        DependencyProducer[] providers = new DependencyProducer[Modifier.isStatic(modifiers) ? 0 : 1];
        // System.out.println("RESOLVING " + directMethodHandle);
        for (int i = 0; i < dependencies.size(); i++) {
            InternalDependency d = dependencies.get(i);
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

    /**
     * A builder for {@link UseSiteFieldHookModel}. Instances of this class are avilable via {@link BeanFieldHook#processor()}.
     */
    public static final class Builder extends UseSiteMemberHookModel.Builder {

        /** The field that activate the hook. */
        final Field field;

        final FieldHookModel hook;

        public Builder(HookModel.Builder source, FieldHookModel hook, Field field) {
            super(source, hook);
            this.hook = hook;
            this.field = field;
        }

        public Builder(UseSiteClassHookModel.Builder classBuilder, FieldHookModel model, Field field) {
            super(classBuilder.source, model);
            this.hook = model;
            this.field = field;
            this.managedBy = classBuilder;
        }

        public void checkWritable() {}

        void invokeBootstrap() {
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
        
        public VarHandle varHandle() {
            throw new UnsupportedOperationException();
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

    /**
     * This class mainly exists because {@link Field} is mutable. We want to avoid situations where a method activates two
     * different sidecars. And both sidecars access the Method instance. And one of them may call
     * {@link Method#setAccessible(boolean)} which could then allow the other sidecar to unintentional have access to an
     * accessible method.
     */
    @SuppressWarnings("unused")
    public static class Shared {

        /** The method we are processing. */
        private final Field fieldUnsafe;

        /** Whether or not {@link #fieldUnsafe} has been exposed to users. */
        private boolean isFieldUsed;

        /** The source. */
        private final HookModel.Builder builder;

        private VarHandle varHandle;

        public Shared(HookModel.Builder source, Field field) {
            this.builder = requireNonNull(source);
            this.fieldUnsafe = requireNonNull(field);
        }

        VarHandle direct() {
            if (varHandle == null) {
                varHandle = builder.oc.unreflectVarHandle(fieldUnsafe);
            }
            return varHandle;
        }
    }
}
