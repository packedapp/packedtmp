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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import app.packed.container.Extension;
import app.packed.container.Realm;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.inject.ExtensionInjectionManager;

/**
 * A single instance of this class exists per extension per application.
 * <p>
 * Since all extensions that are used throughout an application is always installed in the root container.
 * <p>
 * The actual tree is maintained in {@link ExtensionSetup}. This class just holds the root
 */
public final class ExtensionRealmSetup extends RealmSetup {

    /** A model of the extension/ */
    final ExtensionModel extensionModel;

    /** The extension setup for the root container. */
    private final ExtensionSetup rootExtension;

    private final ArrayList<BeanSetup> beans = new ArrayList<>();

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
    ExtensionRealmSetup(ExtensionSetup root, Class<? extends Extension<?>> extensionType) {
        this.extensionModel = ExtensionModel.of(extensionType);
        this.rootExtension = requireNonNull(root);
    }

    public ExtensionInjectionManager injectionManagerFor(BeanSetup bean) {
        beans.add(bean);
        return bean.parent.extensions.get(realmType()).injectionManager;
    }

    /** Closes the extension for configuration */
    void close() {
        // Let the extension do their final stuff
        rootExtension.onApplicationClose();
        super.close();
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
