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
package packed.internal.inject.builder;

import app.packed.bundle.Bundle;
import app.packed.bundle.BundleDescriptor;
import app.packed.bundle.ContainerBuildContext;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.ServiceNode;

/**
 *
 */
public class InternalBundleDescriptor {

    /**
     * @param bundle
     * @return
     */
    public static BundleDescriptor.Builder of(Bundle bundle) {
        InternalConfigurationSite ics = InternalConfigurationSite.ofStack(ConfigurationSiteType.BUNDLE_DESCRIPTOR_OF);
        InjectorBuilder conf = new InjectorBuilder(ics, bundle);

        ContainerBuildContext bs = new ContainerBuildContext() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T with(Class<? super T> type) {
                if (type == InjectorBuilder.class) {
                    return (T) conf;
                }
                return super.with(type);
            }
        };
        bs.configure(bundle);

        // BundleSupport.invoke().configureInjectorBundle((InjectorBundle) bundle, conf, false);

        DependencyGraph injectorBuilder = new DependencyGraph(conf);
        injectorBuilder.analyze(conf);

        //////////////// Create the builder
        BundleDescriptor.Builder builder = new BundleDescriptor.Builder(bundle.getClass());
        builder.setBundleDescription(conf.getDescription());// Nahh, this is the runtime description

        for (ServiceNode<?> n : conf.privateNodeMap) {
            if (n instanceof ServiceBuildNode) {
                builder.addServiceDescriptor(((ServiceBuildNode<?>) n).toDescriptor());
            }
        }

        for (ServiceBuildNode<?> n : conf.publicNodeList) {
            if (n instanceof ServiceBuildNodeExposed) {
                builder.contract().services().addProvides(n.getKey());
            }
        }

        if (conf.box.services().requiredServicesOptionally != null) {
            if (conf.box.services().requiredServicesMandatory != null) {
                conf.box.services().requiredServicesOptionally.removeAll(conf.box.services().requiredServicesMandatory);// cannot both be mandatory and optional
            }
            conf.box.services().requiredServicesOptionally.forEach(k -> builder.contract().services().addOptional(k));
        }
        if (conf.box.services().requiredServicesMandatory != null) {
            conf.box.services().requiredServicesMandatory.forEach(k -> builder.contract().services().addRequires(k));
        }
        return builder;
    }
}
