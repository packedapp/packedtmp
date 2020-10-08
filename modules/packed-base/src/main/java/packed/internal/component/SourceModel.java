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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.base.Key;
import packed.internal.classscan.OpenClass;
import packed.internal.inject.Dependant;
import packed.internal.sidecar.SidecarContextDependencyProvider;

/**
 * A model of a source, a cached instance of this class is acquired via {@link RealmModel#modelOf(Class)}.
 */
public final class SourceModel {

    /** The simple name of the component type (razy), typically used for lazy generating a component name. */
    /// Should we have a little of cache simpleName0, simpleName1, ...
    private String simpleName;

    /** All methods with a sidecar. */
    private final List<SourceModelMethod> methods;

    /** All fields with a sidecar. */
    private final List<SourceModelField> fields;

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
    String defaultPrefix() {
        String s = simpleName;
        if (s == null) {
            s = simpleName = type.getSimpleName();
        }
        return s;
    }

    public <T> void register(ComponentNodeConfiguration compConf) {
        SourceBuild source = compConf.source;

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

        final RealmModel realm;

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
        private Builder(RealmModel realm, OpenClass cp) {
            this.realm = requireNonNull(realm);
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
            cp.findMethodsAndFields(method -> SourceModelMethod.process(this, method), field -> SourceModelField.process(this, field));
            return new SourceModel(this);
        }
    }
}
