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
package packed.internal.service.buildtime.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.config.ConfigSite;
import app.packed.container.ExtensionMember;
import app.packed.service.Service;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceWirelets;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.buildtime.export.ExportedBuildEntry;
import packed.internal.service.runtime.ConstantInjectorEntry;
import packed.internal.service.runtime.RuntimeService;

/** The common superclass for upstream service wirelets. */
public abstract class PackedDownstreamInjectionWirelet extends ServiceWirelet {

    @ExtensionMember(ServiceExtension.class)
    public static class FilterOnKey extends PackedDownstreamInjectionWirelet {

        final Set<Key<?>> set;

        public FilterOnKey(Set<Key<?>> set) {
            this.set = requireNonNull(set);
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceExtension p) {

        }

        /** {@inheritDoc} */
        @Override
        public void process(ConfigSite cs, LinkedHashMap<Key<?>, RuntimeService<?>> newServices) {
            newServices.keySet().removeAll(set);
        }
    }

    /** A wirelet for {@link ServiceWirelets#peekFrom(Consumer)}. */
    @ExtensionMember(ServiceExtension.class)
    public static class PeekDownstreamWirelet extends PackedDownstreamInjectionWirelet {

        /** The peek action to execute. */
        private final Consumer<? super Service> action;

        /**
         * Creates a new downstream peek wirelet.
         * 
         * @param action
         *            the peek action to execute
         */
        public PeekDownstreamWirelet(Consumer<? super Service> action) {
            this.action = requireNonNull(action, "action is null");
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceExtension extension) {
            for (ExportedBuildEntry<?> e : InjectionManager.fromExtension(extension).exports()) {
                action.accept(e.toDescriptor());
            }
        }

        /** {@inheritDoc} */
        @Override
        public void process(ConfigSite cs, LinkedHashMap<Key<?>, RuntimeService<?>> newServices) {
            for (var s : newServices.values()) {
                action.accept(s);
            }
        }
    }

    @ExtensionMember(ServiceExtension.class)
    public static class ProvideConstantDownstream extends PackedDownstreamInjectionWirelet {

        final Object instance;

        final Key<?> key;

        public ProvideConstantDownstream(Key<?> key, Object instance) {
            this.key = requireNonNull(key, "key is null");
            this.instance = requireNonNull(instance, "instance is null");
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceExtension p) {
            System.out.println("Nice builder " + p);
        }

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void process(ConfigSite cs, LinkedHashMap<Key<?>, RuntimeService<?>> newServices) {
            newServices.put(key, new ConstantInjectorEntry(cs, key, instance));
        }

        @Override
        public String toString() {
            return "provideInstant(\"" + instance + "\")";
        }
    }

    public abstract void process(ConfigSite cs, LinkedHashMap<Key<?>, RuntimeService<?>> newServices);
}
