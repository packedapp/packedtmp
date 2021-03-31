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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.hooks.ClassHook;
import packed.internal.component.ComponentSetup;
import packed.internal.hooks.BootstrapClassClassHookModel;
import packed.internal.hooks.ContextMethodProvide;
import packed.internal.hooks.usesite.UseSiteClassHookModel;
import packed.internal.hooks.usesite.UseSiteFieldHookModel;
import packed.internal.hooks.usesite.UseSiteMemberHookModel;
import packed.internal.hooks.usesite.UseSiteMethodHookModel;
import packed.internal.inject.Dependant;
import packed.internal.invoke.ClassMemberAccessor;
import packed.internal.invoke.ClassScanner;

/** A model of a class source. */
public final class ClassSourceModel {

    /** All field hooks. */
    private final List<UseSiteFieldHookModel> fields;

    /** All method hooks. */
    private final List<UseSiteMethodHookModel> methods;

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    private String simpleName;

    // Noget med injection
    public final Map<Key<?>, ContextMethodProvide> sourceServices;

    /** The type this is a model for. */
    public final Class<?> type;

    /**
     * Creates a new model.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private ClassSourceModel(ClassSourceModel.Builder builder) {
        this.type = builder.oc.type();
        this.methods = List.copyOf(builder.methods);
        this.fields = List.copyOf(builder.fields);
        this.sourceServices = Map.copyOf(builder.sourceContexts);
    }

    public <T> void registerHooks(ComponentSetup component, ClassSourceSetup source) {
        for (UseSiteFieldHookModel f : fields) {
            registerMember(component, source, f);
        }

        for (UseSiteMethodHookModel m : methods) {
            registerMember(component, source, m);
        }
    }

    private void registerMember(ComponentSetup compConf, ClassSourceSetup source, UseSiteMemberHookModel m) {
        requireNonNull(source);
        Dependant i = new Dependant(compConf, source, m, m.createProviders());
//        if (i.hasUnresolved()) {
        compConf.container.addDependant(i);
        // }
        if (m.processor != null) {
            m.processor.accept(compConf);
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
            s = simpleName = type.getSimpleName();
        }
        return s;
    }

    /**
     * Creates a new component model instance.
     * 
     * @param realm
     *            a model of the container source that is trying to install the component
     * @param oc
     *            a class processor usable by hooks
     * @return a model of the component
     */
    public static ClassSourceModel newModel(ClassMemberAccessor oc) {
        return new Builder(oc).build();
    }

    /** A builder object for {@link ClassSourceModel}. */
    public static final class Builder extends ClassScanner {

        public final Map<Class<? extends ClassHook.Bootstrap>, UseSiteClassHookModel.Builder> classes = new HashMap<>();

        /** All field hooks. */
        public final ArrayList<UseSiteFieldHookModel> fields = new ArrayList<>();

        /** All method hooks. */
        public final ArrayList<UseSiteMethodHookModel> methods = new ArrayList<>();

        public final ClassMemberAccessor oc;

        public final Map<Key<?>, ContextMethodProvide> sourceContexts = new HashMap<>();

        /**
         * Creates a new component model builder
         * 
         * @param cp
         *            a class processor usable by hooks
         * 
         */
        Builder(ClassMemberAccessor cp) {
            super(cp.type());
            this.oc = requireNonNull(cp);
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        ClassSourceModel build() {
            for (Annotation a : oc.type().getAnnotations()) {
                System.out.println(a);
            }

            // TODO run through annotations

            // findAssinableTo(htp, componentType);
            // findAnnotatedTypes(htp, componentType);
            // Inherited annotations???

            scan(true, Object.class);

            // Finish all classes
            for (UseSiteClassHookModel.Builder b : classes.values()) {
                b.complete();
            }
            return new ClassSourceModel(this);
        }

        UseSiteClassHookModel.Builder manageMemberBy(UseSiteMemberHookModel.Builder member, Class<? extends ClassHook.Bootstrap> classBootStrap) {
            return classes.computeIfAbsent(classBootStrap,
                    c -> new UseSiteClassHookModel.Builder(this, BootstrapClassClassHookModel.ofManaged(classBootStrap)));
        }

        @Override
        protected void onField(Field field) {
            UseSiteFieldHookModel.process(this, field);
        }

        @Override
        protected void onMethod(Method method) {
            UseSiteMethodHookModel.process(this, method);
        }

        public Class<?> type() {
            return oc.type();
        }
    }

    // Ideen er lidt at vi ikke laver metoder for mange gange...
    // Vi kan maaske endda cache dem... Saa hvis vi har en shared
    // abstract class saa kan vi genbruge den..
    // Ved ikke lige med mht til MethodHandle???
    // Et array, med entries for already resolved MethodHandle

    class ScannedClass {
        @Nullable
        Constructor<?>[] constructors; // nah..
        @Nullable
        Field[] declaredFields;
        @Nullable
        Method[] declaredMethods;
        @Nullable
        ScannedClass parent;
        final Class<?> type;

        ScannedClass(Class<?> type) {
            this.type = requireNonNull(type);
        }

    }
}
