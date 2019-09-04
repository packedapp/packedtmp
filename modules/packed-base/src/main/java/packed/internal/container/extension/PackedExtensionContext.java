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

import java.util.IdentityHashMap;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.config.ConfigSite;
import app.packed.container.WireletList;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionContext;
import packed.internal.access.SharedSecrets;
import packed.internal.container.PackedContainerConfiguration;

/**
 *
 */
// Overvejer at lave ExtensionContext...
// Er isaer nyttigt ved komplicered plugins, hvor noget af funktionaliteten
// bliver lagt ud i support klasser, som ikke kan kalde protected metoder paa extension
public final class PackedExtensionContext implements ExtensionContext {

    /** The extension this context wraps. */
    public final Extension extension;

    /** Whether or not the extension is configurable. */
    private boolean isConfigurable = true;

    final PackedContainerConfiguration pcc;

    private PackedExtensionContext(PackedContainerConfiguration pcc, Extension extension) {
        this.pcc = requireNonNull(pcc);
        this.extension = requireNonNull(extension);
    }

    public ArtifactBuildContext buildContext() {
        return pcc.buildContext();
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (!isConfigurable) {
            throw new IllegalStateException("This extension " + extension.getClass().getSimpleName() + " is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite containerConfigSite() {
        return pcc.configSite();
    }

    /**
     * 
     */
    public void onConfigured() {
        SharedSecrets.extension().onConfigured(extension);
        isConfigurable = false;
    }

    public void putIntoInstantiationContext(ArtifactInstantiationContext context, Object sidecar) {
        context.put(pcc, sidecar);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return pcc.use(extensionType);
    }

    public WireletList wirelets() {
        return pcc.wirelets();
    }

    public static PackedExtensionContext create(Class<? extends Extension> extensionType, PackedContainerConfiguration pcc) {
        Extension e = ExtensionModel.newInstance(extensionType, pcc);
        return new PackedExtensionContext(pcc, e);
    }

    static class DefCon {
        // Det her med at finde ud af hvordan extensions er relateret....

        static final Module m = DefCon.class.getModule();

        // Eneste problem med Base Extensions er at vi skal predetermind en order
        // Og denne order skal maaske ogsaa vise sig i cc.extension()
        // Eftersom det er et view. Kraever det en lille smule extra kode...

        IdentityHashMap<Class<?>, Extension> baseExtensions;

        IdentityHashMap<Class<?>, Extension> externalExtensions;

        public <T> T use(Class<T> t) {
            if (t.getModule() == m) {
                // get From baseExtensions
            } else {
                // getFrom External Dependencies..
            }
            // We can actually remove all modules that do not implement #configure()

            return null;
            // ideen er selvfolgelig
        }
    }

}
