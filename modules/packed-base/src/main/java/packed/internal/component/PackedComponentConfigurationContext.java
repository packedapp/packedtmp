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

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import app.packed.artifact.ArtifactSource;
import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentPath;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerBundle;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import packed.internal.artifact.AssembleOutput;
import packed.internal.artifact.PackedAssembleContext;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.ContainerWirelet.ContainerNameWirelet;
import packed.internal.container.PackedContainerConfigurationContext;
import packed.internal.container.PackedExtensionConfiguration;
import packed.internal.hook.applicator.DelayedAccessor;

/** A common superclass for all component configuration classes. */
public abstract class PackedComponentConfigurationContext implements ComponentConfigurationContext {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The artifact this component is a part of. */
    private final PackedAssembleContext artifact;

    /** Any children of this component (lazily initialized), in order of insertion. */
    @Nullable
    protected LinkedHashMap<String, PackedComponentConfigurationContext> children;

    /** The configuration site of this component. */
    private final ConfigSite configSite;

    /** The container this component belongs to, or null for a root container. */
    @Nullable
    // BelongsToContainer
    private final PackedContainerConfigurationContext container;

    /** Ugly stuff. */
    public ArrayList<DelayedAccessor> del = new ArrayList<>();

    /** The depth of the component in the hierarchy (including any parent artifacts). */
    private final int depth;

    /** The description of the component. */
    @Nullable
    protected String description;

    public final PackedComponentDriver<?> driver;

    /** Any extension this component belongs to. */
    @Nullable
    private final PackedExtensionConfiguration extension;

    /** The name of the component. */
    @Nullable
    public String name;

    /** The parent of this component, or null for a root container. */
    @Nullable
    public final PackedComponentConfigurationContext parent;

    /** The state of this configuration. */
    // Maaske er det en special GuestConfigurationAdaptor som er rod paa runtime.
    protected ComponentConfigurationState state = new ComponentConfigurationState();

    /**
     * A special constructor for the top level container.
     * 
     * @param configSite
     *            the configuration site of the component
     * @param output
     *            the output of the build process
     */
    protected PackedComponentConfigurationContext(PackedComponentDriver<?> driver, ConfigSite configSite, AssembleOutput output) {
        this.driver = requireNonNull(driver);
        this.configSite = requireNonNull(configSite);

        this.parent = null;
        this.container = null;
        this.depth = 0;

        this.extension = null;
        this.artifact = new PackedAssembleContext((PackedContainerConfigurationContext) this, output);
    }

    /**
     * Creates a new abstract component configuration
     * 
     * @param configSite
     *            the configuration site of the component
     * @param parent
     *            the parent of the component
     */
    protected PackedComponentConfigurationContext(PackedComponentDriver<?> driver, ConfigSite configSite, PackedComponentConfigurationContext parent) {
        this.driver = requireNonNull(driver);
        this.configSite = requireNonNull(configSite);

        this.parent = requireNonNull(parent);
        this.container = parent instanceof PackedContainerConfigurationContext ? (PackedContainerConfigurationContext) parent : parent.container;
        this.depth = parent.depth() + 1;

        this.extension = container.activeExtension;
        this.artifact = parent.artifact;
    }

    protected PackedComponentConfigurationContext(PackedComponentDriver<?> driver, ConfigSite configSite, PackedHostConfigurationContext parent,
            PackedContainerConfigurationContext pcc, AssembleOutput output) {
        this.driver = requireNonNull(driver);
        this.configSite = requireNonNull(configSite);

        this.parent = requireNonNull(parent);
        this.container = null;
        this.depth = parent.depth() + 1;

        this.extension = null;
        this.artifact = new PackedAssembleContext(pcc, output);
    }

    public PackedContainerConfigurationContext actualContainer() {
        if (this instanceof PackedContainerConfigurationContext) {
            return (PackedContainerConfigurationContext) this;
        }
        return container;
    }

    /**
     * Adds the specified child to this component.
     * 
     * @param child
     *            the child to add
     */
    protected final void addChild(PackedComponentConfigurationContext child) {
        requireNonNull(child.name);
        LinkedHashMap<String, PackedComponentConfigurationContext> c = children;
        if (c == null) {
            c = children = new LinkedHashMap<>();
        }
        children.put(child.name, child);
    }

