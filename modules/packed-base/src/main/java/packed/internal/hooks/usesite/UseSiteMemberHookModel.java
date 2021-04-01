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
package packed.internal.hooks.usesite;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.hooks.ClassHook;
import packed.internal.component.ComponentSetup;
import packed.internal.hooks.AbstractHookModel;
import packed.internal.hooks.ClassHookModel;
import packed.internal.inject.DependencyDescriptor;
import packed.internal.inject.DependencyProvider;

/**
 *
 */
// SourceModel...
// Maa have en liste af regions slots den skal bruge
public abstract class UseSiteMemberHookModel {

    /** Dependencies that needs to be resolved. */
    public final List<DependencyDescriptor> dependencies;

    @Nullable
    public final Consumer<? super ComponentSetup> processor;

    public final boolean provideAsConstant;

    @Nullable
    public final Key<?> provideAskey;

    // Jeg tror man loeber alle parameterene igennem og ser om der
    // er en sidecar provide der passer dem
    // Saa man sidecar providen dertil.

    UseSiteMemberHookModel(Builder builder, List<DependencyDescriptor> dependencies) {
        this.dependencies = requireNonNull(dependencies);
        this.provideAsConstant = builder.provideAsConstant;
        this.provideAskey = builder.provideAsKey;
        this.processor = builder.processor;
    }

    public abstract DependencyProvider[] createProviders();

    /**
     * Returns the modifiers of the underlying member.
     * 
     * @return the modifiers of the underlying member
     * 
     * @see Member#getModifiers()
     */
    public abstract int getModifiers();

    public abstract MethodHandle methodHandle();

    public static abstract class Builder extends AbstractBootstrapBuilder {

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

        Builder(HookedClassModel.Builder source, AbstractHookModel<?> model) {
            super(source);
            this.buildtimeModel = model;
        }

        public final Optional<Class<?>> buildType() {
            if (disabled) {
                return Optional.empty();
            } else if (buildtimeModel == null) {
                return Optional.empty();
            }
            return Optional.of(buildtimeModel.bootstrapImplementation());
        }

        /**
         * 
         */
        public void complete() {

        }

        public final void disable() {
            disabled = true;
            this.buildtimeModel = null;
        }

        @SuppressWarnings("unchecked")
        public final <T extends ClassHook.Bootstrap> T manageBy(Class<T> type) {
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
