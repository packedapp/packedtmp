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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import app.packed.bean.hooks.OldBeanConstructor;
import app.packed.bean.hooks.OldBeanClass;
import app.packed.bean.hooks.OldBeanField;
import app.packed.bean.hooks.OldBeanMethod;
import packed.internal.bean.hooks.ClassHookModel;
import packed.internal.bean.hooks.FieldHookModel;
import packed.internal.bean.hooks.MethodHookBootstrapModel;
import packed.internal.devtools.spi.PackedDevTools;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/** A model of class hook */
public final class UseSiteClassHookModel {

    /** A handle for invoking {@link OldBeanMethod#bootstrap()}. */
    private static final MethodHandle MH_CLASS_HOOK_BOOTSTRAP_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), OldBeanClass.class,
            "bootstrap", void.class);

    /** A handle for accessing {@link OldBeanMethod#processor}. */
    private static final VarHandle VH_CLASS_HOOK_BOOTSTRAP_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), OldBeanClass.class, "builder",
            UseSiteClassHookModel.Builder.class);

    /** A builder object for a class hook */
    public static final class Builder extends AbstractBootstrapBuilder {

        /** The instance of bootstrap. */
        final OldBeanClass instance;

        final LinkedHashSet<UseSiteMemberHookModel.Builder> managedMembers = new LinkedHashSet<>();

        /** The model of the bootstrap class. */
        public final ClassHookModel model;

        public Builder(HookModel.Builder source, ClassHookModel model) {
            super(source);
            this.model = model;
            this.instance = (OldBeanClass) model.newInstance();
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

        public List<OldBeanConstructor> constructors() {
            throw new UnsupportedOperationException();
        }

        public List<OldBeanField> fields(boolean declaredFieldsOnly, Class<?>... skipClasses) {
            ArrayList<OldBeanField> list = new ArrayList<>();
            Class<?> c = source.type();
            Field[] fields = c.getDeclaredFields();
            PackedDevTools.INSTANCE.reflectMembers(c, fields);

            
            for (Field f : fields) {
                UseSiteFieldHookModel.Builder b = new UseSiteFieldHookModel.Builder(this, ExposedFieldBootstrap.MODEL, f);
                list.add((OldBeanField) b.initialize());
            }
            return List.copyOf(list);
        }

        public List<OldBeanMethod> methods(boolean declaredFieldsOnly, Class<?>... skipClasses) {
            ArrayList<OldBeanMethod> list = new ArrayList<>();
            Class<?> c = source.type();
            Method[] methods = c.getDeclaredMethods();
            PackedDevTools.INSTANCE.reflectMembers(c, methods);

            for (Method m : methods) {
                // TODO I think we need to do some filtering on bridge and maybe synthetic methods
                UseSiteMethodHookModel.Builder b = new UseSiteMethodHookModel.Builder(source, ExposedMethodBootstrap.MODEL, m);
                list.add((OldBeanMethod) b.initialize());
            }
            return List.copyOf(list);
        }

        public Class<?> type() {
            throw new UnsupportedOperationException();
        }

        static class ExposedFieldBootstrap extends OldBeanField {
            static final FieldHookModel MODEL = FieldHookModel.getModelForFake(ExposedFieldBootstrap.class);
        }

        static class ExposedMethodBootstrap extends OldBeanMethod {
            static final MethodHookBootstrapModel MODEL = MethodHookBootstrapModel.getModelForFake(ExposedMethodBootstrap.class);
        }
    }
}
