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

import java.util.ArrayList;

import app.packed.component.Realm;
import app.packed.container.Extension;
import packed.internal.bean.BeanSetup;
import packed.internal.inject.ExtensionInjectionManager;

/**
 * A single instance of this class exists per extension per application.
 * <p>
 * Since all extensions that are used throughout an application is always installed in the root container.
 * <p>
 * The actual tree is maintained in {@link ExtensionSetup}. This class just holds the root
 */
public final class ExtensionTreeSetup extends RealmSetup {

    /** A model of the extension/ */
    public final ExtensionModel extensionModel;

    /** The extension in the root container. */
    public final ExtensionSetup root;

    private final ArrayList<BeanSetup> beans = new ArrayList<>();

    // Tror vi skal have en liste af alle extension beans...

    ExtensionTreeSetup(ExtensionSetup root, Class<? extends Extension<?>> extensionType) {
        this.extensionModel = ExtensionModel.of(extensionType);
        this.root = requireNonNull(root);
    }

    public ExtensionInjectionManager injectionManagerFor(BeanSetup bean) {
        beans.add(bean);
        return bean.parent.extensions.get(realmType()).injectionManager;
    }

    void close() {
        root.onApplicationClose();

        for (BeanSetup bs : beans) {
            System.out.println(bs);
        }

        isClosed = true;
    }

    /** {@inheritDoc} */
    @Override
    public Realm realm() {
        return Realm.extension(realmType());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> realmType() {
        return extensionModel.type();
    }
}
