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

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Factory;
import packed.internal.component.ComponentSetup;
import packed.internal.component.ConstantPool;
import packed.internal.component.SourcedComponentDriver;
import packed.internal.inject.Dependant;
import packed.internal.inject.DependencyDescriptor;
import packed.internal.inject.DependencyProvider;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.util.MethodHandleUtil;

/** A configuration object for a component class source. */
public final class ClassSourceSetup implements DependencyProvider {

    /** An injectable, if this source needs to be created at runtime (not a constant). */
    @Nullable
    private final Dependant dependant;

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
    public final ClassSourceModel model;

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
     * @param driver
     *            the component driver
     */
    public ClassSourceSetup(ComponentSetup component, SourcedComponentDriver<?> driver) {
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
            component.pool.addSourceClass(this); // non-constants singlestons are added to the constant pool elsewhere
        }

        // if (driver.modifiers().isSingleton())

        if (factory == null) {
            this.dependant = null;
        } else {
            MethodHandle mh = realm.toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.variables();
            this.dependant = new Dependant(this, dependencies, mh);
            component.memberOfContainer.addDependant(dependant);
        }

        // Register hooks, maybe move to component setup
        model.registerHooks(component, this);
    }

    public void writeConstantPool(ConstantPool pool) {
        assert poolIndex >= 0;
        assert constant != null;
        pool.store(poolIndex, constant);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Dependant dependant() {
        return dependant;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        if (constant != null) {
            return MethodHandleUtil.insertFakeParameter(MethodHandleUtil.constant(constant), ConstantPool.class); // MethodHandle()T -> MethodHandle(ConstantPool)T
        } else if (poolIndex > -1) {
            return ConstantPool.readConstant(poolIndex, model.type);
        } else {
            return dependant.buildMethodHandle();
        }
    }

    public ServiceSetup provide(ComponentSetup component) {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        ServiceSetup s = service;
        if (s == null) {
            Key<?> key;
            if (constant != null) {
                key = Key.of(model.type); // Move to model?? What if instance has Qualifier???
            } else {
                key = factory.key();
            }
            s = service = component.memberOfContainer.getServiceManagerOrCreate().provideSource(component, key);
        }
        return s;
    }
}
