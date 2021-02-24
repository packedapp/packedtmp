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
import packed.internal.hooks.ClassHookBootstrapModel;
import packed.internal.hooks.FieldHookBootstrapModel;
import packed.internal.hooks.MethodHookBootstrapModel;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class ClassHookModel {

    /** A MethodHandle that can invoke {@link MethodHook.Bootstrap#bootstrap}. */
    private static final MethodHandle MH_EXTENSION_METHOD_BOOTSTRAP_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(),
            ClassHook.Bootstrap.class, "bootstrap", void.class);

    /** A VarHandle that can access {@link MethodHook.Bootstrap#builder}. */
    private static final VarHandle VH_EXTENSION_METHOD_BOOTSTRAP_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(),
            ClassHook.Bootstrap.class, "builder", ClassHookModel.Builder.class);

    public static final class Builder extends AbstractBootstrapBuilder {

        final ClassHook.Bootstrap instance;

        public final ClassHookBootstrapModel bootstrapModel;

        final LinkedHashSet<MemberHookModel.Builder> managedMembers = new LinkedHashSet<>();

        Builder(ClassSourceModel.Builder source, ClassHookBootstrapModel model) {
            super(source);
            this.bootstrapModel = model;
            this.instance = (Bootstrap) bootstrapModel.newInstance();
        }

        void complete() {
            VH_EXTENSION_METHOD_BOOTSTRAP_BUILDER.set(instance, this);
            try {
                MH_EXTENSION_METHOD_BOOTSTRAP_BOOTSTRAP.invoke(instance);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
            for (MemberHookModel.Builder b : managedMembers) {
                b.complete();
            }
            VH_EXTENSION_METHOD_BOOTSTRAP_BUILDER.set(instance, null);
        }

        public List<FieldHook.Bootstrap> fields(boolean declaredFieldsOnly, Class<?>... skipClasses) {
            ArrayList<FieldHook.Bootstrap> list = new ArrayList<>();
            for (Field f : source.type().getDeclaredFields()) {
                FieldHookModel.Builder b = new FieldHookModel.Builder(this, ExposedFieldBootstrap.MODEL, f);
                list.add((FieldHook.Bootstrap) b.initialize());
            }
            return List.copyOf(list);
        }

        public List<MethodHook.Bootstrap> methods(boolean declaredFieldsOnly, Class<?>... skipClasses) {
            ArrayList<MethodHook.Bootstrap> list = new ArrayList<>();
            for (Method m : source.type().getDeclaredMethods()) {
                MethodHookModel.Builder b = new MethodHookModel.Builder(source, ExposedMethodBootstrap.MODEL, m);
                list.add((MethodHook.Bootstrap) b.initialize());
            }
            return List.copyOf(list);
        }

        static class ExposedMethodBootstrap extends MethodHook.Bootstrap {
            static final MethodHookBootstrapModel MODEL = MethodHookBootstrapModel.getModelForFake(ExposedMethodBootstrap.class);
        }

        static class ExposedFieldBootstrap extends FieldHook.Bootstrap {
            static final FieldHookBootstrapModel MODEL = FieldHookBootstrapModel.getModelForFake(ExposedFieldBootstrap.class);
        }
    }
}
