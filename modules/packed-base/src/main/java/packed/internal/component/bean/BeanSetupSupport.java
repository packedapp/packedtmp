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
package packed.internal.component.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Factory;
import packed.internal.component.ComponentSetup;
import packed.internal.component.RealmAccessor;
import packed.internal.hooks.usesite.BootstrappedClassModel;
import packed.internal.inject.dependency.DependencyDescriptor;
import packed.internal.inject.dependency.DependencyProducer;
import packed.internal.inject.dependency.InjectionNode;
import packed.internal.inject.service.build.ServiceSetup;
import packed.internal.lifetime.LifetimePool;
import packed.internal.lifetime.PoolAccessor;

/** A configuration object for a component class source. */
public final class BeanSetupSupport implements DependencyProducer {

    public final BeanSetup bean;

    /** If the source represents a constant. */
    @Nullable
    private final Object boundToinstance;

    /**
     * Factory that was specified. We only keep this around to find the key that it should be exposed as a service with. As
     * we need to lazy calculate it from {@link #provide(ComponentSetup)}
     */
    @Nullable
    private final Factory<?> factory;

    /** A model of every hook on the source. */
    public final BootstrappedClassModel hookModel; // contains provided stuff

    /** An injection node, if instances of the source needs to be created at runtime (not a constant). */
    @Nullable
    private final InjectionNode injectionNode;

    /** A service object if the source is provided as a service. */
    @Nullable
    public ServiceSetup service;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    public final PoolAccessor singletonAccessor;

    /**
     * Creates a new setup.
     * 
     * @param bean
     *            the component
     * @param source
     *            the class, factory or instance source
     */
    BeanSetupSupport(BeanSetup bean, PackedBeanDriver<?> driver, Object source) {
        this.bean = bean;

        // Reserve a place in the constant pool if the source is a singleton
        // If instance != null we kan vel pool.permstore()
        // BuildStore
        this.singletonAccessor = driver.isConstant ? bean.lifetime.pool.reserve() : null;

        // A realm accessor that allows us to find all hooks a component source
        RealmAccessor accessor = bean.realm.accessor();

        // The source is either a Class, a Factory, or a generic instance
        if (source instanceof Class<?> cl) {
            this.boundToinstance = null;
            // TODO fix
            boolean isStaticClassSource = false;
            this.factory = isStaticClassSource ? null : Factory.of(cl);

        } else if (source instanceof Factory<?> fac) {
            this.boundToinstance = null;
            this.factory = fac;
        } else {
            this.boundToinstance = source;
            this.factory = null;

            // non-constants singlestons are added to the constant pool elsewhere
            bean.lifetime.pool.addConstant(pool -> singletonAccessor.store(pool, boundToinstance)); // writeToPool will be called later
        }

        if (factory == null) {
            this.injectionNode = null;
        } else {
            MethodHandle mh = accessor.toMethodHandle(factory);

            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<DependencyDescriptor> dependencies = (List) factory.variables();
            this.injectionNode = new InjectionNode(this, dependencies, mh);
            bean.container.injection.addNode(injectionNode);
        }

        // Find a hook model for the bean type and wire it
        this.hookModel = accessor.modelOf(driver.beanType());
        hookModel.onWire(this);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public InjectionNode dependant() {
        return injectionNode;
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // Must return MethodHandle(ConstantPool)T
        if (boundToinstance != null) {
            // Jeg tror faktisk godt vi vil returnere den for array'et istedet for...
            MethodHandle mh = MethodHandles.constant(boundToinstance.getClass(), boundToinstance); // MethodHandle()T
            mh = MethodHandles.dropArguments(mh, 0, LifetimePool.class); // MethodHandle()T -> // MethodHandle(LifetimePool)T
            return mh;
        } else if (singletonAccessor != null) {
            return singletonAccessor.indexedReader(hookModel.clazz);
        } else {
            return injectionNode.buildMethodHandle();
        }
    }

    public ServiceSetup provide() {
        // Maybe we should throw an exception, if the user tries to provide an entry multiple times??
        ServiceSetup s = service;
        if (s == null) {
            Key<?> key;
            if (factory != null) {
                key = factory.key();
            } else {
                key = Key.of(hookModel.clazz); // Move to model?? What if instance has Qualifier???
            }
            s = service = bean.container.injection.getServiceManagerOrCreate().provideSource(bean, key);
        }
        return s;
    }
}
