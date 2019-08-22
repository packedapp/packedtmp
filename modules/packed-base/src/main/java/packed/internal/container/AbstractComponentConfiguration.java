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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import app.packed.artifact.ArtifactDriver;
import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerSource;
import app.packed.feature.FeatureMap;
import app.packed.util.Nullable;
import packed.internal.container.extension.hook.DelayedAccessor;

/** A common superclass for all component configuration classes. */
public abstract class AbstractComponentConfiguration implements ComponentHolder, ComponentConfiguration {

    /** The build context of the artifact this configuration belongs to. */
    final PackedArtifactBuildContext buildContext;

    /** Any children of this component (lazily initialized), in order of insertion. */
    @Nullable
    LinkedHashMap<String, AbstractComponentConfiguration> children;

    /** The configuration site of this component. */
    private final ConfigSite configSite;

    /** The component that was last installed. */
    @Nullable
    CoreComponentConfiguration currentComponent;

    public ArrayList<DelayedAccessor> del = new ArrayList<>();

    /** The depth of the component in the hierarchy (including any parent artifacts). */
    private final int depth;

    /** The description of the component. */
    @Nullable
    private String description;

    private final FeatureMap features = new FeatureMap();

    /** Whether or not the name can be postfix'able. Useful for images only. */
    boolean isNamePostfixable = false;

    /** The name of the component. */
    @Nullable
    String name;

    /** The parent of this component, or null if a root component. */
    @Nullable
    final AbstractComponentConfiguration parent;

    /** The state of this configuration. */
    State state = State.INITIAL;

    /**
     * Creates a new abstract component configuration
     * 
     * @param configSite
     *            the configuration site of the component
     * @param parent
     *            the parent of the component
     */
    AbstractComponentConfiguration(ConfigSite configSite, AbstractComponentConfiguration parent) {
        this.configSite = requireNonNull(configSite);
        this.parent = requireNonNull(parent);
        this.depth = parent.depth() + 1;
        this.buildContext = parent.buildContext;
    }

    /**
     * A special constructor for configuration of the root container
     * 
     * @param configSite
     *            the configuration site of the component
     * @param artifactDriver
     *            the artifact driver used to create the artifact.
     */
    AbstractComponentConfiguration(ConfigSite configSite, ArtifactDriver<?> artifactDriver) {
        this.configSite = requireNonNull(configSite);
        this.parent = null;
        this.depth = 0;
        this.buildContext = new PackedArtifactBuildContext((PackedContainerConfiguration) this, artifactDriver);
    }

    void addChild(AbstractComponentConfiguration configuration) {
        if (children == null) {
            children = new LinkedHashMap<>();
        }
        requireNonNull(configuration.name);
        children.put(configuration.name, configuration);
    }

    @Override
    public final void checkConfigurable() {
        if (state == State.FINAL) {
            throw new IllegalStateException("This configuration can no longer be modified");
        }
    }

    @Override
    public final ConfigSite configSite() {
        return configSite;
    }

    /** {@inheritDoc} */
    @Override
    public final int depth() {
        return depth;
    }

    void extensionsPrepareInstantiation(ArtifactInstantiationContext ic) {
        if (children != null) {
            for (AbstractComponentConfiguration acc : children.values()) {
                if (buildContext == acc.buildContext) {
                    acc.extensionsPrepareInstantiation(ic);
                }
            }
        }
    }

    @Override
    public final FeatureMap features() {
        return features;
    }

    @Override
    @Nullable
    public final String getDescription() {
        return description;
    }

    @Override
    public final String getName() {
        return initializeName(State.GET_NAME_INVOKED, null);
    }

    Map<String, AbstractComponent> initializeChildren(AbstractComponent parent, ArtifactInstantiationContext ic) {
        if (children == null) {
            return null;
        }
        HashMap<String, AbstractComponent> result = new HashMap<>();
        for (AbstractComponentConfiguration acc : children.values()) {
            AbstractComponent ac = acc.instantiate(parent, ic);
            result.put(ac.name(), ac);
        }
        return result;
    }

    protected String initializeName(State state, String setName) {
        String n = name;
        if (n != null) {
            return n;
        }
        n = setName;
        if (this instanceof PackedContainerConfiguration) {
            Optional<ComponentNameWirelet> o = ((PackedContainerConfiguration) this).wirelets().findLast(ComponentNameWirelet.class);
            if (o.isPresent()) {
                n = o.get().name;
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

    private String initializeNameDefaultName() {
        if (this instanceof PackedContainerConfiguration) {
            // I think try and move some of this to ComponentNameWirelet
            @Nullable
            ContainerSource source = ((PackedContainerConfiguration) this).source;
            if (source instanceof Bundle) {
                String nnn = source.getClass().getSimpleName();
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
            return ((CoreComponentConfiguration) this).model.defaultPrefix();
        }

    }

    abstract AbstractComponent instantiate(AbstractComponent parent, ArtifactInstantiationContext ic);

    /**
     * Returns the path of this configuration. Invoking this method will initialize the name of the component. The component
     * path returned does not maintain any reference to this configuration object.
     * 
     * @return the path of this configuration.
     */
    @Override
    public final ComponentPath path() {
        initializeName(State.PATH_INVOKED, null);
        return PackedComponentPath.of(this);
    }

    @Override
    public AbstractComponentConfiguration setDescription(String description) {
        requireNonNull(description, "description is null");
        checkConfigurable();
        this.description = description;
        return this;
    }

    @Override
    public AbstractComponentConfiguration setName(String name) {
        ComponentNameWirelet.checkName(name);
        switch (state) {
        case INITIAL:
            initializeName(State.SET_NAME_INVOKED, name);
            return this;
        case FINAL:
            checkConfigurable();
        case GET_NAME_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via call to #getName()");
        case PATH_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via call to #path()");
        case INSTALL_INVOKED:
            throw new IllegalStateException("Cannot call this method after installing new components in the container");
        case LINK_INVOKED:
            throw new IllegalStateException("Cannot call this method after containerConfiguration.link has been invoked");
        case SET_NAME_INVOKED:
            throw new IllegalStateException("#setName(String) can only be called once");
        }
        throw new InternalError();
    }

    /** The state of the component configuration */
    enum State {

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
