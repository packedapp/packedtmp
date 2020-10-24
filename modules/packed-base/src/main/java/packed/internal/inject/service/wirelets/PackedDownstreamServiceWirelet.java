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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.LinkedHashMap;
import java.util.Set;

import app.packed.base.Key;
import app.packed.block.ExtensionMember;
import app.packed.config.ConfigSite;
import app.packed.inject.ServiceExtension;
import packed.internal.inject.service.ServiceBuildManager;
import packed.internal.inject.service.runtime.ConstantRuntimeService;
import packed.internal.inject.service.runtime.RuntimeService;
import packed.internal.util.LookupUtil;

/** The common superclass for upstream service wirelets. */
public abstract class PackedDownstreamServiceWirelet extends OldServiceWirelet {

    /** A VarHandle that can access ServiceExtension#sm. */
    private static final VarHandle VH_SERVICE_EXTENSION_NODE = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), ServiceExtension.class, "sbm",
            ServiceBuildManager.class);

    /**
     * Extracts the service node from a service extension.
     * 
     * @param extension
     *            the extension to extract from
     * @return the service node
     */
    public static ServiceBuildManager fromExtension(ServiceExtension extension) {
        return (ServiceBuildManager) VH_SERVICE_EXTENSION_NODE.get(extension);
    }

    @ExtensionMember(ServiceExtension.class)
    public static class FilterOnKey extends PackedDownstreamServiceWirelet {

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
        public void process(ConfigSite cs, LinkedHashMap<Key<?>, RuntimeService> newServices) {
            newServices.keySet().removeAll(set);
        }
    }

    @ExtensionMember(ServiceExtension.class)
    public static class ProvideInstance extends PackedDownstreamServiceWirelet {

        /** The instance to provide. */
        final Object instance;

        /** The key. */
        final Key<?> key;

        public ProvideInstance(Key<?> key, Object instance) {
            this.key = requireNonNull(key, "key is null");
            this.instance = requireNonNull(instance, "instance is null");
        }

        /** {@inheritDoc} */
        @Override
        protected void process(ServiceExtension p) {}

        /** {@inheritDoc} */
        @Override
        public void process(ConfigSite cs, LinkedHashMap<Key<?>, RuntimeService> newServices) {
            newServices.put(key, new ConstantRuntimeService(cs, key, instance));
        }

        @Override
        public String toString() {
            return "provideInstant(\"" + instance + "\")";
        }
    }

    public abstract void process(ConfigSite cs, LinkedHashMap<Key<?>, RuntimeService> newServices);
}
