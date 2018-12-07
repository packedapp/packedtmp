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
import java.util.Objects;

import app.packed.container.ComponentConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import app.packed.inject.TypeLiteral;
import app.packed.util.Nullable;
import packed.internal.inject.InjectSupport;
import packed.internal.inject.builder.ServiceBuildNodeDefault;
import packed.internal.inject.builder.InjectorBuilder;
import packed.internal.invokers.InternalFunction;
import packed.internal.util.Checks;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** The default implementation of {@link ComponentConfiguration}. */
public class InternalComponentConfiguration<T> extends ServiceBuildNodeDefault<T> implements ComponentConfiguration<T> {

    /** A list of all children that have been added (lazily initialized). */
    ArrayList<InternalComponentConfiguration<?>> children;

    /** A map of all children that have been added whose name has been explicitly set (lazily initialized). */
    HashMap<String, InternalComponentConfiguration<?>> childrenExplicitNamed;

    /** The internal component, after it has been initialized. */
    InternalComponent component;

    /**
     * The thread that was used to create this configuration, is needed, because some operations are only allowed from the
     * installing thread.
     */
    final Thread initializationThread;

    ArrayList<MixinBuildNode> mixins;

    @Nullable
    String name;

    /** The parent of this configuration, or null for the root component. */
    final @Nullable InternalComponentConfiguration<?> parent;

    // /**
    // * @param containerBuilder
    // * @param configurationSite
    // * @param factory
    // * @param bindingMode
    // */
    // public InternalComponentConfiguration(ContainerBuilder containerBuilder, InternalConfigurationSite configurationSite,
    // @Nullable InternalComponentConfiguration<?> parent, InternalFactory<T> factory) {
    // super(containerBuilder, configurationSite, BindingMode.SINGLETON, factory.function, factory.dependencies);
    // this.parent = parent;
    // this.initializationThread = Thread.currentThread();
    // }

    /**
     * @param containerBuilder
     * @param configurationSite
     * @param instance
     */
    public InternalComponentConfiguration(ContainerBuilder containerBuilder, InternalConfigurationSite configurationSite,
            @Nullable InternalComponentConfiguration<?> parent, T instance) {
        super(containerBuilder, configurationSite, instance);
        this.parent = parent;
        this.initializationThread = Thread.currentThread();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration<T> addMixin(Class<?> implementation) {
        return addMixin(Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration<T> addMixin(Factory<?> factory) {
        checkConfigurable();
        InternalFunction<?> f = InjectSupport.toInternalFunction(factory);
        return addMixin0(new MixinBuildNode(injectorBuilder, configurationSite, injectorBuilder.accessor.readable(f)));
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration<T> addMixin(Object instance) {
        checkConfigurable();
        return addMixin0(new MixinBuildNode(injectorBuilder, configurationSite, instance));
    }

    private ComponentConfiguration<T> addMixin0(MixinBuildNode node) {
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
    public InternalComponentConfiguration<?> asNone() {
        super.asNone();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public <S> ComponentConfiguration<S> install(Class<S> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public <S> ComponentConfiguration<S> install(Factory<S> factory) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <S> ComponentConfiguration<S> install(S instance) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public <S> ComponentConfiguration<S> install(TypeLiteral<S> implementation) {
        return install(Factory.findInjectable(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public InjectorConfiguration privates() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public InternalComponentConfiguration<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration<T> setName(String name) {
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
    static class MixinBuildNode extends ServiceBuildNodeDefault<Object> {

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
        public MixinBuildNode(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, Object instance) {
            super(injectorConfiguration, configurationSite, instance);
        }
    }
}
