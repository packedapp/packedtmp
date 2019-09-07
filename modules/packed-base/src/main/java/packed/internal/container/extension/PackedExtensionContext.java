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

/** The default implementation of {@link ExtensionContext}. */
public final class PackedExtensionContext implements ExtensionContext {

    /** The extension this context wraps. */
    private final Extension extension;

    /** Whether or not the extension is configurable. */
    private boolean isConfigurable = true;

    /** The configuration of the container the extension is registered in. */
    public final PackedContainerConfiguration pcc;

    /**
     * @param pcc
     *            the configuration of the container the extension is registered in
     * @param extension
     *            the extension to wrap
     */
    private PackedExtensionContext(PackedContainerConfiguration pcc, Extension extension) {
        this.pcc = requireNonNull(pcc);
        this.extension = requireNonNull(extension);
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactBuildContext buildContext() {
        return pcc.buildContext();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite containerConfigSite() {
        return pcc.configSite();
    }

    /**
     * Returns the extension this context wraps.
     * 
     * @return the extension this context wraps
     */
    public Extension extension() {
        return extension;
    }

    public void onConfigured() {
        SharedSecrets.extension().onConfigured(extension);
        isConfigurable = false;
    }

    /** {@inheritDoc} */
    @Override
    public void putIntoInstantiationContext(ArtifactInstantiationContext context, Object sidecar) {
        context.put(pcc, sidecar);
    }

    /** {@inheritDoc} */
    @Override
    public void requireConfigurable() {
        if (!isConfigurable) {
            throw new IllegalStateException("This extension " + extension.getClass().getSimpleName() + " is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return pcc.use(extensionType);
    }

    @Override
    public WireletList wirelets() {
        return pcc.wirelets();
    }

    /**
     * Creates a new extension and its context.
     * 
     * @param pcc
     *            the container the extension will be registered in
     * @param extensionType
     *            the type of extension to create
     * @return the new context
     */
    public static PackedExtensionContext create(PackedContainerConfiguration pcc, Class<? extends Extension> extensionType) {
        return new PackedExtensionContext(pcc, ExtensionModel.newInstance(extensionType, pcc));
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
