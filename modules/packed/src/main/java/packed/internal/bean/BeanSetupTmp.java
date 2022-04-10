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
package packed.internal.bean;

import java.lang.invoke.MethodHandle;
import java.util.List;

import app.packed.base.Nullable;
import app.packed.bean.BeanKind;
import packed.internal.bean.PackedBeanDriver.SourceType;
import packed.internal.bean.inject.DependencyNode;
import packed.internal.bean.inject.DependencyProducer;
import packed.internal.bean.inject.InternalDependency;
import packed.internal.inject.InternalFactory;
import packed.internal.inject.ReflectiveFactory;
import packed.internal.lifetime.PoolEntryHandle;

/**
 *
 */
public final class BeanSetupTmp implements DependencyProducer {

    /**
     * A dependency node for the bean instance.
     * <p>
     * The node is {@code null} for functional beans, or bean instance that was specified when configuring the bean. Or
     * non-null if a bean instance needs to be created at runtime. This include beans that have an empty constructor (no
     * actual dependencies).
     */
    @Nullable
    private final DependencyNode dependencyNode;

    /** A pool accessor if a single instance of this bean is created. null otherwise */
    // What if managed prototype bean????
    @Nullable
    public final PoolEntryHandle singletonHandle;

    BeanSetupTmp(BeanSetup bean) {
        this.singletonHandle = bean.driver.beanKind() == BeanKind.CONTAINER ? bean.lifetime.pool.reserve(bean.driver.beanType) : null;

        if (bean.driver.sourceType == SourceType.INSTANCE || bean.driver.sourceType == SourceType.NONE) {
            // We either have no bean instances or an instance was explicitly provided.
            this.dependencyNode = null;

            // Store the supplied bean instance in the lifetime (constant) pool.
            // Skal vel faktisk vaere i application poolen????
            // Ja der er helt sikker forskel paa noget der bliver initializeret naar containeren bliver initialiseret
            // og saa constant over hele applikation.
            // Skal vi overhoved have en constant pool???

            // functional beans will have null in driver.source
            if (bean.driver.source != null) {
                bean.lifetime.pool.addConstant(pool -> singletonHandle.store(pool, bean.driver.source));
            }
            // Or maybe just bind the instance directly in the method handles.
        } else {
            InternalFactory<?> factory;
            if (bean.driver.sourceType == SourceType.CLASS) {
                factory = ReflectiveFactory.DEFAULT_FACTORY.get((Class<?>) bean.driver.source);
            } else {
                factory = (InternalFactory<?>) bean.driver.source;
            }
            List<InternalDependency> dependencies = factory.dependencies();

            // Extract a MethodHandlefrom the factory
            MethodHandle mh = bean.realm.accessor().toMethodHandle(factory);

            this.dependencyNode = new BeanInstanceDependencyNode(bean, dependencies, mh);

            bean.parent.beans.addConsumer(dependencyNode);
        }
    }
    

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        // If we have a singleton accessor return a method handle that can read the single bean instance
        // Otherwise return a method handle that can instantiate a new bean
        if (singletonHandle != null) {
            return singletonHandle.poolReader(); // MethodHandle(ConstantPool)T
        } else {
            return dependencyNode.runtimeMethodHandle(); // MethodHandle(ConstantPool)T
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public DependencyNode dependencyConsumer() {
        return dependencyNode;
    }
}
