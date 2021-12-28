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

import app.packed.extension.Extension;
import packed.internal.application.ApplicationSetup;

/**
 *
 */
public final class ExtensionRealmSetup extends RealmSetup {

    public final ExtensionModel extensionModel;

    /** The first extension that was added to the realm. */
    final ExtensionSetup root;

    ExtensionRealmSetup(ExtensionSetup root, ApplicationSetup application, Class<? extends Extension> extensionType) {
        this.extensionModel = ExtensionModel.of(extensionType);
        this.root = requireNonNull(root);
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> realmType() {
        return extensionModel.type();
    }
}
