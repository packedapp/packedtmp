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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import app.packed.component.BaseComponentConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.component.feature.FeatureMap;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerSource;
import app.packed.container.Extension;
import app.packed.lang.Nullable;
import packed.internal.artifact.BuildOutput;
import packed.internal.artifact.PackedArtifactBuildContext;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.container.ContainerWirelet.ComponentNameWirelet;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.PackedExtensionContext;
import packed.internal.hook.applicator.DelayedAccessor;

/** A common superclass for all component configuration classes. */
public abstract class AbstractComponentConfiguration<T> implements ComponentHolder, BaseComponentConfiguration {

    /** The artifact this component is a part of. */
    private final PackedArtifactBuildContext artifact;

    /** Any children of this component (lazily initialized), in order of insertion. */
    @Nullable
    protected LinkedHashMap<String, AbstractComponentConfiguration<?>> children;

    /** The configuration site of this component. */
    private final ConfigSite configSite;

    /** The container this component belongs to, or null for the top level container. */
    @Nullable
    public final PackedContainerConfiguration container;

    /** Ugly stuff. */
    public ArrayList<DelayedAccessor> del = new ArrayList<>();

    /** The depth of the component in the hierarchy (including any parent artifacts). */
    private final int depth;

    /** The description of the component. */
    @Nullable
    private String description;

    /** Any extension this component belongs to. */
    @Nullable
    private final PackedExtensionContext extension;

    /** Annoying features. */
    private final FeatureMap features = new FeatureMap();

    /** The name of the component. */
    @Nullable
    public String name;

    /** The parent of this component, or null for the top level container. */
    @Nullable
    public final AbstractComponentConfiguration<?> parent;

    /** The state of this configuration. */
    protected State state = State.INITIAL;

    /**
     * Creates a new abstract component configuration
     * 
     * @param configSite
     *            the configuration site of the component
     * @param parent
     *            the parent of the component
     */
    protected AbstractComponentConfiguration(ConfigSite configSite, AbstractComponentConfiguration<?> parent) {
        this.configSite = requireNonNull(configSite);
        this.parent = requireNonNull(parent);
        this.depth = parent.depth() + 1;
        this.artifact = parent.artifact;
        this.container = parent instanceof PackedContainerConfiguration ? (PackedContainerConfiguration) parent : parent.container;
        this.extension = container.activeExtension;
    }

    /**
     * A special constructor for the top level container.
     * 
     * @param configSite
     *            the configuration site of the component
     * @param output
     *            the output of the build process
     */
    protected AbstractComponentConfiguration(ConfigSite configSite, BuildOutput output) {
        this.configSite = requireNonNull(configSite);
        this.parent = null;
        this.container = null;
        this.depth = 0;
        this.extension = null;
        this.artifact = new PackedArtifactBuildContext((PackedContainerConfiguration) this, output);
    }

    /**
     * Adds the specified child to this component.
     * 
     * @param child
     *            the child to add
     */
    protected final void addChild(AbstractComponentConfiguration<?> child) {
        LinkedHashMap<String, AbstractComponentConfiguration<?>> c = children;
        if (c == null) {
            c = children = new LinkedHashMap<>();
        }
        requireNonNull(child.name);
        children.put(child.name, child);
    }

    /**
     * Returns the artifact this component is a part of.
     * 
     * @return the artifact this component is a part of
     */
    public final PackedArtifactBuildContext artifact() {
        return artifact;
    }

    /** {@inheritDoc} */
    @Override
    public final void checkConfigurable() {
        if (state == State.FINAL) {
            throw new IllegalStateException("This component can no longer be configured");
        }
    }

    /** {@inheritDoc} */
    @Override
    public final ConfigSite configSite() {
        return configSite;
    }

