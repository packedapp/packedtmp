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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import app.packed.component.UserOrExtension;
import app.packed.extension.Extension;

/**
 * A single instance of this class exists per extension per application.
 * <p>
 * Since all extensions that are used throughout an application is always installed in the root container.
 * 
 * 
 */
public final class ExtensionTreeSetup extends RealmSetup {

    /** A model of the extension/ */
    public final ExtensionModel extensionModel;

    /** The extension in the root container. */
    public final ExtensionSetup root;

    ExtensionTreeSetup(ExtensionSetup root, Class<? extends Extension<?>> extensionType) {
        this.extensionModel = ExtensionModel.of(extensionType);
        this.root = requireNonNull(root);
    }

    void close() {
        root.onApplicationClose();
        isClosed = true;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> realmType() {
        return extensionModel.type();
    }
    
    public Class<? extends Extension<?>> extensionType() {
        return extensionModel.type();
    }

    /** {@inheritDoc} */
    @Override
    public UserOrExtension owner() {
        return UserOrExtension.extension(extensionModel.type());
    }
}
