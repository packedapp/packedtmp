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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.base.Key;
import packed.internal.classscan.invoke.OpenClass;
import packed.internal.container.RealmModel;
import packed.internal.errorhandling.UncheckedThrowableFactory;
import packed.internal.inject.dependency.Dependant;
import packed.internal.sidecar.FieldSidecarModel;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarContextDependencyProvider;
import packed.internal.sidecar.model.Model;

/**
 * A model of a source, a cached instance of this class is acquired via {@link RealmModel#modelOf(Class)}.
 */
public final class SourceModel extends Model {

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    /// Should we have a little of cache simpleName0, simpleName1, ...
    private String simpleName;

    /** All methods with a sidecar. */
    private final List<SourceModelMethod> methods;

    /** All fields with a sidecar. */
    private final List<SourceModelField> fields;

    public final Map<Key<?>, SidecarContextDependencyProvider> sourceServices;

    /**
     * Creates a new descriptor.
     * 
     * @param builder
     *            a builder for this descriptor
     */
    private SourceModel(SourceModel.Builder builder) {
        super(builder.cp.type());
        this.methods = List.copyOf(builder.methods);
        this.fields = List.copyOf(builder.fields);
        this.sourceServices = Map.copyOf(builder.sourceContexts);
    }

    /**
     * Returns the default prefix for the component, if no name is explicitly set by the user.
     * 
     * @return the default prefix for the component, if no name is explicitly set by the user
     */
    String defaultPrefix() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = modelType().getSimpleName();
        }
        return s;
    }

    public <T> void register(ComponentNodeConfiguration compConf) {
        SourceAssembly source = compConf.source;

        for (SourceModelField f : fields) {
            Dependant i = new Dependant(source, f, f.createProviders());
            compConf.injectionManager().addInjectable(i);
        }

        for (SourceModelMethod m : methods) {
            Dependant i = new Dependant(source, m, m.createProviders());
            compConf.injectionManager().addInjectable(i);

        }
    }

    /**
     * Creates a new component model instance.
     * 
     * @param csm
     *            a model of the container source that is trying to install the component
     * @param cp
     *            a class processor usable by hooks
     * @return a model of the component
     */
    public static SourceModel newInstance(RealmModel csm, OpenClass cp) {
        return new Builder(csm, cp).build();
    }

    /** A builder object for a component model. */
    static final class Builder {

        final OpenClass cp;

        final RealmModel csm;

        final ArrayList<SourceModelMethod> methods = new ArrayList<>();

        final ArrayList<SourceModelField> fields = new ArrayList<>();

        final Map<Key<?>, SidecarContextDependencyProvider> sourceContexts = new HashMap<>();

        /**
         * Creates a new component model builder
         * 
         * @param cp
         *            a class processor usable by hooks
         * 
         */
        private Builder(RealmModel csm, OpenClass cp) {
            this.csm = requireNonNull(csm);
            this.cp = requireNonNull(cp);
        }

        /**
         * Builds and returns a new model.
         * 
         * @return a new model
         */
        private SourceModel build() {
            // findAssinableTo(htp, componentType);
            // findAnnotatedTypes(htp, componentType);
            // Inherited annotations???
            cp.findMethodsAndFields(method -> SourceModelMethod.findAnnotatedMethods(this, method), field -> findAnnotatedFields(field));
            return new SourceModel(this);
        }

        private void findAnnotatedFields(Field field) {
            VarHandle varHandle = null;
            for (Annotation a : field.getAnnotations()) {
                FieldSidecarModel model = FieldSidecarModel.getModelForAnnotatedMethod(a.annotationType());
                if (model != null) {
                    // We can have more than 1 sidecar attached to a method
                    if (varHandle == null) {
                        varHandle = cp.unreflectVarhandle(field, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    }
                    SourceModelField smm = new SourceModelField(field, model, varHandle);
                    smm.bootstrap(this);
                }
            }
        }

        private void findAnnotatedMethods(Method method) {
            MethodHandle directMethodHandle = null;
            for (Annotation a : method.getAnnotations()) {
                MethodSidecarModel model = MethodSidecarModel.getModelForAnnotatedMethod(a.annotationType());
                if (model != null) {
                    // We can have more than 1 sidecar attached to a method
                    if (directMethodHandle == null) {
                        directMethodHandle = cp.unreflect(method, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY);
                    }

                    SourceModelMethod smm = SourceModelMethod.bootstrap(method, model, directMethodHandle);
                    if (smm != null) {
                        methods.add(smm);
                    }
                }
            }
        }
    }
}
