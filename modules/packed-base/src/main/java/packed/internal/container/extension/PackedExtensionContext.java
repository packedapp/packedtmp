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
package packed.internal.container.extension;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.container.InternalExtensionException;
import app.packed.lang.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.moduleaccess.ModuleAccess;

/** The default implementation of {@link ExtensionContext} with addition methods only available inside this module. */
public final class PackedExtensionContext implements ExtensionContext {

    /** The extension instance this context wraps, initialized in {@link #initialize(PackedContainerConfiguration)}. */
    @Nullable
    private Extension extension;

    /** The model of the extension. */
    private final ExtensionModel<?> extensionModel;

    /** Whether or not the extension has been configured. */
    private boolean isConfigured;

    /** The configuration of the container the extension is registered in. */
    private final PackedContainerConfiguration pcc;

    /**
     * Creates a new extension context.
     * 
     * @param pcc
     *            the configuration of the container the extension is registered in
     * @param extensionModel
     *            the extension model
     */
    public PackedExtensionContext(PackedContainerConfiguration pcc, ExtensionModel<?> extensionModel) {
        this.pcc = requireNonNull(pcc);
        this.extensionModel = requireNonNull(extensionModel);
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (isConfigured) {
            throw new IllegalStateException("This extension (" + extension().getClass().getSimpleName() + ") is no longer configurable");
        }
    }

    /**
     * Returns the configuration of the container the extension is registered in.
     * 
     * @return the configuration of the container the extension is registered in
     */
    public PackedContainerConfiguration container() {
        return pcc;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite containerConfigSite() {
        return pcc.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath containerPath() {
        return pcc.path();
    }

    /**
     * Returns the extension instance this context wraps.
     * 
     * @return the extension instance this context wraps
     * @throws IllegalStateException
     *             if trying to call this method from the constructor of the extension
     */
    public Extension extension() {
        Extension e = extension;
        if (e == null) {
            throw new IllegalStateException("Cannot call this method from the constructor of the extension");
        }
        return e;
    }

    /**
     * Initializes the extension.
     * 
     * @param pcc
     */
    public void initialize(PackedContainerConfiguration pcc) {
        // Sets Extension.context = this
        this.extension = extensionModel.newInstance(this);
        ModuleAccess.extension().setExtensionContext(extension, this);

        // Run any onAdd action that has set via ExtensionComposer#onAdd().
        PackedExtensionContext existing = pcc.activeExtension;
        try {
            pcc.activeExtension = this;
            if (extensionModel.onAdd != null) {
                extensionModel.onAdd.accept(extension);
            }

            if (pcc.wireletContext != null) {
                pcc.wireletContext.initialize(this);
            }

            // Call any link callbacks
            if (extensionModel.onLinkage != null) {
                // First link any children
                ArrayList<PackedContainerConfiguration> containers = pcc.containers;
                if (containers != null) {
                    for (PackedContainerConfiguration child : containers) {
                        PackedExtensionContext e = child.getExtension(extensionModel.extensionType);
                        if (e != null) {
                            extensionModel.onLinkage.accept(extension, e.extension);
                        }
                    }
                }

                // Second link any parent
                if (pcc.parent instanceof PackedContainerConfiguration) {
                    PackedContainerConfiguration p = (PackedContainerConfiguration) pcc.parent;
                    PackedExtensionContext e = p.getExtension(extensionModel.extensionType);
                    // set activate extension???
                    if (e != null) {
                        extensionModel.onLinkage.accept(e.extension, extension);
                    }
                }
            }

            // Registers this context with the artifact build context.
            // In order to compute a total order among dependencies when
            // processing the extensions
            pcc.artifact().usesExtension(this);
        } finally {
            pcc.activeExtension = existing;
        }
    }

    /**
     * Returns the model of the extension.
     * 
     * @return the model of the extension
     */
    public ExtensionModel<?> model() {
        return extensionModel;
    }

    /** Invoked by the container configuration, whenever the extension is configured. */
    public void onConfigured() {
        if (extensionModel.onConfigured != null) {
            extensionModel.onConfigured.accept(extension);
        }
        isConfigured = true;
    }

    /**
     * Returns the type of extension this context wraps.
     * 
     * @return the type of extension this context wraps
     */
    public Class<? extends Extension> type() {
        return extensionModel.extensionType;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");

        // We allow an extension to use itself, alternative would be to throw an IAE, but why?
        if (extensionType == extension.getClass()) {
            return (T) extension;
        }

        // We need to check whether or not the extension is allowed to use the specified extension every time.
        // An alternative would be to cache it in a map for each extension.
        // However this would incur extra memory usage. And if we only request an extension once
        // There would be significant overhead to instantiating a new map and caching the extension.
        // A better solution is that each extension caches the extensions they use (if they want to).
        // This saves a check + map lookup for each additional request.
        if (!extensionModel.dependencies.contains(extensionType)) {
            throw new InternalExtensionException("The specified extension type is not among " + extensionModel.extensionType.getSimpleName()
                    + " dependencies, extensionType = " + extensionType + ", valid dependencies = " + extensionModel.dependencies);
        }
        return (T) pcc.useExtension(extensionType, this).extension;
    }
}
