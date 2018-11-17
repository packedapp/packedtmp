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
package packed.internal.inject.buildnodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import app.packed.bundle.Bundle;
import app.packed.bundle.InjectorBundle;
import app.packed.inject.BindingMode;
import app.packed.inject.Key;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.ConfigurationSite;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleSupport;
import packed.internal.util.configurationsite.ConfigurationSiteType;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 *
 */
public class InternalBundleDescriptor {

    public Map<Key<?>, ServiceDescriptor> exposedServices;
    public Set<Key<?>> optionalServices;
    public Set<Key<?>> requiredServices;

    /**
     * @param bundle
     * @return
     */
    public static InternalBundleDescriptor of(Bundle bundle) {
        InternalConfigurationSite ics = InternalConfigurationSite.ofStack(ConfigurationSiteType.DESCRIPTOR_OF);

        InternalInjectorConfiguration conf = new InternalInjectorConfiguration(ics, bundle);

        BundleSupport.configure((InjectorBundle) bundle, conf, false);

        InjectorBuilder ib = new InjectorBuilder(conf);
        ib.setup();

        InternalBundleDescriptor d = new InternalBundleDescriptor();

        d.requiredServices = Set.copyOf(conf.requiredServicesMandatory);
        d.optionalServices = Set.copyOf(conf.requiredServicesOptionally);
        HashMap<Key<?>, ServiceDescriptor> map = new HashMap<>();
        for (BuildNode<?> n : conf.publicExposedNodeList) {
            if (n instanceof BuildNodeExposed) {
                BuildNodeExposed<?> bne = (BuildNodeExposed<?>) n;
                ServiceDescriptor sd = new ServiceDescriptorImpl(bne);
                map.put(sd.getKey(), sd);
            }
        }
        d.exposedServices = Map.copyOf(map);
        return d;
    }

    static class ServiceDescriptorImpl implements ServiceDescriptor {
        private final BindingMode bindingMode;
        private final ConfigurationSite configurationSite;
        private final @Nullable String description;
        private final Key<?> key;

        private final Set<String> tags;

        ServiceDescriptorImpl(BuildNodeExposed<?> bne) {
            this.key = bne.getKey();
            this.tags = Set.copyOf(bne.tags());
            this.bindingMode = bne.getBindingMode();
            this.configurationSite = bne.getConfigurationSite();
            this.description = bne.getDescription();
        }

        /** {@inheritDoc} */
        @Override
        public BindingMode getBindingMode() {
            return bindingMode;
        }

        /** {@inheritDoc} */
        @Override
        public ConfigurationSite getConfigurationSite() {
            return configurationSite;
        }

        /** {@inheritDoc} */
        @Override
        public @Nullable String getDescription() {
            return description;
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> getKey() {
            return key;
        }

        /** {@inheritDoc} */
        @Override
        public Set<String> tags() {
            return tags;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[Key = " + getKey().toStringSimple());
            sb.append(", bindingMode = " + getBindingMode());
            if (!tags.isEmpty()) {
                sb.append(", tags = " + tags);
            }
            sb.append("]");
            return sb.toString();
        }
    }
}
