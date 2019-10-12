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

import app.packed.service.ServiceExtension;
import packed.internal.container.extension.w2.WireletListNew;
import packed.internal.container.extension.w2.XtensionPipeline;
import packed.internal.service.build.ServiceExtensionNode;

/**
 *
 */
public final class ServicePipeline extends XtensionPipeline<ServiceExtension, ServicePipeline, ServiceWirelet> {

    public final ServiceExtensionNode node;

    /**
     * @param extension
     * @param wirelets
     */
    public ServicePipeline(ServiceExtension extension, WireletListNew<ServiceWirelet> wirelets, ServiceExtensionNode node) {
        super(extension, wirelets);
        this.node = requireNonNull(node);
    }

    /**
     * @param from
     * @param wirelets
     */
    private ServicePipeline(ServicePipeline from, WireletListNew<ServiceWirelet> wirelets) {
        super(from, wirelets);
        this.node = from.node;
    }

    /** {@inheritDoc} */
    @Override
    protected ServicePipeline spawn(WireletListNew<ServiceWirelet> wirelets) {
        return new ServicePipeline(this, wirelets);
    }
}
