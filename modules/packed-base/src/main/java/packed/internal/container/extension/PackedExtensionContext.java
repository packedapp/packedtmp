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
import java.util.function.Function;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.config.ConfigSite;
import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionContext;
import app.packed.container.extension.ExtensionNode;
import app.packed.util.Nullable;
import packed.internal.access.SharedSecrets;
import packed.internal.container.PackedContainerConfiguration;

/** The default implementation of {@link ExtensionContext} with addition data only available from inside this module. */
public final class PackedExtensionContext implements ExtensionContext {

    /** The extension instance this context wraps. */
    private final Extension extension;

    /** Whether or not the extension is configurable. */
    private boolean isConfigurable = true;

    /** Any extension node the extension might have. */
    @Nullable
    private ExtensionNode<?> node;

    /** The configuration of the container the extension is registered in. */
    public final PackedContainerConfiguration pcc;

    public final ExtensionModel<?> model;

    /**
     * Creates a new extension context.
     * 
     * @param pcc
     *            the configuration of the container the extension is registered in
     * @param model
     *            the extension model
     */
    private PackedExtensionContext(PackedContainerConfiguration pcc, ExtensionModel<?> model) {
        this.pcc = requireNonNull(pcc);
        this.model = model;
        this.extension = requireNonNull(model.newInstance());
    }

    /** {@inheritDoc} */
    @Override
    public ArtifactBuildContext buildContext() {
        return pcc.buildContext();
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (!isConfigurable) {
            throw new IllegalStateException("This extension (" + extension.getClass().getSimpleName() + ") is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite containerConfigSite() {
        return pcc.configSite();
    }

    /**
     * Returns the extension instance this context wraps.
     * 
     * @return the extension instance this context wraps
     */
    public Extension extension() {
        return extension;
    }

    @Nullable
    public ExtensionNode<?> extensionNode() {
        return node;
    }

    /**
     * 
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void initialize() {
        // Sets Extension.context = this
        SharedSecrets.extension().setExtensionContext(extension, this);

        // Run any onAdd action that has set via ExtensionComposer#onAdd().
        if (model.onAdd != null) {
            model.onAdd.accept(extension);
        }

        if (model.nodeFactory != null) {
            node = (ExtensionNode<?>) ((Function) model.nodeFactory).apply(extension);

            // Need to do checks
            // ExtensionNodeModel node = context.model.node();
            // ExtensionNode<?> en = null;
            // if (node != null) {
            // en = ((ComposableExtension<?>) e).node();
            // if (en == null) {
            // throw new ExtensionDeclarationException(StringFormatter.format(e.getClass()) + ".node() must not return null");
            // } else if (en.getClass() != node.type) {
            // throw new ExtensionDeclarationException(StringFormatter.format(e.getClass()) + ".node() must return an (exact)
            // instance of "
            // + StringFormatter.format(node.type) + ", but returned an instance of " + StringFormatter.format(en.getClass()));
            // }
            // }
        }
        if (node == null) {
            // Check that method definition _is not_ overridden but is ExtensionNode<?>
        } else {
            // Check that method definition _is overridden but is ExtensionNode<?>
        }
    }

    public void onConfigured() {
        if (model.onConfigured != null) {
            model.onConfigured.accept(extension);
        }

        isConfigurable = false;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return pcc.use(extensionType);
    }

    /**
     * Creates a new context and instantiates the extension.
     * 
     * @param pcc
     *            the container the extension will be registered in
     * @param extensionType
     *            the type of extension to instantiate
     * @return the new extension context
     */
    public static PackedExtensionContext create(PackedContainerConfiguration pcc, Class<? extends Extension> extensionType) {
        return new PackedExtensionContext(pcc, ExtensionModel.of(extensionType));
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
