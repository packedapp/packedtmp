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
package packed.internal.bean.hooks.usesite;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.hooks.OldBeanClass;
import app.packed.bean.operation.OperationMirror;
import app.packed.inject.service.ServiceExtension;
import packed.internal.bean.BeanOperationManager;
import packed.internal.bean.BeanOperationSetup;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.hooks.AbstractHookModel;
import packed.internal.bean.hooks.ClassHookModel;
import packed.internal.component.ComponentSetup;
import packed.internal.inject.DependencyNode;
import packed.internal.inject.DependencyProducer;
import packed.internal.inject.InternalDependency;

/**
 *
 */
public sealed abstract class UseSiteMemberHookModel extends JavaHookElementModel permits UseSiteFieldHookModel, UseSiteMethodHookModel {

    /** Dependencies that needs to be resolved. */
    public final List<InternalDependency> dependencies;

    @Nullable
    public final Consumer<? super ComponentSetup> processor;

    public final boolean provideAsConstant;

    @Nullable
    public final Key<?> provideAskey;

    // Jeg tror man loeber alle parameterene igennem og ser om der
    // er en sidecar provide der passer dem
    // Saa man sidecar providen dertil.

    @Nullable
    private final Supplier<? extends OperationMirror> supplier;

    UseSiteMemberHookModel(Builder builder, List<InternalDependency> dependencies) {
        this.dependencies = requireNonNull(dependencies);
        this.provideAsConstant = builder.provideAsConstant;
        this.provideAskey = builder.provideAsKey;
        this.processor = builder.processor;
        this.supplier = builder.operation == null ? null : builder.operation.supplier;
        if (builder instanceof UseSiteMethodHookModel.Builder bb) {
            System.out.println("XXXX " + bb.shared.methodUnsafe);
        }
    }

    public void onWire(BeanSetup bean) {
        // Register hooks, maybe move to component setup
        DependencyNode node = new BeanMemberDependencyNode(bean, this, createProviders());

        BeanOperationManager bom = bean.operations;
        BeanOperationSetup os = new BeanOperationSetup(bean, ServiceExtension.class);
        bom.addOperation(os);
        os.mirrorSupplier = supplier;

        bean.parent.injectionManager.addConsumer(node);
        if (processor != null) {
            processor.accept(bean);
        }
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

    public static sealed abstract class Builder extends AbstractBootstrapBuilder permits UseSiteFieldHookModel.Builder, UseSiteMethodHookModel.Builder {

        @Nullable
        // Eneste problem er at dette ogsaa kan vaere en buildTime model..
        // Maaske skal vi have en faelles klasse??
        AbstractHookModel<?> buildtimeModel;

        /** Any extension class that manages this member. */
        UseSiteClassHookModel.@Nullable Builder managedBy;

        @Nullable
        Consumer<? super ComponentSetup> processor;

        /** If the member is being provided as a service whether or not it is constant. */
        boolean provideAsConstant;

        /** If the member is being provided as a service its key. */
        @Nullable
        Key<?> provideAsKey;

        Builder(HookModel.Builder source, AbstractHookModel<?> model) {
            super(source);
            this.buildtimeModel = model;
        }

        @Nullable
        private PackedHookOperationConfiguration operation;

        public final PackedHookOperationConfiguration operation() {
            PackedHookOperationConfiguration o = operation;
            if (o == null) {
                operation = o = new PackedHookOperationConfiguration();
            }
            return o;
        }

        public final Optional<Class<?>> buildType() {
            if (disabled) {
                return Optional.empty();
            } else if (buildtimeModel == null) {
                return Optional.empty();
            }
            return Optional.of(buildtimeModel.bootstrapImplementation());
        }

        public void complete() {}

        public final void disable() {
            disabled = true;
            this.buildtimeModel = null;
        }

        @SuppressWarnings("unchecked")
        public final <T extends OldBeanClass> T manageBy(Class<T> type) {
            requireNonNull(type, "The specified type is null");
            checkNotDisabled();
            if (managedBy != null) {
                throw new IllegalStateException("This method can only be invoked once");
            }
            UseSiteClassHookModel.Builder builder = managedBy = source.classes.computeIfAbsent(type,
                    c -> new UseSiteClassHookModel.Builder(source, ClassHookModel.ofManaged(type)));
            return (T) builder.instance;
        }
    }
}
