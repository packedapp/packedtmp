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

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Factory;
import packed.internal.hooks.usesite.HookedClassModel;
import packed.internal.hooks.usesite.UseSiteFieldHookModel;
import packed.internal.hooks.usesite.UseSiteMemberHookModel;
import packed.internal.hooks.usesite.UseSiteMethodHookModel;
import packed.internal.inject.dependency.DependancyConsumer;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProducer;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.invoke.constantpool.ConstantPool;
import packed.internal.invoke.constantpool.PoolWriteable;
import packed.internal.util.MethodHandleUtil;

/** A configuration object for a component class source. */
public final class ClassSourceSetup implements DependencyProducer, PoolWriteable {

    /** An injectable, if this source needs to be created at runtime (not a constant). */
    @Nullable
    private final DependancyConsumer dependant;

    /**
     * Factory that was specified. We only keep this around to find the key that it should be exposed as a service with. As
     * we need to lazy calculate it from {@link #provide(ComponentSetup)}
     */
    @Nullable
    private final Factory<?> factory;

    /** If the source represents a constant. */
    @Nullable
    private final Object constant;

    /** The source model. */
    public final HookedClassModel model;

    /** The index at which to store the runtime instance, or -1 if it should not be stored. */
    public final int poolIndex;

    /** A service object if the source is provided as a service. */
    @Nullable
    public ServiceSetup service;

    public final ComponentSetup component;

    /**
     * Creates a new setup.
     * 
     * @param component
     *            the component
     * @param driver
     *            the component driver
     */
    ClassSourceSetup(ComponentSetup component, SourcedComponentDriver<?> driver) {
        this.component = requireNonNull(component);

        // Reserve a place in the constant pool if the source is a singleton
        this.poolIndex = component.modifiers().isSingleton() ? component.pool.reserve() : -1;

        RealmAccessor realm = component.realm.accessor();

        // The source is either a Class, a Factory, or a generic instance
        Object source = driver.binding;
        if (source instanceof Class<?> cl) {
            this.constant = null;
            this.factory = component.modifiers().isStaticClassSource() ? null : Factory.of(cl);
            this.model = realm.modelOf(cl);
        } else if (source instanceof Factory<?> fac) {
            this.constant = null;
            this.factory = fac;
            this.model = realm.modelOf(factory.rawType());
        } else {
            this.constant = source;
            this.factory = null;
            this.model = realm.modelOf(source.getClass());
            component.pool.addConstant(this); // non-constants singlestons are added to the constant pool elsewhere
        }

        // if (driver.modifiers().isSingleton())
        if (factory == null) {
            this.dependant = null;
        } else {
            MethodHandle mh = realm.toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.variables();
            this.dependant = new DependancyConsumer(this, dependencies, mh);
            component.container.addDependant(dependant);
        }

        // Register hooks, maybe move to component setup
        registerHooks(model, component);
    }

    private <T> void registerHooks(HookedClassModel model, ComponentSetup component) {
        for (UseSiteFieldHookModel f : model.fields) {
            registerMember(component, f);
        }

        for (UseSiteMethodHookModel m : model.methods) {
            registerMember(component, m);
        }
    }

    private void registerMember(ComponentSetup component, UseSiteMemberHookModel m) {
        DependancyConsumer i = new DependancyConsumer(component, this, m, m.createProviders());
        component.container.addDependant(i);
        if (m.processor != null) {
            m.processor.accept(component);
        }
    }

    public void writeConstantPool(ConstantPool pool) {
        assert poolIndex >= 0;
        assert constant != null;
        pool.store(poolIndex, constant);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DependancyConsumer dependant() {
        return dependant;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        if (constant != null) {
            return MethodHandleUtil.insertFakeParameter(MethodHandleUtil.constant(constant), ConstantPool.class); // MethodHandle()T ->
                                                                                                                  // MethodHandle(ConstantPool)T
        } else if (poolIndex > -1) {
            return ConstantPool.readConstant(poolIndex, model.type);
        } else {
            return dependant.buildMethodHandle();
        }
    }

    public ServiceSetup provide(SourcedComponentSetup component) {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        ServiceSetup s = service;
        if (s == null) {
            Key<?> key;
            if (constant != null) {
                key = Key.of(model.type); // Move to model?? What if instance has Qualifier???
            } else {
                key = factory.key();
            }
            s = service = component.container.getServiceManagerOrCreate().provideSource(component, key);
        }
        return s;
    }
}
