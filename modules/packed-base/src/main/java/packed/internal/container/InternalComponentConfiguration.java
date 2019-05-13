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
package packed.internal.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import app.packed.container.ComponentServiceConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.InstantiationMode;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.classscan.ComponentClassDescriptor;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.AppPackedInjectSupport;
import packed.internal.inject.InternalDependencyDescriptor;
import packed.internal.inject.buildtime.BuildtimeServiceNodeDefault;
import packed.internal.inject.buildtime.ContainerBuilder;
import packed.internal.invokable.InternalFunction;
import packed.internal.util.Checks;

/** The default implementation of {@link ComponentServiceConfiguration}. */
public class InternalComponentConfiguration<T> extends BuildtimeServiceNodeDefault<T> implements ComponentServiceConfiguration<T> {

    /** A list of all children that have been added (lazily initialized). */
    public ArrayList<InternalComponentConfiguration<?>> children;

    /** A map of all children that have been added whose name has been explicitly set (lazily initialized). */
    public HashMap<String, InternalComponentConfiguration<?>> childrenExplicitNamed;

    /** The internal component, after it has been initialized. */
    InternalComponent component;

    /**
     * The thread that was used to create this configuration. This is needed, because some operations are only allowed from
     * the installing thread.
     */
    final Thread initializationThread;

    /** A list of all mixins that have been added (lazily initialized). */
    public ArrayList<MixinNode> mixins;

    @Nullable
    public String name;

    /** The parent of this configuration, or null for the root component. */
    @Nullable
    final InternalComponentConfiguration<?> parent;

    /** The object instances of the component, the array will be passed along to InternalComponent. */
    public Object[] instances;

    public InternalComponentConfiguration(ContainerBuilder containerBuilder, InternalConfigurationSite configurationSite, ComponentClassDescriptor descriptor,
            @Nullable InternalComponentConfiguration<?> parent, InternalFunction<T> function, List<InternalDependencyDescriptor> dependencies) {
        super(containerBuilder, configurationSite, descriptor, InstantiationMode.SINGLETON, function, dependencies);
        this.parent = parent;
        this.initializationThread = Thread.currentThread();
    }

    /**
     * @param containerBuilder
     * @param configurationSite
     * @param instance
     */
    public InternalComponentConfiguration(ContainerBuilder containerBuilder, InternalConfigurationSite configurationSite, ComponentClassDescriptor descriptor,
            @Nullable InternalComponentConfiguration<?> parent, T instance) {
        super(containerBuilder, configurationSite, descriptor, instance);
        this.parent = parent;
        this.initializationThread = Thread.currentThread();
    }

    /**
     * Invokes the specified action for this configuration as well any child configuration.
     *
     * @param action
     *            the action to invoke
     */
    public void forEachRecursively(Consumer<? super InternalComponentConfiguration<?>> action) {
        action.accept(this);
        if (children != null) {
            for (InternalComponentConfiguration<?> child : children) {
                child.forEachRecursively(action);
            }
        }
    }

    @Override
    public ComponentClassDescriptor descriptor() {
        return (ComponentClassDescriptor) super.descriptor();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> addMixin(Class<?> implementation) {
        return addMixin(Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> addMixin(Factory<?> factory) {
        checkConfigurable();
        InternalFunction<?> f = AppPackedInjectSupport.toInternalFunction(factory);
        return addMixin0(new MixinNode(injectorBuilder, configurationSite, injectorBuilder.accessor.readable(f)));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> addMixin(Object instance) {
        checkConfigurable();
        return addMixin0(new MixinNode(injectorBuilder, configurationSite, instance));
    }

    private ComponentServiceConfiguration<T> addMixin0(MixinNode node) {
        if (mixins == null) {
            mixins = new ArrayList<>(1);
        }
        mixins.add(node);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public InternalComponentConfiguration<T> as(Class<? super T> key) {
        super.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public InternalComponentConfiguration<T> as(Key<? super T> key) {
        super.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public InternalComponentConfiguration<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    public boolean isRuntimeComponent() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> setName(String name) {
        checkConfigurable();
        if (!Objects.equals(name, this.name)) {
            if (parent != null) {
                if (name == null) { // we allow clearing of the name if automatically set, for example, by an annotation
                    parent.childrenExplicitNamed.remove(this.name);
                } else {
                    Checks.checkLetterNumberUnderscoreDotOrHyphen(name);
                    if (parent.childrenExplicitNamed == null) {
                        parent.childrenExplicitNamed = new HashMap<>(4);
                    } else if (parent.childrenExplicitNamed.containsKey(name)) {
                        throw new IllegalArgumentException("An existing component with the specified name already exist, name = " + name);
                    }
                    if (this.name != null) {
                        parent.childrenExplicitNamed.remove(this.name);
                    }
                    parent.childrenExplicitNamed.put(name, this);
                }
            }
            this.name = name;
        }
        return this;
    }

    /** A special build node that is used for mixins. */
    static class MixinNode extends BuildtimeServiceNodeDefault<Object> {

        // /**
        // * @param injectorBuilder
        // * @param configurationSite
        // * @param factory
        // */
        // public MixinBuildNode(InjectorBuilder injectorBuilder, InternalConfigurationSite configurationSite,
        // InternalFactory<Object> factory) {
        // super(injectorBuilder, configurationSite, BindingMode.SINGLETON, factory.function, factory.dependencies);
        // }

        /**
         * @param injectorConfiguration
         * @param configurationSite
         * @param instance
         */
        public MixinNode(ContainerBuilder injectorConfiguration, InternalConfigurationSite configurationSite, Object instance) {
            // Null should probably be service class descriptor... or its own...
            super(injectorConfiguration, configurationSite, null, instance);
        }
    }

    void init(InternalContainer container) {
        if (parent == null) {
            component = new InternalComponent(container, this, null, false, getName());
        } else {
            component = new InternalComponent(container, this, parent.component, false, getName());
            parent.component.children.put(component.name(), component);
        }
    }

}
