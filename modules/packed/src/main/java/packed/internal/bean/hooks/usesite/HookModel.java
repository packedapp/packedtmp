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

import java.lang.annotation.Annotation;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.hooks.OldBeanClass;
import app.packed.bean.hooks.OldBeanMethod;
import app.packed.extension.Extension;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.hooks.ClassHookModel;
import packed.internal.bean.hooks.FieldHookModel;
import packed.internal.bean.hooks.HookedMethodProvide;
import packed.internal.bean.hooks.MethodHookBootstrapModel;
import packed.internal.bean.hooks.usesite.UseSiteMethodHookModel.Shared;
import packed.internal.container.ExtensionModel;
import packed.internal.util.MemberScanner;
import packed.internal.util.OpenClass;

/** A model of a class that uses hooks. */
public final class HookModel {

    /** The class of the model. */
    public final Class<?> clazz;

    /** Any extension this model is a part of. */
    @Nullable
    public final Class<? extends Extension<?>> extensionClass;

    /** All hooks. */
    private final List<UseSiteMemberHookModel> models;

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    private String simpleName;

    // Noget med injection, som vi gerne vil metamodellere
    // Tror det er hooks som provider en keyed service paa klasse niveau
    public final Map<Key<?>, HookedMethodProvide> sourceServices;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private HookModel(HookModel.Builder builder) {
        this.clazz = builder.oc.type();
        this.models = List.copyOf(builder.models);
        this.sourceServices = Map.copyOf(builder.sourceContexts);
        this.extensionClass = builder.extension == null ? null : builder.extension.type();
    }

    public Key<?> defaultKey() {
        // What if instance has Qualifier???
        return Key.of(clazz);
    }

    public void onWire(BeanSetup bean) {
        for (UseSiteMemberHookModel hook : models) {
            hook.onWire(bean);
        }
    }

    /**
     * Returns the default prefix for the source.
     * 
     * @return the default prefix for the source
     */
    public String simpleName() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = clazz.getSimpleName();
        }
        return s;
    }

    /** A builder object for {@link HookModel}. */
    public static abstract class Builder extends MemberScanner {

        final Map<Class<? extends OldBeanClass>, UseSiteClassHookModel.Builder> classes = new HashMap<>();

        /** Any extension this lovely fellow is a part of. */
        @Nullable
        final ExtensionModel extension;

        /** All field hooks. */
        final ArrayList<UseSiteMemberHookModel> models = new ArrayList<>();

        final OpenClass oc;

        final Map<Key<?>, HookedMethodProvide> sourceContexts = new HashMap<>();

        /**
         * Creates a new component model builder
         * 
         * @param cp
         *            a class processor usable by hooks
         * 
         */
        public Builder(OpenClass cp, @Nullable ExtensionModel extension) {
            super(cp.type());
            this.oc = requireNonNull(cp);
            this.extension = extension;
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        public HookModel build() {

            // TODO run through annotations

            // findAssinableTo(htp, componentType);
            // findAnnotatedTypes(htp, componentType);
            // Inherited annotations???

            scan(true, Object.class);

            // Finish all classes
            for (UseSiteClassHookModel.Builder b : classes.values()) {
                b.complete();
            }
            return new HookModel(this);
        }

        protected abstract @Nullable FieldHookModel getFieldModel(Class<? extends Annotation> annotationType);

        protected abstract @Nullable MethodHookBootstrapModel getMethodModel(Class<? extends Annotation> annotationType);

        UseSiteClassHookModel.Builder manageMemberBy(UseSiteMemberHookModel.Builder member, Class<? extends OldBeanClass> classBootStrap) {
            return classes.computeIfAbsent(classBootStrap, c -> new UseSiteClassHookModel.Builder(this, ClassHookModel.ofManaged(classBootStrap)));
        }

        @Override
        protected void onField(Field field) {
            VarHandle varHandle = null;

            // Run through every annotation on the field, and see if we any hook that are activated
            for (Annotation a : field.getAnnotations()) {
                FieldHookModel hook = getFieldModel(a.annotationType());

                if (hook != null) {
                    // We can have more than 1 sidecar attached to a method
                    if (varHandle == null) {
                        varHandle = oc.unreflectVarHandle(field);
                    }

                    UseSiteFieldHookModel.Builder builder = new UseSiteFieldHookModel.Builder(this, hook, field);
                    builder.invokeBootstrap();
                    if (builder.buildtimeModel != null) {
                        UseSiteFieldHookModel smm = new UseSiteFieldHookModel(builder, varHandle);
                        models.add(smm);
                    }
                }
            }
        }

        @Override
        protected void onMethod(Method method) {
            UseSiteMethodHookModel.Shared shared = null;
            for (Annotation a : method.getAnnotations()) {
                MethodHookBootstrapModel model = getMethodModel(a.annotationType());
                if (model != null) {
                    if (shared == null) {
                        shared = new Shared(this, method);
                    }
                    UseSiteMethodHookModel.Builder builder = new UseSiteMethodHookModel.Builder(model, shared);

                    OldBeanMethod bootstrap = model.bootstrap(builder);
                    if (builder.managedBy == null) {
                        model.clearBuilder(bootstrap);
                    }

                    if (builder.buildtimeModel != null) {
                        UseSiteMethodHookModel smm = new UseSiteMethodHookModel(builder);
                        models.add(smm);
                    }
                }
            }
            // I guess we might need to run through and look for ServiceHooks?
        }

        Class<?> type() {
            return classToScan;
        }
    }
}

// Ideen er lidt at vi ikke laver metoder for mange gange...
// Vi kan maaske endda cache dem... Saa hvis vi har en shared
// abstract class saa kan vi genbruge den..
// Ved ikke lige med mht til MethodHandle???
// Et array, med entries for already resolved MethodHandle
//class ScannedClass {
//    @Nullable
//    Constructor<?>[] constructors; // nah..
//    @Nullable
//    Field[] declaredFields;
//    @Nullable
//    Method[] declaredMethods;
//    @Nullable
//    ScannedClass parent;
//    final Class<?> type;
//
//    ScannedClass(Class<?> type) {
//        this.type = requireNonNull(type);
//    }
//
//}