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
package internal.app.packed.container;

import app.packed.container.Realm;
import app.packed.extension.Extension;

/**
 * A single instance of this class exists per extension per application. And is used to have a single point. Where we
 * can close the extension.
 * <p>
 * Since all extensions that are used throughout an application is always installed in the root container.
 */
public final class ExtensionTreeSetup extends RealmSetup {

    /** A model of the extension. */
    final ExtensionModel extensionModel;

    /** Whether or not this type of extension is still configurable. */
    private boolean isDone;

    /** The root extension. */
    private final ExtensionSetup root;

    /**
     * Creates a new realm.
     * <p>
     * This constructor is called from the constructor of the specified root
     * 
     * @param root
     *            the root extension
     * @param extensionType
     *            the type of extension
     */
    ExtensionTreeSetup(ExtensionSetup root, Class<? extends Extension<?>> extensionType) {
        this.extensionModel = ExtensionModel.of(extensionType);
        this.root = root;
    }

    void close() {
        this.isDone = true;
        root.close();
    }

    /** {@return whether or not the realm is closed.} */
    @Override
    public boolean isDone() {
        return isDone;
    }

    /** {@inheritDoc} */
    @Override
    public Realm realm() {
        return extensionModel.realm();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> realmType() {
        return extensionModel.type();
    }
}
