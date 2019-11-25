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
package packed.internal.service.build;

import static java.util.Objects.requireNonNull;

import app.packed.container.ExtensionWirelet;
import app.packed.container.MutableWireletList;
import app.packed.service.ServiceExtension;
import packed.internal.service.build.wirelets.ServiceWirelet;

/** The default wirelet pipeline for */
public final class ServiceWireletPipeline extends ExtensionWirelet.Pipeline<ServiceExtension, ServiceWireletPipeline, ServiceWirelet> {

    public final ServiceExtensionNode node;

    /**
     * @param wirelets
     */
    public ServiceWireletPipeline(MutableWireletList<ServiceWirelet> wirelets, ServiceExtensionNode node) {
        super(wirelets);
        this.node = requireNonNull(node);
    }

    /**
     * @param from
     * @param wirelets
     */
    private ServiceWireletPipeline(ServiceWireletPipeline from, MutableWireletList<ServiceWirelet> wirelets) {
        super(from, wirelets);
        this.node = from.node;
    }

    /** {@inheritDoc} */
    @Override
    protected ServiceWireletPipeline spawn(MutableWireletList<ServiceWirelet> wirelets) {
        return new ServiceWireletPipeline(this, wirelets);
    }
}
