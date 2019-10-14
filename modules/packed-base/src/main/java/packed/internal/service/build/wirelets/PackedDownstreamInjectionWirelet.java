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
package packed.internal.service.build.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.config.ConfigSite;
import app.packed.service.ServiceDescriptor;
import app.packed.service.ServiceWirelets;
import app.packed.util.Key;
import packed.internal.service.ServiceEntry;
import packed.internal.service.build.ServiceWireletPipeline;
import packed.internal.service.build.export.ExportedBuildEntry;
import packed.internal.service.run.RuntimeEntry;
import packed.internal.service.run.SingletonRuntimeEntry;

/** The common superclass for upstream service wirelets. */
public abstract class PackedDownstreamInjectionWirelet extends ServiceWirelet {

    public static class FilterOnKey extends PackedDownstreamInjectionWirelet {

        final Set<Key<?>> set;

        public FilterOnKey(Set<Key<?>> set) {
            this.set = requireNonNull(set);
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceWireletPipeline p) {

        }

        /** {@inheritDoc} */
        @Override
        public void process(ConfigSite cs, LinkedHashMap<Key<?>, ServiceEntry<?>> newServices) {
            newServices.keySet().removeAll(set);
        }
    }

    /** A wirelet for {@link ServiceWirelets#peekUpstream(Consumer)}. */
    public static class PeekDownstreamWirelet extends PackedDownstreamInjectionWirelet {

        /** The peek action to execute. */
        private final Consumer<? super ServiceDescriptor> action;

        /**
         * Creates a new downstream peek wirelet.
         * 
         * @param action
         *            the peek action to execute
         */
        public PeekDownstreamWirelet(Consumer<? super ServiceDescriptor> action) {
            this.action = requireNonNull(action, "action is null");
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceWireletPipeline extension) {
            for (ExportedBuildEntry<?> e : extension.node.exports()) {
                action.accept(e.toDescriptor());
            }
        }

        /** {@inheritDoc} */
        @Override
        public void process(ConfigSite cs, LinkedHashMap<Key<?>, ServiceEntry<?>> newServices) {
            for (var s : newServices.values()) {
                action.accept((RuntimeEntry<?>) s);
            }
        }
    }

    public static class ProvideConstantDownstream extends PackedDownstreamInjectionWirelet {

        final Object instance;

        final Key<?> key;

        public ProvideConstantDownstream(Key<?> key, Object instance) {
            this.key = requireNonNull(key, "key is null");
            this.instance = requireNonNull(instance, "instance is null");
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceWireletPipeline p) {
            System.out.println("Nice builder " + p.node);
        }

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void process(ConfigSite cs, LinkedHashMap<Key<?>, ServiceEntry<?>> newServices) {
            newServices.put(key, new SingletonRuntimeEntry(cs, key, null, instance));
        }

        @Override
        public String toString() {
            return "provideInstant(\"" + instance + "\")";
        }
    }

    public abstract void process(ConfigSite cs, LinkedHashMap<Key<?>, ServiceEntry<?>> newServices);
}