    /**
     * Returns the artifact this component is a part of.
     * 
     * @return the artifact this component is a part of
     */
    public final PackedAssembleContext artifact() {
        return artifact;
    }

    /**
     * Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
     * not located on any subclasses of {@link Extension} or any class that implements {@link ArtifactSource}.
     * <p>
     * Invoking this method typically takes in the order of 1-2 microseconds.
     * <p>
     * If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
     * {@link ConfigSite#UNKNOWN}.
     * 
     * @param operation
     *            the operation
     * @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
     * @see StackWalker
     */
    // TODO add stuff about we also ignore non-concrete container sources...
    protected final ConfigSite captureStackFrame(String operation) {
        // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
        // to the extension class in order to simplify the filtering mechanism.

        // Vi kan spoerge "if context.captureStackFrame() ...."

        if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
            return ConfigSite.UNKNOWN;
        }
        Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
        return sf.isPresent() ? configSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
    }

    /**
     * @param frame
     *            the frame to filter
     * @return whether or not to filter the frame
     */
    private final boolean captureStackFrameIgnoreFilter(StackFrame frame) {

        Class<?> c = frame.getDeclaringClass();
        // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract bundle der override configure()...
        // Syntes bare vi filtrer app.packed.base modulet fra...
        // Kan vi ikke checke om imod vores container source.

        // ((PackedExtensionContext) context()).container().source
        // Nah hvis man koere fra config er det jo fint....
        // Fra config() paa en bundle er det fint...
        // Fra alt andet ikke...

        // Dvs ourContainerSource
        return Extension.class.isAssignableFrom(c)
                || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && ArtifactSource.class.isAssignableFrom(c));
    }

    /** {@inheritDoc} */
    @Override
    public final void checkConfigurable() {
        if (state.oldState == State.FINAL) {
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
    public final PackedContainerConfigurationContext container() {
        return container;
    }

    PackedContainerConfigurationContext containerX() {
        PackedComponentConfigurationContext c = this;
        while (!(c instanceof PackedContainerConfigurationContext)) {
            c = c.parent;
        }
        return (PackedContainerConfigurationContext) c;
    }

    public final int depth() {
        return depth;
    }

    public final ComponentRuntimeDescriptor descritor() {
        return ComponentRuntimeDescriptor.of(driver, this);
    }

    public final Optional<Class<? extends Extension>> extension() {
        return extension == null ? Optional.empty() : extension.optional();
    }

    protected void extensionsPrepareInstantiation(PackedInstantiationContext ic) {
        if (children != null) {
            for (PackedComponentConfigurationContext acc : children.values()) {
                if (artifact == acc.artifact) {
                    acc.extensionsPrepareInstantiation(ic);
                }
            }
        }
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

    final Map<String, PackedComponent> initializeChildren(PackedComponent parent, PackedInstantiationContext ic) {
        if (children == null) {
            return null;
        }
        // Hmm, we should probably used LinkedHashMap to retain order.
        // It just uses so much memory...
        HashMap<String, PackedComponent> result = new HashMap<>(children.size());
        for (PackedComponentConfigurationContext acc : children.values()) {
            PackedComponent ac = acc.driver.create(parent, acc, ic);
            result.put(ac.name(), ac);
        }
        return Map.copyOf(result);
    }

    public final String initializeName(State newState, String setName) {
        String n = name;
        if (n != null) {
            return n;
        }
        n = setName;
        if (this instanceof PackedContainerConfigurationContext) {
            PackedContainerConfigurationContext pcc = (PackedContainerConfigurationContext) this;
            if (pcc.wireletContext != null) {
                n = pcc.wireletContext.name(pcc);
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
        this.state.oldState = newState;
        return this.name = n;
    }

    @Deprecated
    protected abstract String initializeNameDefaultName();

    public boolean isInSameContainer(PackedComponentConfigurationContext other) {
        return containerX() == other.containerX();
    }

    @Override
    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath path() {
        initializeName(State.PATH_INVOKED, null);
        return PackedComponentPath.of(this); // show we weak intern them????
    }

    /** {@inheritDoc} */
    @Override
    public void setDescription(String description) {
        requireNonNull(description, "description is null");
        checkConfigurable();
        this.description = description;
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        // First lets check the name is valid
        ContainerNameWirelet.checkName(name);
        switch (state.oldState) {
        case INITIAL:
            initializeName(State.SET_NAME_INVOKED, name);
            return;
        case FINAL:
            checkConfigurable();
        case GET_NAME_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after the name has been initialized via calls to #getName()");
        case EXTENSION_USED:
            throw new IllegalStateException("Cannot call #setName(String) after any extensions has has been used");
        case PATH_INVOKED:
            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via calls to #path()");
        case INSTALL_INVOKED:
            throw new IllegalStateException("Cannot call this method after having installed components or used extensions");
        case LINK_INVOKED:
            throw new IllegalStateException("Cannot call this method after #link() has been invoked");
        case SET_NAME_INVOKED:
            throw new IllegalStateException("#setName(String) can only be called once");
        }
        throw new InternalError();
    }

    @Override
    public <C> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        requireNonNull(driver, "driver is null");
        throw new UnsupportedOperationException();
    }

    /** The state of the component configuration */
    public enum State {

        /** The initial state. */
        EXTENSION_USED,

        /** */
        FINAL,

        /** {@link ComponentConfiguration#getName()} has been invoked. */
        GET_NAME_INVOKED,

        /** The initial state. */
        INITIAL,

        /** One of the install component methods has been invoked. */
        INSTALL_INVOKED,

        /** {@link ContainerConfiguration#link(ContainerBundle, Wirelet...)} has been invoked. */
        LINK_INVOKED,

        /** One of the install component methods has been invoked. */
        PATH_INVOKED,

        /** Set name has been invoked. */
        SET_NAME_INVOKED;
    }
}
//
///**
//* Captures the configuration site by finding the first stack frame where the declaring class of the frame's method is
//* not located on any subclasses of {@link Extension} or any class that implements {@link ArtifactSource}.
//* <p>
//* Invoking this method typically takes in the order of 1-2 microseconds.
//* <p>
//* If capturing of stack-frame-based config sites has been disable via, for example, fooo. This method returns
//* {@link ConfigSite#UNKNOWN}.
//* 
//* @param operation
//*            the operation
//* @return a stack frame capturing config site, or {@link ConfigSite#UNKNOWN} if stack frame capturing has been disabled
//* @see StackWalker
//*/
//// TODO add stuff about we also ignore non-concrete container sources...
//@Override
//protected final ConfigSite captureStackFrame(String operation) {
//  // API-NOTE This method is not available on ExtensionContext to encourage capturing of stack frames to be limited
//  // to the extension class in order to simplify the filtering mechanism.
//
//  if (ConfigSiteSupport.STACK_FRAME_CAPTURING_DIABLED) {
//      return ConfigSite.UNKNOWN;
//  }
//  Optional<StackFrame> sf = STACK_WALKER.walk(e -> e.filter(f -> !captureStackFrameIgnoreFilter(f)).findFirst());
//  return sf.isPresent() ? configSite().thenStackFrame(operation, sf.get()) : ConfigSite.UNKNOWN;
//}
//
///**
//* @param frame
//*            the frame to filter
//* @return whether or not to filter the frame
//*/
//private final boolean captureStackFrameIgnoreFilter(StackFrame frame) {
//  Class<?> c = frame.getDeclaringClass();
//  // Det virker ikke skide godt, hvis man f.eks. er en metode on a abstract bundle der override configure()...
//  // Syntes bare vi filtrer app.packed.base modulet fra...
//  // Kan vi ikke checke om imod vores container source.
//
//  // ((PackedExtensionContext) context()).container().source
//  // Nah hvis man koere fra config er det jo fint....
//  // Fra config() paa en bundle er det fint...
//  // Fra alt andet ikke...
//
//  // Dvs ourContainerSource
//  return Extension.class.isAssignableFrom(c)
//          || ((Modifier.isAbstract(c.getModifiers()) || Modifier.isInterface(c.getModifiers())) && ArtifactSource.class.isAssignableFrom(c));
//}