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

import java.util.List;
import java.util.Optional;

import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.container.ExtensionWirelet;

/** A support class for calling package private methods in the app.packed.extension package. */
public interface AppPackedExtensionAccess extends SecretAccess {

    void pipelineInitialize(Optional<ExtensionWirelet.Pipeline<?, ?, ?>> previous, List<?> wirelets, ExtensionWirelet.Pipeline<?, ?, ?> pipeline);

    /**
     * Initializes the extension.
     * 
     * @param context
     *            the extension context containing the extension
     */
    void setExtensionContext(Extension extension, ExtensionContext context);
}
