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
package packed.internal.inject.service.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.block.ExtensionMember;
import app.packed.inject.ServiceExtension;
import packed.internal.inject.service.build.ServiceBuild;
import packed.internal.inject.service.sandbox.ProvideAllFromServiceLocator;

/** The common superclass for upstream service wirelets. */
public abstract class PackedUpstreamInjectionWirelet extends OldServiceWirelet {

    /**
     * Processes an imported injector.
     * 
     * @param ii
     *            the imported injector to process
     */
    public abstract void process(ProvideAllFromServiceLocator ii);

    @ExtensionMember(ServiceExtension.class)
    public static class FilterOnKey extends PackedUpstreamInjectionWirelet {

        final Set<Key<?>> set;

        public FilterOnKey(Set<Key<?>> set) {
            this.set = requireNonNull(set);
        }

        /** {@inheritDoc} */
        @Override
        public void process(ProvideAllFromServiceLocator ii) {
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
        @Override
        public void process(ProvideAllFromServiceLocator ii) {
            if (ii.entries.containsKey(to)) {
                throw new RuntimeException();
            }
            // We map, not alias...
            ServiceBuild e = extract ? ii.entries.get(frpm) : ii.entries.remove(frpm);
            if (e == null) {
                // FAIL -> WireletProcessingException????
                throw new RuntimeException();
            }
            ServiceBuild newE = new MappingServiceBuild(ii.node, ii.configSite, e, to, function);
            ii.entries.put(to, newE);
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceExtension p) {
            // Kan vi smide ProvideAllFromInjector in i pipelinen???

            throw new UnsupportedOperationException();
        }
    }
}
