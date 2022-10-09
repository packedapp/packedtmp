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
import java.util.List;

import app.packed.base.Nullable;
import app.packed.bean.BeanSourceKind;
import internal.app.packed.bean.BeanKind;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanHandleInstaller;
import internal.app.packed.container.ExtensionRealmSetup;
import internal.app.packed.lifetime.pool.Accessor;
import internal.app.packed.operation.op.PackedOp;
import internal.app.packed.operation.op.ReflectiveOp;

/**
 * An injection manager for a bean.
 */
public final class BeanInjectionManager implements DependencyProducer {

    /**
     * A dependency node representing a bean instance and its factory method. Or {@code null} for functional beans and other
     * {@code void} beans.
     */
    @Nullable
    private final DependencyNode instanceNode;

    /** The parent injector. */
    final ContainerOrExtensionInjectionManager parent;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    @Nullable
    public final Accessor singletonAccessor;

    public BeanInjectionManager(BeanSetup bean, PackedBeanHandleInstaller<?> driver) {
        if (driver.sourceKind == BeanSourceKind.INSTANCE) {
            this.singletonAccessor = new Accessor.ConstantAccessor(driver.source);
        } else if (driver.beanKind() == BeanKind.SINGLETON) {
            this.singletonAccessor = bean.container.lifetime.pool.reserve(driver.beanClass);
        } else {
            this.singletonAccessor = null;
        }

        // Can only register a single extension bean of a particular type
        if (bean.realm instanceof ExtensionRealmSetup e) {
            ExtensionInjectionManager eim = bean.installer.extensionOwner.injectionManager;
            if (driver.beanKind() == BeanKind.SINGLETON) {
                eim.addBean(bean);
            }
            parent = eim;
        } else {
            parent = bean.container.injectionManager;
        }

        // Only create an instance node if we have instances
        if (driver.sourceKind == BeanSourceKind.INSTANCE || !bean.installer.instantiate) {
            this.instanceNode = null;
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
        if (singletonAccessor != null) {
            return singletonAccessor.poolReader(); // MethodHandle(ConstantPool)T
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
