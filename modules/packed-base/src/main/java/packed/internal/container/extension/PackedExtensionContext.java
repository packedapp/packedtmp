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
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionContext;
import app.packed.container.extension.ExtensionDeclarationException;
import app.packed.util.Key;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.module.ModuleAccess;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.build.service.RuntimeAdaptorEntry;
import packed.internal.service.run.SingletonRuntimeEntry;

/** The default implementation of {@link ExtensionContext} with addition data only available from inside this module. */
public final class PackedExtensionContext implements ExtensionContext {

    /** The extension instance this context wraps. */
    private final Extension extension;

    /** Whether or not the extension is configurable. */
    private boolean isConfigurable = true;

    /** The model of the extension. */
    public final ExtensionModel<?> model;

    /** The configuration of the container the extension is registered in. */
    public final PackedContainerConfiguration pcc;

    private RuntimeAdaptorEntry<? extends Extension> serviceEntry;

    /**
     * Creates a new extension context.
     * 
     * @param pcc
     *            the configuration of the container the extension is registered in
     * @param model
     *            the extension model
     */
    public PackedExtensionContext(PackedContainerConfiguration pcc, ExtensionModel<?> model) {
        this.pcc = requireNonNull(pcc);
        this.model = model;
        this.extension = requireNonNull(model.newInstance());
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (!isConfigurable) {
            throw new IllegalStateException("This extension (" + extension.getClass().getSimpleName() + ") is no longer configurable");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BuildEntry<? extends Extension> serviceEntry(ServiceExtensionNode sen) {
        RuntimeAdaptorEntry<? extends Extension> e = serviceEntry;
        if (e == null) {
            e = serviceEntry = new RuntimeAdaptorEntry(sen, new SingletonRuntimeEntry<Extension>(ConfigSite.UNKNOWN, (Key) Key.of(type()), null, extension));
        }
        return e;
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
     */
    public Extension extension() {
        return extension;
    }

    public Class<? extends Extension> type() {
        return model.extensionType;
    }

    /**
     * Initializes the extension.
     * 
     * @param pcc
     */
    public void initialize(PackedContainerConfiguration pcc) {
        // Sets Extension.context = this
        ModuleAccess.extension().setExtensionContext(extension, this);

        // Run any onAdd action that has set via ExtensionComposer#onAdd().
        PackedExtensionContext existing = pcc.activeExtension;
        try {
            pcc.activeExtension = this;
            if (model.onAdd != null) {
                model.onAdd.accept(extension);
            }

            if (pcc.wireletContext != null) {
                pcc.wireletContext.initialize(this);
            }

            // Call any link callbacks
            if (model.onLinkage != null) {
                // First link any children
                ArrayList<PackedContainerConfiguration> containers = pcc.containers;
                if (containers != null) {
                    for (PackedContainerConfiguration child : containers) {
                        PackedExtensionContext e = child.getExtension(model.extensionType);
                        if (e != null) {
                            model.onLinkage.accept(extension, e.extension);
                        }
                    }
                }

                // Second link any parent
                if (pcc.parent instanceof PackedContainerConfiguration) {
                    PackedContainerConfiguration p = (PackedContainerConfiguration) pcc.parent;
                    PackedExtensionContext e = p.getExtension(model.extensionType);
                    // set activate extension???
                    if (e != null) {
                        model.onLinkage.accept(e.extension, extension);
                    }
                }
            }

        } finally {
            pcc.activeExtension = existing;
        }
    }

    public void onConfigured() {
        if (model.onConfigured != null) {
            model.onConfigured.accept(extension);
        }
        isConfigurable = false;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        requireNonNull(extensionType, "extensionType is null");

        // Unfortunately, we need to check this every time.
        // An alternative would be to cache it in a map for each extension.
        // However this would incur extra memory usage. And if we only request an extension once
        // There would be significant overhead to instantiating a new map and caching the extension.
        // A better solution is that each extension caches the extensions if they want to.
        // This saves a check + map lookup for each additional request.
        if (!model.dependencies.contains(extensionType)) {
            throw new ExtensionDeclarationException("The specified extension type is not among " + model.extensionType.getSimpleName()
                    + " dependencies, extensionType = " + extensionType + ", valid dependencies = " + model.dependencies);
        }
        // TODO check extensionType is in valid types...
        // We actually need to make this check every time...
        // Otherwise it could use extensions that someone else has installed.
        // But itself is not allowed to access

        PackedExtensionContext pec = pcc.useExtension(this, extensionType);
        return (T) pec.extension;
    }
}

// /**
// * Creates a new context and instantiates the extension.
// *
// * @param pcc
// * the container the extension will be registered in
// * @param extensionType
// * the type of extension to instantiate
// * @return the new extension context
// */
// public static PackedExtensionContext create(PackedContainerConfiguration pcc, Class<? extends Extension>
// extensionType) {
// return new PackedExtensionContext(pcc, ExtensionModel.of(extensionType));
// }
// static class DefCon {
// // Det her med at finde ud af hvordan extensions er relateret....
//
// static final Module m = DefCon.class.getModule();
//
// // Eneste problem med Base Extensions er at vi skal predetermind en order
// // Og denne order skal maaske ogsaa vise sig i cc.extension()
// // Eftersom det er et view. Kraever det en lille smule extra kode...
//
// IdentityHashMap<Class<?>, Extension> baseExtensions;
//
// IdentityHashMap<Class<?>, Extension> externalExtensions;
//
// public <T> T use(Class<T> t) {
// if (t.getModule() == m) {
// // get From baseExtensions
// } else {
// // getFrom External Dependencies..
// }
// // We can actually remove all modules that do not implement #configure()
//
// return null;
// // ideen er selvfolgelig
// }
// }
