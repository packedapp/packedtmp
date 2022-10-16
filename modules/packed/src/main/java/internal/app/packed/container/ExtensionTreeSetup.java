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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.container.Extension;
import app.packed.container.UserOrExtension;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/**
 * A single instance of this class exists per extension per application.
 * <p>
 * Since all extensions that are used throughout an application is always installed in the root container.
 * <p>
 * The actual tree is maintained in {@link ExtensionSetup}. This class just holds the root
 */
public final class ExtensionTreeSetup extends RealmSetup {

    /** A handle for invoking the protected method {@link Extension#onApplicationClose()}. */
    private static final MethodHandle MH_EXTENSION_ON_APPLICATION_CLOSE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "onApplicationClose", void.class);

    /** A model of the extension. */
    final ExtensionModel extensionModel;

    /** Whether or not this realm is configurable. */
    private boolean isClosed;

    /** The extension setup for the root container. */
    private final ExtensionSetup rootExtension;

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
        this.rootExtension = requireNonNull(root);
    }

    /** Closes the extension for configuration */
    void close() {
        // Let the extension do their final stuff
//        Invokes {@link Extension#onApplicationClose()}.
        try {
            MH_EXTENSION_ON_APPLICATION_CLOSE.invokeExact(rootExtension.instance());
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }

        isClosed = true;
    }

    /** {@return whether or not the realm is closed.} */
    public final boolean isClosed() {
        return isClosed;
    }
    /** {@inheritDoc} */
    @Override
    public UserOrExtension realm() {
        return extensionModel.realm();
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension<?>> realmType() {
        return extensionModel.type();
    }
}
