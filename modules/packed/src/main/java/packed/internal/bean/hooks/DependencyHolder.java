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
package packed.internal.bean.hooks;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.List;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.operation.mirror.OperationMirror;
import app.packed.inject.service.ServiceExtension;
import packed.internal.bean.BeanOperationSetup;
import packed.internal.bean.BeanSetup;
import packed.internal.inject.DependencyNode;
import packed.internal.inject.DependencyProducer;
import packed.internal.inject.InternalDependency;

/**
 *
 */
public abstract class DependencyHolder extends KeyProvidable{

    /** Dependencies that needs to be resolved. */
    public final List<InternalDependency> dependencies;
//
//    @Nullable
//    public final Consumer<? super ComponentSetup> processor = null;

    public final boolean provideAsConstant;

    // Jeg tror man loeber alle parameterene igennem og ser om der
    // er en sidecar provide der passer dem
    // Saa man sidecar providen dertil.

    @Nullable
    private final Supplier<? extends OperationMirror> supplier;

    DependencyHolder(List<InternalDependency> dependencies, boolean provideAsConstant, Key<?> provideAsKey) {
        super(provideAsKey);
        this.dependencies = requireNonNull(dependencies);
        this.provideAsConstant = provideAsConstant;
        // this.processor = builder.processor;
        this.supplier = null;
    }

    public void onWire(BeanSetup bean) {
        // Register hooks, maybe move to component setup
        DependencyNode node = new BeanMemberDependencyNode(bean, this, createProviders());

        BeanOperationSetup os = new BeanOperationSetup(bean, ServiceExtension.class);
        bean.addOperation(os);
        os.mirrorSupplier = supplier;

        bean.parent.injectionManager.addConsumer(node);
        
        
//        if (processor != null) {
//            processor.accept(bean);
//        }
    }

    public abstract DependencyProducer[] createProviders();

    /**
     * Returns the modifiers of the underlying member.
     * 
     * @return the modifiers of the underlying member
     * 
     * @see Member#getModifiers()
     */
    public abstract int getModifiers();

    public abstract MethodHandle methodHandle();

}
