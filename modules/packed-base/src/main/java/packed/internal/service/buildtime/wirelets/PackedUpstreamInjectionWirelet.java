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

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.container.ExtensionMember;
import app.packed.service.Service;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceWirelets;
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.service.ProvideAllFromOtherInjector;

/** The common superclass for upstream service wirelets. */
public abstract class PackedUpstreamInjectionWirelet extends ServiceWirelet {

    /**
     * Processes an imported injector.
     * 
     * @param ii
     *            the imported injector to process
     */
    public abstract void process(ProvideAllFromOtherInjector ii);

    @ExtensionMember(ServiceExtension.class)
    public static class FilterOnKey extends PackedUpstreamInjectionWirelet {

        final Set<Key<?>> set;

        public FilterOnKey(Set<Key<?>> set) {
            this.set = requireNonNull(set);
        }

        /** {@inheritDoc} */
        @Override
        public void process(ProvideAllFromOtherInjector ii) {
            for (Key<?> key : set) {
                ii.entries.remove(key);
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceExtension p) {

        }
    }

    // Transform/map -> Replaces
    // Extract -> does not remove existing item..
    @ExtensionMember(ServiceExtension.class)
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
        public void process(ProvideAllFromOtherInjector ii) {
            if (ii.entries.containsKey(to)) {
                throw new RuntimeException();
            }
            // We map, not alias...
            BuildtimeService<?> e = extract ? ii.entries.get(frpm) : ii.entries.remove(frpm);
            if (e == null) {
                // FAIL -> WireletProcessingException????
                throw new RuntimeException();
            }
            BuildtimeService newE = new MappingBuildEntry(ii.node, ii.configSite, e, to, function);
            ii.entries.put(to, newE);
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceExtension p) {
            // Kan vi smide ProvideAllFromInjector in i pipelinen???

            throw new UnsupportedOperationException();
        }
    }

    /** A wirelet for {@link ServiceWirelets#peekFrom(Consumer)}. */
    @ExtensionMember(ServiceExtension.class)
    public static class PeekFrom extends PackedUpstreamInjectionWirelet {

        /** The peek action to execute. */
        private final Consumer<? super Service> action;

        /**
         * Creates a new upstream peek wirelet.
         * 
         * @param action
         *            the peek action to execute
         */
        public PeekFrom(Consumer<? super Service> action) {
            this.action = requireNonNull(action, "action is null");
        }

        /** {@inheritDoc} */
        @Override
        public void process(ProvideAllFromOtherInjector ii) {
            for (BuildtimeService<?> e : ii.entries.values()) {
                action.accept(e.toDescriptor());
            }
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceExtension extension) {
            // TODO Auto-generated method stub
        }
    }
}
