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
package internal.app.packed.service.inject;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.bean.BeanSourceKind;
import internal.app.packed.bean.BeanKind;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanHandleInstaller;
import internal.app.packed.container.ExtensionRealmSetup;
import internal.app.packed.lifetime.pool.LifetimeConstantPool;
import internal.app.packed.lifetime.pool.PoolEntryHandle;
import internal.app.packed.operation.op.PackedOp;
import internal.app.packed.operation.op.ReflectiveOp;

/**
 * An injection manager for a bean.
 */
public final class BeanInjectionManager implements DependencyProducer {

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

    public BeanInjectionManager(BeanSetup bean, PackedBeanHandleInstaller<?> driver) {
        this.bean = bean;
        this.singletonHandle = driver.beanKind() == BeanKind.SINGLETON ? bean.lifetime().pool.reserve(driver.beanClass) : null;

        // Can only register a single extension bean of a particular type

        if (bean.realm instanceof ExtensionRealmSetup e) {
            ExtensionInjectionManager eim = e.injectionManagerFor(bean);
            if (driver.beanKind() == BeanKind.SINGLETON) {
                eim.addBean(bean);
            }
            parent = eim;
        } else {
            parent = bean.container.injectionManager;
        }

        // Only create an instance node if we have instances
        if (!bean.installer.hasInstances) {
            this.instanceNode = null;
        } else if (driver.sourceKind == BeanSourceKind.INSTANCE) {
            this.instanceNode = null;
            bean.lifetime().pool.addConstant(pool -> singletonHandle.store(pool, driver.source));
        } else {
            PackedOp<?> op;
            if (driver.sourceKind == BeanSourceKind.CLASS) {
                op = ReflectiveOp.DEFAULT_FACTORY.get((Class<?>) driver.source);
            } else {
                op = (PackedOp<?>) driver.source;
            }
            // Extract a MethodHandlefrom the factory
            MethodHandle mh = bean.realm.beanAccessor().toMethodHandle(op);

            List<InternalDependency> dependencies = InternalDependency.fromOperationType(op.type());// null;//factory.dependencies();
            this.instanceNode = new BeanInstanceDependencyNode(bean, this, dependencies, mh);

            bean.container.injectionManager.addConsumer(instanceNode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // If we have a singleton accessor return a method handle that can read the single bean instance
        // Otherwise return a method handle that can instantiate a new bean

        if (bean.installer.sourceKind == BeanSourceKind.INSTANCE) {
            Object instance = bean.installer.source;
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
