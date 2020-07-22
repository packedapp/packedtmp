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
package packed.internal.moduleaccess;

import app.packed.container.ContainerBundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.WireletPipeline;
import packed.internal.container.WireletPipelineContext;

/** A support class for calling package private methods in the app.packed.container package. */
public interface AppPackedContainerAccess extends SecretAccess {

    /**
     * Calls the doConfigure method in {@link ContainerBundle}.
     * 
     * @param bundle
     *            the bundle to configure
     * @param configuration
     *            the configuration of the container
     */
    void bundleConfigure(ContainerBundle bundle, ContainerConfiguration configuration);

    /**
     * Initializes the extension.
     * 
     * @param context
     *            the extension context containing the extension
     */
    void extensionSetConfiguration(Extension extension, ExtensionConfiguration context);

    void pipelineInitialize(WireletPipeline<?, ?> pipeline, WireletPipelineContext context);
}
