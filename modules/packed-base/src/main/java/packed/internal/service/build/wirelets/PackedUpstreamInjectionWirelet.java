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

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.service.ServiceDescriptor;
import app.packed.service.ServiceWirelets;
import app.packed.util.Key;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceWireletPipeline;
import packed.internal.service.build.service.ProvideAllFromInjector;

/** The common superclass for upstream service wirelets. */
public abstract class PackedUpstreamInjectionWirelet extends ServiceWirelet {

    /**
     * Processes an imported injector.
     * 
     * @param ii
     *            the imported injector to process
     */
    public abstract void process(ProvideAllFromInjector ii);

    public static class FilterOnKey extends PackedUpstreamInjectionWirelet {

        final Set<Key<?>> set;

        public FilterOnKey(Set<Key<?>> set) {
            this.set = requireNonNull(set);
        }

        /** {@inheritDoc} */
        @Override
        public void process(ProvideAllFromInjector ii) {
            for (Key<?> key : set) {
                ii.entries.remove(key);
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceWireletPipeline p) {

        }
    }

    // Transform/map -> Replaces
    // Extract -> does not remove existing item..
    public static class ApplyFunctionUpstream extends PackedUpstreamInjectionWirelet {

        final Function<?, ?> function;
        final Key<?> frpm;
        final Key<?> to;
        final boolean extract;

        public ApplyFunctionUpstream(Key<?> key, Key<?> to, Function<?, ?> function, boolean extract) {
            this.frpm = requireNonNull(key, "key is null");
            this.function = requireNonNull(function);
            this.to = requireNonNull(to);
            this.extract = extract;
        }

        /** {@inheritDoc} */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public void process(ProvideAllFromInjector ii) {
            if (ii.entries.containsKey(to)) {
                throw new RuntimeException();
            }
            // We map, not alias...
            BuildEntry<?> e = extract ? ii.entries.get(frpm) : ii.entries.remove(frpm);
            if (e == null) {
                // FAIL -> WireletProcessingException????
                throw new RuntimeException();
            }
            BuildEntry newE = new MappingBuildEntry(ii.node, e, to, function, ii.configSite);
            ii.entries.put(to, newE);
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceWireletPipeline p) {
            // Kan vi smide ProvideAllFromInjector in i pipelinen???

            throw new UnsupportedOperationException();
        }
    }

    /** A wirelet for {@link ServiceWirelets#peekUpstream(Consumer)}. */
    public static class PeekUpstream extends PackedUpstreamInjectionWirelet {

        /** The peek action to execute. */
        private final Consumer<? super ServiceDescriptor> action;

        /**
         * Creates a new upstream peek wirelet.
         * 
         * @param action
         *            the peek action to execute
         */
        public PeekUpstream(Consumer<? super ServiceDescriptor> action) {
            this.action = requireNonNull(action, "action is null");
        }

        /** {@inheritDoc} */
        @Override
        public void process(ProvideAllFromInjector ii) {
            for (BuildEntry<?> e : ii.entries.values()) {
                action.accept(e.toDescriptor());
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceWireletPipeline extension) {
            // TODO Auto-generated method stub
        }
    }
}
