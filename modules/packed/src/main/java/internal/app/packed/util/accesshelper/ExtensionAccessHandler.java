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
package internal.app.packed.util.accesshelper;

import java.util.function.Supplier;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.ExtensionPointHandle;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionPointHandle;

/**
 * Access helper for Extension and related classes.
 */
public abstract class ExtensionAccessHandler extends AccessHelper {

    private static final Supplier<ExtensionAccessHandler> CONSTANT = StableValue.supplier(() -> init(ExtensionAccessHandler.class, Extension.class));

    public static ExtensionAccessHandler instance() {
        return CONSTANT.get();
    }

    /**
     * Gets the ExtensionSetup from an Extension.
     *
     * @param extension the extension
     * @return the extension setup
     */
    public abstract ExtensionSetup getExtensionHandle(Extension<?> extension);

    /**
     * Gets the PackedExtensionPointHandle from an ExtensionPoint.
     *
     * @param extensionPoint the extension point
     * @return the packed extension use site
     */
    public abstract PackedExtensionPointHandle getExtensionPointPackedExtensionUseSite(ExtensionPoint<?> extensionPoint);

    /**
     * Invokes the protected newExtensionMirror method on an Extension.
     *
     * @param extension the extension
     * @return the extension mirror
     */
    public abstract ExtensionMirror<?> invokeExtensionNewExtensionMirror(Extension<?> extension);

    /**
     * Invokes the protected onClose method on an Extension.
     *
     * @param extension the extension
     */
    public abstract void invokeExtensionOnApplicationClose(Extension<?> extension);

    /**
     * Invokes the protected onConfigured method on an Extension.
     *
     * @param extension the extension
     */
    public abstract void invokeExtensionOnAssemblyClose(Extension<?> extension);

    /**
     * Invokes the protected onNew method on an Extension.
     *
     * @param extension the extension
     */
    public abstract void invokeExtensionOnNew(Extension<?> extension);

    /**
     * Invokes the protected newExtensionPoint method on an Extension.
     *
     * @param extension the extension
     * @param usesite the use site handle
     * @return the extension point
     */
    public abstract ExtensionPoint<?> newExtensionPoint(Extension<?> extension, ExtensionPointHandle usesite);
}
