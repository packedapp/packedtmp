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
package packed.internal.inject.build.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;

import app.packed.container.extension.ExtensionWirelet;
import app.packed.inject.ServiceDescriptor;
import app.packed.inject.ServiceWirelets;
import app.packed.util.Key;
import packed.internal.inject.build.InjectionPipeline;

/** The common superclass for upstream service wirelets. */
public abstract class PackedDownstreamInjectionWirelet extends ExtensionWirelet<InjectionPipeline> {

    public static class FilterOnKey extends PackedDownstreamInjectionWirelet {

        final Set<Key<?>> set;

        public FilterOnKey(Set<Key<?>> set) {
            this.set = requireNonNull(set);
        }

        /** {@inheritDoc} */
        @Override
        protected void process(InjectionPipeline p) {

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
        protected void process(InjectionPipeline extension) {
            System.out.println(action);
        }
    }

    public static class ProvideConstantDownstream extends PackedDownstreamInjectionWirelet {

        final Object constant;

        final Key<?> key;

        public ProvideConstantDownstream(Key<?> key, Object constant) {
            this.key = requireNonNull(key, "key is null");
            this.constant = requireNonNull(constant, "constant is null");
        }

        /** {@inheritDoc} */
        @Override
        protected void process(InjectionPipeline p) {
            System.out.println("Nice builder " + p.ib);
        }
    }
}
