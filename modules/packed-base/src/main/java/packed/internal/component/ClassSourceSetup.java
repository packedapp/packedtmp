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

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentModifier;
import app.packed.inject.Factory;
import packed.internal.component.PackedComponentDriver.BoundClassComponentDriver;
import packed.internal.hooks.usesite.BootstrappedClassModel;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProducer;
import packed.internal.inject.dependency.InjectionNode;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.invoke.constantpool.ConstantPool;
import packed.internal.invoke.constantpool.PoolWriteable;
import packed.internal.util.MethodHandleUtil;

/** A configuration object for a component class source. */
public final class ClassSourceSetup implements DependencyProducer, PoolWriteable {

    public final SourcedComponentSetup component;

    /** If the source represents a constant. */
    @Nullable
    private final Object constant;

    /**
     * Factory that was specified. We only keep this around to find the key that it should be exposed as a service with. As
     * we need to lazy calculate it from {@link #provide(ComponentSetup)}
     */
    @Nullable
    private final Factory<?> factory;

    /** A model of every hook on the source. */
    public final BootstrappedClassModel hooks; // contains provided stuff

    /** An injection node, if instances of the source needs to be created at runtime (not a constant). */
    @Nullable
    private final InjectionNode instantiator;

    /** The index at which to store the runtime instance, or -1 if it should not be stored. */
    public final int poolIndex;

    /** A service object if the source is provided as a service. */
    @Nullable
    public ServiceSetup service;

    /**
     * Creates a new setup.
     * 
     * @param component
     *            the component
     * @param source
     *            the class, factory or instance source
     */
    ClassSourceSetup(SourcedComponentSetup component, BoundClassComponentDriver<?> driver, Object source) {
        this.component = component;

        // Reserve a place in the constant pool if the source is a singleton
        this.poolIndex = PackedComponentModifierSet.isSet(driver.modifiers, ComponentModifier.CONSTANT) ? component.pool.reserveObject() : -1;

        // A realm accessor that allows us to find all hooks a component source
        RealmAccessor accessor = component.realm.accessor();

        // The source is either a Class, a Factory, or a generic instance
        if (source instanceof Class<?> cl) {
            this.constant = null;
            boolean isStaticClassSource = PackedComponentModifierSet.isSet(driver.modifiers, ComponentModifier.STATEFUL);
            // was driver.modifiers().isStaticClassSource() 
            this.factory = isStaticClassSource ? null : Factory.of(cl);
            this.hooks = accessor.modelOf(cl);
        } else if (source instanceof Factory<?> fac) {
            this.constant = null;
            this.factory = fac;
            this.hooks = accessor.modelOf(factory.rawType());
        } else {
            this.constant = source;
            this.factory = null;
            this.hooks = accessor.modelOf(source.getClass());

            // non-constants singlestons are added to the constant pool elsewhere
            component.pool.addConstant(this); // writeToPool will be called later
        }

        if (factory == null) {
            this.instantiator = null;
        } else {
            MethodHandle mh = accessor.toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.variables();
            this.instantiator = new InjectionNode(this, dependencies, mh);
            component.container.injection.addNode(instantiator);
        }

        hooks.onWire(this);
    }
    
    /**
     * Creates a new setup.
     * 
     * @param component
     *            the component
     * @param source
     *            the class, factory or instance source
     */
    ClassSourceSetup(SourcedComponentSetup component, OldPackedClassComponentDriver<?> driver, Object source) {
        this.component = component;

        // Reserve a place in the constant pool if the source is a singleton
        this.poolIndex = driver.modifiers().isSingleton() ? component.pool.reserveObject() : -1;

        // A realm accessor that allows us to find all hooks a component source
        RealmAccessor accessor = component.realm.accessor();

        // The source is either a Class, a Factory, or a generic instance
        if (source instanceof Class<?> cl) {
            this.constant = null;
            this.factory = driver.modifiers().isStaticClassSource() ? null : Factory.of(cl);
            this.hooks = accessor.modelOf(cl);
        } else if (source instanceof Factory<?> fac) {
            this.constant = null;
            this.factory = fac;
            this.hooks = accessor.modelOf(factory.rawType());
        } else {
            this.constant = source;
            this.factory = null;
            this.hooks = accessor.modelOf(source.getClass());

            // non-constants singlestons are added to the constant pool elsewhere
            component.pool.addConstant(this); // writeToPool will be called later
        }

        if (factory == null) {
            this.instantiator = null;
        } else {
            MethodHandle mh = accessor.toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.variables();
            this.instantiator = new InjectionNode(this, dependencies, mh);
            component.container.injection.addNode(instantiator);
        }

        hooks.onWire(this);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public InjectionNode dependant() {
        return instantiator;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // Must return MethodHandle(ConstantPool)T

        if (constant != null) { // we have
            return MethodHandleUtil.insertFakeParameter(MethodHandleUtil.constant(constant), ConstantPool.class); // MethodHandle()T ->
                                                                                                                  // MethodHandle(ConstantPool)T
        } else if (poolIndex > -1) {
            return ConstantPool.indexedReader(poolIndex, hooks.clazz);
        } else {
            return instantiator.buildMethodHandle();
        }
    }

    public ServiceSetup provide(SourcedComponentSetup component) {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        ServiceSetup s = service;
        if (s == null) {
            Key<?> key;
            if (constant != null) {
                key = Key.of(hooks.clazz); // Move to model?? What if instance has Qualifier???
            } else {
                key = factory.key();
            }
            s = service = component.container.injection.getServiceManagerOrCreate().provideSource(component, key);
        }
        return s;
    }

    public void writeToPool(ConstantPool pool) {
        assert poolIndex >= 0;
        assert constant != null;
        pool.storeObject(poolIndex, constant);
    }
}
