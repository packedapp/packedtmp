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
import packed.internal.inject.dependency.DependencyProvider;
import packed.internal.inject.dependency.Injectable;
import packed.internal.sidecar.FieldSidecarModel;
import packed.internal.sidecar.MethodSidecarModel;
import packed.internal.sidecar.SidecarDependencyProvider;
import packed.internal.sidecar.model.Model;
import packed.internal.util.ThrowableUtil;

/**
 * A model of a container, a cached instance of this class is acquired via {@link RealmModel#modelOf(Class)}.
 */
public final class SourceModel extends Model {

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    /// Should we have a little of cache simpleName0, simpleName1, ...
    private String simpleName;

    public final List<SourceModelSidecarMethod> methods;

    public final List<SourceModelSidecarField> fields;

    public final Map<Key<?>, SidecarDependencyProvider> sourceServices;

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

        // There should probably be some order we call extensions in....
        /// Other first, packed lasts?
        /// Think they need an order id....
        // Boer vaere lowest dependency id first...
        // Preferable deterministic

//        this.extensionHooks = builder.extensionBuilders.entrySet().stream().map(e -> {
//            HookRequest r;
//            try {
//                r = e.getValue().build();
//            } catch (Throwable ee) {
//                throw ThrowableUtil.orUndeclared(ee);
//            }
//            return new ExtensionRequestPair(e.getKey(), r);
//        }).toArray(i -> new ExtensionRequestPair[i]);
        this.sourceServices = Map.copyOf(builder.globalServices);
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

        // Iterate through all "interesting" methods on the source.
        for (SourceModelSidecarMethod smm : methods) {
            DependencyProvider[] dp = smm.createProviders();
            if (smm.isInstanceMethod()) {
                dp[0] = source;
            }
            Injectable i = new Injectable(source, smm, dp);
            compConf.injectionManager().addInjectable(i);

        }
        for (SourceModelSidecarField smm : fields) {
            DependencyProvider[] dp = smm.createProviders();
            if (smm.isInstanceMethod()) {
                dp[0] = source;
            }
            Injectable i = new Injectable(source, smm, dp);
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

        private final OpenClass cp;

        final RealmModel csm;

        /** A map of builders for every activated extension. */

        final ArrayList<SourceModelSidecarMethod> methods = new ArrayList<>();

        final ArrayList<SourceModelSidecarField> fields = new ArrayList<>();

        final Map<Key<?>, SidecarDependencyProvider> globalServices = new HashMap<>();

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

            try (MemberUnreflector htp = new MemberUnreflector(cp, UncheckedThrowableFactory.INTERNAL_EXTENSION_EXCEPTION_FACTORY)) {

                // findAssinableTo(htp, componentType);
                // findAnnotatedTypes(htp, componentType);
                // Inherited annotations???
                cp.findMethodsAndFields(method -> findAnnotatedMethods(htp, method), field -> findAnnotatedFields(htp, field));
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
            return new SourceModel(this);
        }

        private void findAnnotatedFields(MemberUnreflector htp, Field field) throws Throwable {
            VarHandle varHandle = null;

            for (Annotation a : field.getAnnotations()) {

                FieldSidecarModel model = FieldSidecarModel.getModelForAnnotatedMethod(a.annotationType());
                if (model != null) {

                    // We can have more than 1 sidecar attached to a method
                    if (varHandle == null) {
                        varHandle = htp.unreflectVarhandle(field);
                    }

                    SourceModelSidecarField smm = new SourceModelSidecarField(field, model, varHandle);
                    smm.bootstrap(this);

                }
            }
        }

        private void findAnnotatedMethods(MemberUnreflector htp, Method method) throws Throwable {
            MethodHandle directMethodHandle = null;

            for (Annotation a : method.getAnnotations()) {

                MethodSidecarModel model = MethodSidecarModel.getModelForAnnotatedMethod(a.annotationType());
                if (model != null) {

                    // We can have more than 1 sidecar attached to a method
                    if (directMethodHandle == null) {
                        directMethodHandle = htp.unreflect(method);
                    }

                    SourceModelSidecarMethod smm = new SourceModelSidecarMethod(method, model, directMethodHandle);
                    smm.bootstrap(this);

                }
            }
        }

    }
}
