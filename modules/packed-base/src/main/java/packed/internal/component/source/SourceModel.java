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
import app.packed.container.Extension;
import app.packed.container.ClassHook;
import packed.internal.bundle.ExtensionModel;
import packed.internal.bundle.extension.ClassHookBootstrapModel;
import packed.internal.bundle.extension.SidecarContextDependencyProvider;
import packed.internal.classscan.OpenClass;
import packed.internal.component.ComponentBuild;
import packed.internal.inject.Dependant;

/**
 * A model of a source, a cached instance of this class is acquired via {@link RealmModel#modelOf(Class)}.
 */
public final class SourceModel {

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    /// Should we have a little of cache simpleName0, simpleName1, ...
    private String simpleName;

    /** All methods with a sidecar. */
    private final List<MethodHookModel> methods;

    /** All fields with a sidecar. */
    private final List<FieldHookModel> fields;

    public final Map<Key<?>, SidecarContextDependencyProvider> sourceServices;

    public final Class<?> type;

    /**
     * Creates a new descriptor.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private SourceModel(SourceModel.Builder builder) {
        this.type = builder.cp.type();
        this.methods = List.copyOf(builder.methods);
        this.fields = List.copyOf(builder.fields);
        this.sourceServices = Map.copyOf(builder.sourceContexts);
    }

    /**
     * Returns the default prefix for the component, if no name is explicitly set by the user.
     * 
     * @return the default prefix for the component, if no name is explicitly set by the user
     */
    public String defaultPrefix() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = type.getSimpleName();
        }
        return s;
    }


    public <T> void register(ComponentBuild compConf, SourceBuild source) {
        for (FieldHookModel f : fields) {
            registerMember(compConf, source, f);
        }

        for (MethodHookModel m : methods) {
            registerMember(compConf, source, m);
        }
    }

    private void registerMember(ComponentBuild compConf, SourceBuild source, MemberHookModel m) {
        requireNonNull(source);
        Dependant i = new Dependant(compConf, source, m, m.createProviders());
//        if (i.hasUnresolved()) {
        compConf.memberOfCube.addDependant(i);
        // }
        if (m.processor != null) {
            m.processor.accept(compConf);
        }
    }

    /**
     * Creates a new component model instance.
     * 
     * @param realm
     *            a model of the container source that is trying to install the component
     * @param cp
     *            a class processor usable by hooks
     * @return a model of the component
     */
    public static SourceModel newInstance(RealmModel realm, OpenClass cp) {
        return new Builder(realm, cp).build();
    }

    /** A builder object for a single class. */
    static final class Builder {

        final OpenClass cp;

        final RealmModel realm;

        final ArrayList<MethodHookModel> methods = new ArrayList<>();

        final ArrayList<FieldHookModel> fields = new ArrayList<>();

        final Map<Class<? extends ClassHook.Bootstrap>, ClassHookModel.Builder> classes = new HashMap<>();

        final Map<Key<?>, SidecarContextDependencyProvider> sourceContexts = new HashMap<>();

        /** */
        @Nullable
        final Class<? extends Extension> extensionType;


        public Class<?> type() {
            return cp.type();
        }
        
        /**
         * Creates a new component model builder
         * 
         * @param cp
         *            a class processor usable by hooks
         * 
         */
        private Builder(RealmModel realm, OpenClass cp) {
            this.realm = requireNonNull(realm);
            this.cp = requireNonNull(cp);
            this.extensionType = ExtensionModel.getExtensionMemberOf(cp.type());
        }

        ClassHookModel.Builder manageMemberBy(MemberHookModel.Builder member, Class<? extends ClassHook.Bootstrap> classBootStrap) {
            return classes.computeIfAbsent(classBootStrap, c -> new ClassHookModel.Builder(this, ClassHookBootstrapModel.ofManaged(classBootStrap)));
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        private SourceModel build() {
            for (Annotation a : cp.type().getAnnotations()) {
                System.out.println(a);
            }

            // TODO run through annotations

            // findAssinableTo(htp, componentType);
            // findAnnotatedTypes(htp, componentType);
            // Inherited annotations???

            cp.findMethodsAndFields(method -> MethodHookModel.process(this, method), field -> FieldHookModel.process(this, field));

            // Finish all classes
            for (ClassHookModel.Builder b : classes.values()) {
                b.complete();
            }
            return new SourceModel(this);
        }
    }

    // Ideen er lidt at vi ikke laver metoder for mange gange...
    // Vi kan maaske endda cache dem... Saa hvis vi har en shared
    // abstract class saa kan vi genbruge den..
    // Ved ikke lige med mht til MethodHandle???
    // Et array, med entries for already resolved MethodHandle

    //
    class ScannedClass {
        final Class<?> type;
        @Nullable
        ScannedClass parent;
        @Nullable
        Method[] declaredMethods;
        @Nullable
        Field[] declaredFields;
        @Nullable
        Constructor<?>[] constructors; // nah..

        ScannedClass(Class<?> type) {
            this.type = requireNonNull(type);
        }

    }
}
