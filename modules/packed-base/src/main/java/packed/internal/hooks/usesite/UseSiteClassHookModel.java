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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import app.packed.hooks.ClassHook;
import app.packed.hooks.ClassHook.Bootstrap;
import app.packed.hooks.FieldHook;
import app.packed.hooks.MethodHook;
import packed.internal.component.ClassSourceModel;
import packed.internal.hooks.BootstrapClassClassHookModel;
import packed.internal.hooks.BootstrapClassFieldHookModel;
import packed.internal.hooks.BootstrapClassMethodHookModel;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** A model of class hook */
public final class UseSiteClassHookModel {

    /** A handle for invoking {@link MethodHook.Bootstrap#bootstrap()}. */
    private static final MethodHandle MH_CLASS_HOOK_BOOTSTRAP_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ClassHook.Bootstrap.class,
            "bootstrap", void.class);

    /** A handle for accessing {@link MethodHook.Bootstrap#builder}. */
    private static final VarHandle VH_CLASS_HOOK_BOOTSTRAP_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ClassHook.Bootstrap.class,
            "builder", UseSiteClassHookModel.Builder.class);

    /** A builder object for a class hook */
    public static final class Builder extends AbstractBootstrapBuilder {

        /** The instance of bootstrap. */
        final ClassHook.Bootstrap instance;

        final LinkedHashSet<UseSiteMemberHookModel.Builder> managedMembers = new LinkedHashSet<>();

        /** The model of the bootstrap class. */
        public final BootstrapClassClassHookModel model;

        public Builder(ClassSourceModel.Builder source, BootstrapClassClassHookModel model) {
            super(source);
            this.model = model;
            this.instance = (Bootstrap) model.newInstance();
        }

        public void complete() {
            VH_CLASS_HOOK_BOOTSTRAP_BUILDER.set(instance, this);
            
            // Invoke ClassHook.Bootstrap#bootstrap()
            try {
                MH_CLASS_HOOK_BOOTSTRAP_BOOTSTRAP.invokeExact(instance);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
            
            for (UseSiteMemberHookModel.Builder b : managedMembers) {
                b.complete();
            }
            VH_CLASS_HOOK_BOOTSTRAP_BUILDER.set(instance, null);
        }

        public List<app.packed.hooks.ConstructorHook.Bootstrap> constructors() {
            throw new UnsupportedOperationException();
        }

        public List<FieldHook.Bootstrap> fields(boolean declaredFieldsOnly, Class<?>... skipClasses) {
            ArrayList<FieldHook.Bootstrap> list = new ArrayList<>();
            for (Field f : source.type().getDeclaredFields()) {
                UseSiteFieldHookModel.Builder b = new UseSiteFieldHookModel.Builder(this, ExposedFieldBootstrap.MODEL, f);
                list.add((FieldHook.Bootstrap) b.initialize());
            }
            return List.copyOf(list);
        }

        public List<MethodHook.Bootstrap> methods(boolean declaredFieldsOnly, Class<?>... skipClasses) {
            ArrayList<MethodHook.Bootstrap> list = new ArrayList<>();
            for (Method m : source.type().getDeclaredMethods()) {
                UseSiteMethodHookModel.Builder b = new UseSiteMethodHookModel.Builder(source, ExposedMethodBootstrap.MODEL, m);
                list.add((MethodHook.Bootstrap) b.initialize());
            }
            return List.copyOf(list);
        }

        public Class<?> type() {
            throw new UnsupportedOperationException();
        }

        static class ExposedFieldBootstrap extends FieldHook.Bootstrap {
            static final BootstrapClassFieldHookModel MODEL = BootstrapClassFieldHookModel.getModelForFake(ExposedFieldBootstrap.class);
        }

        static class ExposedMethodBootstrap extends MethodHook.Bootstrap {
            static final BootstrapClassMethodHookModel MODEL = BootstrapClassMethodHookModel.getModelForFake(ExposedMethodBootstrap.class);
        }
    }
}
