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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Member;
import java.util.Optional;

import org.cakeframework.container.Component;

import app.packed.inject.Dependency;
import app.packed.inject.InjectionSite;
import app.packed.inject.Injector;
import app.packed.inject.Key;
import app.packed.util.Nullable;
import app.packed.util.VariableDescriptor;

/** The default implementation of {@link InjectionSite}. */
public class InternalInjectionSites {

    /** The {@link InjectionSite} class as a key */
    public static final Key<?> INJECTION_SITE_KEY = Key.of(InjectionSite.class);

    public static InjectionSite of(Injector injector, Dependency dependency, Component component) {
        return new InjectionSiteForDependency(injector, dependency, component);
    }

    /**
     * Creates a new injection site.
     *
     * @param injector
     *            the injector that is requesting the injection
     * @param key
     *            the key that is being used for lookup
     * @param component
     *            the component (optional) that is requesting the injection
     * @return an injection site for the specified parameters
     */
    public static InjectionSite of(Injector injector, Key<?> key, @Nullable Component component) {
        return new InjectionSiteForKey(injector, key, component);
    }

    /**
     * An implementation of injection site used, when requesting a service directly through an injector, for example, via
     * {@link Injector#with(Class)}.
     */
    static class InjectionSiteForDependency implements InjectionSite {

        /** An optional component, in case the request is via a component's private injector. */
        @Nullable
        private final Component component;

        /** The key of the service that was requested */
        private final Dependency dependency;

        /** The injector from where the service was requested. */
        private final Injector injector;

        InjectionSiteForDependency(Injector injector, Dependency dependency, @Nullable Component component) {
            this.injector = requireNonNull(injector, "injector is null");
            this.dependency = requireNonNull(dependency, "dependency is null");
            this.component = component;

        }

        /** {@inheritDoc} */
        @Override
        public Optional<Component> getComponent() {
            return Optional.ofNullable(component);
        }

        /** {@inheritDoc} */
        @Override
        public int getIndex() {
            return dependency.getIndex();
        }

        /** {@inheritDoc} */
        @Override
        public Injector getInjector() {
            return injector;
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> getKey() {
            return dependency.getKey();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Member> getMember() {
            return dependency.getMember();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<VariableDescriptor> getVariable() {
            return dependency.getVariable();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isOptional() {
            return dependency.isOptional();
        }
    }

    /**
     * An implementation of injection site used, when requesting a service directly through an injector, for example, via
     * {@link Injector#with(Class)}.
     */
    static class InjectionSiteForKey implements InjectionSite {

        /** An optional component, in case the request is via a component's private injector. */
        @Nullable
        private final Component component;

        /** The injector from where the service was requested. */
        private final Injector injector;

        /** The key of the service that was requested */
        private final Key<?> key;

        InjectionSiteForKey(Injector injector, Key<?> key, @Nullable Component component) {
            this.injector = requireNonNull(injector, "injector is null");
            this.key = requireNonNull(key, "key is null");
            this.component = component;

        }

        /** {@inheritDoc} */
        @Override
        public Optional<Component> getComponent() {
            return Optional.ofNullable(component);
        }

        /** {@inheritDoc} */
        @Override
        public int getIndex() {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public Injector getInjector() {
            return injector;
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> getKey() {
            return key;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Member> getMember() {
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public Optional<VariableDescriptor> getVariable() {
            return Optional.empty();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isOptional() {
            return false;
        }
    }
}
