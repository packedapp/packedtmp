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

import app.packed.container.extension.ExtensionWireletPipeline;

/** The default wirelet pipeline for */
public final class ServiceWireletPipeline extends ExtensionWireletPipeline<ServiceWireletPipeline, ServiceExtensionNode> {

    /**
     * Creates a new pipeline.
     * 
     * @param node
     *            the node to create the pipeline from
     */
    ServiceWireletPipeline(ServiceExtensionNode node) {
        super(node);
    }

    /**
     * Creates a new pipeline.
     * 
     * @param previous
     *            the previous pipeline to spawn from
     */
    ServiceWireletPipeline(ServiceWireletPipeline previous) {
        super(previous.node());
    }

    /** {@inheritDoc} */
    @Override
    public ServiceWireletPipeline spawn() {
        return new ServiceWireletPipeline(node());
    }
}