    /**
     * Returns the container this component is a part of. Or null if this component is the top level container.
     * 
     * @return the container this component is a part of
     */
    @Nullable
    public final PackedContainerConfiguration container() {
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public final int depth() {
        return depth;
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<Class<? extends Extension>> extension() {
        return extension == null ? Optional.empty() : extension.model().optional;
    }

    protected void extensionsPrepareInstantiation(PackedArtifactInstantiationContext ic) {
        if (children != null) {
            for (AbstractComponentConfiguration<?> acc : children.values()) {
                if (artifact == acc.artifact) {
                    acc.extensionsPrepareInstantiation(ic);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final FeatureMap features() {
        return features;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public final String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        return initializeName(State.GET_NAME_INVOKED, null);
    }

    final Map<String, AbstractComponent> initializeChildren(AbstractComponent parent, PackedArtifactInstantiationContext ic) {
        if (children == null) {
            return null;
        }
        // Hmm, we should probably used LinkedHashMap to retain order.
        // It just uses so much memory...
        HashMap<String, AbstractComponent> result = new HashMap<>(children.size());
        for (AbstractComponentConfiguration<?> acc : children.values()) {
            AbstractComponent ac = acc.instantiate(parent, ic);
            result.put(ac.name(), ac);
        }
        return Map.copyOf(result);
    }

    public final String initializeName(State state, String setName) {
        String n = name;
        if (n != null) {
            return n;
        }
        n = setName;
        if (this instanceof PackedContainerConfiguration) {
            PackedContainerConfiguration pcc = (PackedContainerConfiguration) this;
            if (pcc.wireletContext != null) {
                n = pcc.wireletContext.name();
            }
        }

        boolean isFree = false;
        if (n == null) {
            n = initializeNameDefaultName();
            isFree = true;
        } else if (n.endsWith("?")) {
            n = n.substring(0, n.length() - 1);
            isFree = true;
        }

        if (parent != null && parent.children != null && parent.children.containsKey(n)) {
            if (!isFree) {
                throw new RuntimeException("Name already exist " + n);
            }
            int counter = 1;
            String prefix = n;
            do {
                n = prefix + counter++;
            } while (parent.children.containsKey(n));
        }
        this.state = state;
        return this.name = n;
    }

    private final String initializeNameDefaultName() {
        if (this instanceof PackedContainerConfiguration) {
            // I think try and move some of this to ComponentNameWirelet
            @Nullable
            Class<? extends ContainerSource> source = ((PackedContainerConfiguration) this).sourceType();
            if (Bundle.class.isAssignableFrom(source)) {
                String nnn = source.getSimpleName();
                if (nnn.length() > 6 && nnn.endsWith("Bundle")) {
                    nnn = nnn.substring(0, nnn.length() - 6);
                }
                if (nnn.length() > 0) {
                    // checkName, if not just App
                    // TODO need prefix
                    return nnn;
                }
                if (nnn.length() == 0) {
                    return "Container";
                }
            }
            // TODO think it should be named Artifact type, for example, app, injector, ...
            return "Unknown";
        } else {
            return ((AbstractCoreComponentConfiguration<?>) this).componentModel.defaultPrefix();
        }

    }

    protected abstract AbstractComponent instantiate(AbstractComponent parent, PackedArtifactInstantiationContext ic);

    /**
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    @Override
    public final ComponentPath path() {
        initializeName(State.PATH_INVOKED, null);
        return PackedComponentPath.of(this); // show we weak intern them????
    }

    /** {@inheritDoc} */
    @Override
    public AbstractComponentConfiguration<T> setDescription(String description) {
        requireNonNull(description, "description is null");
        checkConfigurable();
        this.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public AbstractComponentConfiguration<T> setName(String name) {
        ComponentNameWirelet.checkName(name);
        switch (state) {
        case INITIAL:
            initializeName(State.SET_NAME_INVOKED, name);
            return this;
        case FINAL:
            checkConfigurable();
        case GET_NAME_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via call to #getName()");
        case EXTENSION_USED:
            throw new IllegalStateException("Cannot call #setName(String) after any extensions has has been installed");
        case PATH_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via call to #path()");
        case INSTALL_INVOKED:
            throw new IllegalStateException("Cannot call this method after having installed components");
        case LINK_INVOKED:
            throw new IllegalStateException("Cannot call this method after #link() has been invoked");
        case SET_NAME_INVOKED:
            throw new IllegalStateException("#setName(String) can only be called once");
        }
        throw new InternalError();
    }

    /** The state of the component configuration */
    // FirstAction???
    public enum State {

        /** The initial state. */
        EXTENSION_USED,

        /** */
        FINAL,

        /** {@link ComponentConfiguration#getName()} or {@link ContainerConfiguration#getName()} has been invoked. */
        GET_NAME_INVOKED,

        /** The initial state. */
        INITIAL,

        /** One of the install component methods has been invoked. */
        INSTALL_INVOKED,

        /** {@link ContainerConfiguration#link(Bundle, app.packed.container.Wirelet...)} has been invoked. */
        LINK_INVOKED,

        /** One of the install component methods has been invoked. */
        PATH_INVOKED,

        /** Set name has been invoked. */
        SET_NAME_INVOKED;
    }
}
