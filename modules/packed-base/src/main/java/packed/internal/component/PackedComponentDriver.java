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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Factory;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.PackedRealm;
import packed.internal.inject.factory.BaseFactory;
import packed.internal.inject.factory.FactoryHandle;
import packed.internal.service.buildtime.service.PackedSingletonConfiguration;

/**
 *
 */
public abstract class PackedComponentDriver<C> implements ComponentDriver<C> {

    public static final int ROLE_CONTAINER = 1;
    public static final int ROLE_GUEST = 2;
    public static final int ROLE_HOST = 4;
    public static final int ROLE_SINGLETON = 8;
    public static final int ROLE_STATELESS = 16;
    public static final int ROLE_EXTENSION = 32;

    // Statemanagement... A function is kind of just a singleton...

    private final int roles;

    protected PackedComponentDriver(int roles) {
        this.roles = roles;
    }

    public final boolean hasRuntimeRepresentation() {
        return !isExtension();
    }

    public String defaultName(PackedRealm realm) {
        if (isContainer()) {
            // I think try and move some of this to ComponentNameWirelet
            @Nullable
            Class<?> source = realm.type();
            if (Bundle.class.isAssignableFrom(source)) {
                String nnn = source.getSimpleName();
                if (nnn.length() > 6 && nnn.endsWith("Bundle")) {
                    nnn = nnn.substring(0, nnn.length() - 6);
                }
                if (nnn.length() > 0) {
                    // checkName, if not just App
                    // TODO need prefix
                    return nnn;
                }
                if (nnn.length() == 0) {
                    return "Container";
                }
            }
            // TODO think it should be named Artifact type, for example, app, injector, ...
            return "Unknown";
        } else {
            return ((ModelComponentDriver<?>) this).model.defaultPrefix();
        }
    }

    public final boolean hasRole(int role) {
        return (roles & role) != 0;
    }

    /**
     * Returns whether or not this driver creates a component with container role.
     * 
     * @return whether or not this driver creates a component with container role
     */
    public final boolean isContainer() {
        return hasRole(ROLE_CONTAINER);
    }

    public final boolean isExtension() {
        return hasRole(ROLE_EXTENSION);
    }

    public final boolean isGuest() {
        return hasRole(ROLE_GUEST);
    }

    public C forBundleConf(ComponentNodeConfiguration cnc) {
        throw new UnsupportedOperationException();
    }

    public static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        public static ContainerComponentDriver INSTANCE = new ContainerComponentDriver();

        ContainerComponentDriver() {
            super(ROLE_CONTAINER);
        }

        @Override
        public ContainerConfiguration forBundleConf(ComponentNodeConfiguration cnc) {
            return new PackedContainerConfiguration(cnc.container);
        }
    }

    public static class SingletonComponentDriver extends ModelComponentDriver<ComponentConfiguration> {
        @Nullable
        public final BaseFactory<?> factory;

        @Nullable
        public final Object instance;

        public SingletonComponentDriver(PackedRealm realm, Factory<?> factory) {
            super(PackedComponentDriver.ROLE_SINGLETON, realm.componentModelOf(factory.rawType()));
            this.factory = (@Nullable BaseFactory<?>) factory;
            this.instance = null;
        }

        public SingletonComponentDriver(PackedRealm realm, Object instance) {
            super(PackedComponentDriver.ROLE_SINGLETON, realm.componentModelOf(instance.getClass()));
            this.factory = null;
            this.instance = requireNonNull(instance);
        }

        public MethodHandle fromFactory(ComponentNodeConfiguration context) {
            FactoryHandle<?> handle = factory.factory.handle;
            return context.realm().fromFactoryHandle(handle);
        }

        public <T> SingletonConfiguration<T> toConf(ComponentNodeConfiguration context) {
            model.invokeOnHookOnInstall(context);
            return new PackedSingletonConfiguration<>(context);
        }

        public boolean isInstance() {
            return instance == null;
        }
    }

    public static abstract class ModelComponentDriver<T> extends PackedComponentDriver<T> {
        public final ComponentModel model;

        /**
         * @param roles
         */
        ModelComponentDriver(int roles, ComponentModel model) {
            super(roles);
            this.model = model;
        }

    }

    public static class StatelessComponentDriver extends ModelComponentDriver<ComponentConfiguration> {

        public StatelessComponentDriver(PackedRealm lookup, Class<?> implementation) {
            super(PackedComponentDriver.ROLE_STATELESS, lookup.componentModelOf(requireNonNull(implementation, "implementation is null")));
            requireNonNull(implementation, "implementation is null");
        }

        public StatelessConfiguration toConf(ComponentNodeConfiguration context) {
            model.invokeOnHookOnInstall(context);
            return new PackedStatelessComponentConfiguration(context);
        }
    }
}
