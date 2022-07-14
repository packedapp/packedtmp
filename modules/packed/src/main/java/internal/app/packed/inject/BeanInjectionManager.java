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
package internal.app.packed.inject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.bean.BeanKind;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanHandleBuilder;
import internal.app.packed.bean.PackedBeanHandleBuilder.SourceType;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionRealmSetup;
import internal.app.packed.inject.factory.InternalFactory;
import internal.app.packed.inject.factory.ReflectiveFactory;
import internal.app.packed.lifetime.pool.LifetimeConstantPool;
import internal.app.packed.lifetime.pool.PoolEntryHandle;

/**
 * An injection manager for a bean.
 */
public final class BeanInjectionManager extends InjectionManager implements DependencyProducer {

    /** The bean this injection manager belongs to. */
    private final BeanSetup bean;

    /**
     * A dependency node representing a bean instance and its factory method. Or {@code null} for functional beans and other
     * {@code void} beans.
     */
    @Nullable
    private final DependencyNode instanceNode;

    /** The parent injector. */
    final ContainerOrExtensionInjectionManager parent;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    // What if managed prototype bean????
    @Nullable
    public final PoolEntryHandle singletonHandle;

    public BeanInjectionManager(BeanSetup bean, PackedBeanHandleBuilder<?> driver) {
        this.bean = bean;
        ContainerSetup container = bean.parent;
        this.singletonHandle = driver.beanKind() == BeanKind.CONTAINER ? bean.lifetime.pool.reserve(driver.beanClass()) : null;

        // Can only register a single extension bean of a particular type

        if (bean.realm instanceof ExtensionRealmSetup e) {
            ExtensionInjectionManager eim = e.injectionManagerFor(bean);
            if (driver.beanKind() == BeanKind.CONTAINER) {
                eim.addBean(bean);
            }
            parent = eim;
        } else {
            parent = container.injectionManager;
        }

        if (bean.builder.sourceType == SourceType.NONE) {
            this.instanceNode = null;
        } else if (driver.sourceType == SourceType.INSTANCE) {
            Object instance = driver.source;

            // We either have no bean instances or an instance was explicitly provided.
            this.instanceNode = null;
            bean.lifetime.pool.addConstant(pool -> singletonHandle.store(pool, instance));

            // new BeanInstanceDependencyNode(bean, this, List.of(), MethodHandles.constant(instance.getClass(), instance));
            // Store the supplied bean instance in the lifetime (constant) pool.
            // Skal vel faktisk vaere i application poolen????
            // Ja der er helt sikker forskel paa noget der bliver initializeret naar containeren bliver initialiseret
            // og saa constant over hele applikation.
            // Skal vi overhoved have en constant pool???

            // functional beans will have null in driver.source

            // Or maybe just bind the instance directly in the method handles.
        } else {
            InternalFactory<?> factory;
            if (driver.sourceType == SourceType.CLASS) {
                factory = ReflectiveFactory.DEFAULT_FACTORY.get((Class<?>) driver.source);
            } else {
                factory = (InternalFactory<?>) driver.source;
            }
            // Extract a MethodHandlefrom the factory
            MethodHandle mh = bean.realm.beanAccessor().toMethodHandle(factory);

            List<InternalDependency> dependencies = factory.dependencies();
            this.instanceNode = new BeanInstanceDependencyNode(bean, this, dependencies, mh);

            bean.parent.injectionManager.addConsumer(instanceNode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // If we have a singleton accessor return a method handle that can read the single bean instance
        // Otherwise return a method handle that can instantiate a new bean

        if (bean.builder.sourceType == SourceType.INSTANCE) {
            Object instance = bean.builder.source;
            MethodHandle mh = MethodHandles.constant(instance.getClass(), instance);
            return MethodHandles.dropArguments(mh, 0, LifetimeConstantPool.class);
            // return MethodHandles.constant(instance.getClass(), instance);
        } else if (singletonHandle != null) {
            return singletonHandle.poolReader(); // MethodHandle(ConstantPool)T
        } else {
            return instanceNode.runtimeMethodHandle(); // MethodHandle(ConstantPool)T
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DependencyNode dependencyConsumer() {
        return instanceNode;
    }
}
