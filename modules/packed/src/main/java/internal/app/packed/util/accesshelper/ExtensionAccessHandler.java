/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.ExtensionPointHandle;
import internal.app.packed.extension.BaseExtensionNamespace;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionPointHandle;

/**
 * Access helper for Extension and related classes.
 */
public abstract class ExtensionAccessHandler extends AccessHelper {

    private static final Supplier<ExtensionAccessHandler> CONSTANT = StableValue.supplier(() -> init(ExtensionAccessHandler.class, Extension.class));

    /**
     * Gets the ExtensionSetup from an Extension.
     *
     * @param extension the extension
     * @return the extension setup
     */
    public abstract ExtensionSetup get_Extension_ExtensionSetup(Extension<?> extension);

    /**
     * Gets the PackedExtensionPointHandle from an ExtensionPoint.
     *
     * @param extensionPoint the extension point
     * @return the packed extension use site
     */
    public abstract PackedExtensionPointHandle get_ExtensionPoint_PackedExtensionPointHandle(ExtensionPoint<?> extensionPoint);

    /**
     * Invokes the protected newExtensionMirror method on an Extension.
     *
     * @param extension the extension
     * @return the extension mirror
     */
    public abstract ExtensionMirror<?> invoke_Extension_NewExtensionMirror(Extension<?> extension);

    /**
     * Invokes the protected newExtensionPoint method on an Extension.
     *
     * @param extension the extension
     * @param usesite the use site handle
     * @return the extension point
     */
    public abstract ExtensionPoint<?> invoke_Extension_NewExtensionPoint(Extension<?> extension, ExtensionPointHandle usesite);

    /**
     * Invokes the protected onClose method on an Extension.
     *
     * @param extension the extension
     */
    public abstract void invoke_Extension_OnApplicationClose(Extension<?> extension);

    /**
     * Invokes the protected onConfigured method on an Extension.
     *
     * @param extension the extension
     */
    public abstract void invoke_Extension_OnAssemblyClose(Extension<?> extension);

    /**
     * Invokes the protected onNew method on an Extension.
     *
     * @param extension the extension
     */
    public abstract void invoke_Extension_OnNew(Extension<?> extension);

    public abstract BaseExtension create_BaseExtension(BaseExtensionNamespace namespace, ExtensionHandle<BaseExtension> handle);

    public static ExtensionAccessHandler instance() {
        return CONSTANT.get();
    }
}
