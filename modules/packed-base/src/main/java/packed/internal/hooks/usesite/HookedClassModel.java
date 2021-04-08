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
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.hooks.ClassHook;
import app.packed.hooks.MethodHook;
import packed.internal.container.ExtensionModel;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.hooks.ClassHookModel;
import packed.internal.hooks.FieldHookModel;
import packed.internal.hooks.HookedMethodProvide;
import packed.internal.hooks.MethodHookBootstrapModel;
import packed.internal.hooks.usesite.UseSiteMethodHookModel.Shared;
import packed.internal.invoke.MemberScanner;
import packed.internal.invoke.OpenClass;

/** A model of a class that uses class or member hooks. */
public final class HookedClassModel {

    /** All field hooks. */
    public final List<UseSiteFieldHookModel> fields;

    /** All method hooks. */
    public final List<UseSiteMethodHookModel> methods;

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    private String simpleName;

    // Noget med injection, som vi gerne vil metamodellere
    // Tror det er hooks som provider en keyed service paa klasse niveau
    public final Map<Key<?>, HookedMethodProvide> sourceServices;

    /** The class we have modeled. */
    public final Class<?> clazz;

    /** Any extension this model is a part of. */
    @Nullable
    public final Class<? extends Extension> extensionClass;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private HookedClassModel(HookedClassModel.Builder builder) {
        this.clazz = builder.oc.type();
        this.methods = List.copyOf(builder.methods);
        this.fields = List.copyOf(builder.fields);
        this.sourceServices = Map.copyOf(builder.sourceContexts);
        this.extensionClass = builder.extension == null ? null : builder.extension.extensionClass();
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

    /** A builder object for {@link HookedClassModel}. */
    public static abstract class Builder extends MemberScanner {

        final Map<Class<? extends ClassHook.Bootstrap>, UseSiteClassHookModel.Builder> classes = new HashMap<>();

        /** All field hooks. */
        final ArrayList<UseSiteFieldHookModel> fields = new ArrayList<>();

        /** All method hooks. */
        final ArrayList<UseSiteMethodHookModel> methods = new ArrayList<>();

        final OpenClass oc;

        final Map<Key<?>, HookedMethodProvide> sourceContexts = new HashMap<>();

        // In order to use @Provide, FooExtension must have ServiceExtension as a dependency.
        @Nullable
        final ExtensionModel extension;

        final HookUseSite hus;

        /**
         * Creates a new component model builder
         * 
         * @param cp
         *            a class processor usable by hooks
         * 
         */
        public Builder(HookUseSite hus, OpenClass cp, @Nullable ExtensionModel extension) {
            super(cp.type());
            this.hus = requireNonNull(hus);
            this.oc = requireNonNull(cp);
            this.extension = extension;
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        public HookedClassModel build() {

            // TODO run through annotations

            // findAssinableTo(htp, componentType);
            // findAnnotatedTypes(htp, componentType);
            // Inherited annotations???

            scan(true, Object.class);

            // Finish all classes
            for (UseSiteClassHookModel.Builder b : classes.values()) {
                b.complete();
            }
            return new HookedClassModel(this);
        }

        UseSiteClassHookModel.Builder manageMemberBy(UseSiteMemberHookModel.Builder member, Class<? extends ClassHook.Bootstrap> classBootStrap) {
            return classes.computeIfAbsent(classBootStrap, c -> new UseSiteClassHookModel.Builder(this, ClassHookModel.ofManaged(classBootStrap)));
        }

        @Override
        protected void onField(Field field) {
            VarHandle varHandle = null;

            // Run through every annotation on the field, and see if we any hook that are activated
            for (Annotation a : field.getAnnotations()) {
                FieldHookModel hook = FieldHookModel.of(hus, null, a.annotationType());

                if (hook != null) {
                    // We can have more than 1 sidecar attached to a method
                    if (varHandle == null) {
                        varHandle = oc.unreflectVarHandle(field, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    }

                    UseSiteFieldHookModel.Builder builder = new UseSiteFieldHookModel.Builder(this, hook, field);
                    builder.invokeBootstrap();
                    if (builder.buildtimeModel != null) {
                        UseSiteFieldHookModel smm = new UseSiteFieldHookModel(builder, varHandle);
                        fields.add(smm);
                    }
                }
            }
        }

        protected abstract @Nullable MethodHookBootstrapModel getMethodModel(Class<? extends Annotation> annotationType);

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

                    MethodHook.Bootstrap bootstrap = model.bootstrap(builder);
                    if (builder.managedBy == null) {
                        model.clearBuilder(bootstrap);
                    }

                    if (builder.buildtimeModel != null) {
                        UseSiteMethodHookModel smm = new UseSiteMethodHookModel(builder);
                        shared.source.methods.add(smm);
                    }
                }
            }
            // I guess we might need to run through and look for ServiceHooks?
        }

        public Class<?> type() {
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