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
import packed.internal.component.PackedComponentDriver.SingletonComponentDriver;
import packed.internal.component.PackedComponentDriver.StatelessComponentDriver;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.ComponentWirelet.ComponentNameWirelet;
import packed.internal.container.PackedContainerConfigurationContext;
import packed.internal.container.PackedExtensionConfiguration;
import packed.internal.container.WireletPack;
import packed.internal.hook.applicator.DelayedAccessor;

/** A common superclass for all component configuration classes. */
public class PackedComponentConfigurationContext implements ComponentConfigurationContext {

    /** A stack walker used from {@link #captureStackFrame(String)}. */
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    /** The artifact this component is a part of. */
    private final PackedAssembleContext artifact;

    /** Any children of this component (lazily initialized). */
    @Nullable
    private HashMap<String, PackedComponentConfigurationContext> children;

    /** The configuration site of this component. */
    private final ConfigSite configSite;

    /** The container this component belongs to, or null for a root container. */
    @Nullable
    // BelongsToContainer
    private final PackedContainerConfigurationContext container;

    /** Ugly stuff. */
    public ArrayList<DelayedAccessor> del = new ArrayList<>();

    /** The depth of the component in the hierarchy (including any parent artifacts). */
    final int depth;

    /** The description of the component. */
    @Nullable
    protected String description;

    public final PackedComponentDriver<?> driver;

    /** Any extension this component belongs to. */
    @Nullable
    private final PackedExtensionConfiguration extension;

    /** The first child of this component. */
    @Nullable
    protected PackedComponentConfigurationContext firstChild;

    @Nullable
    protected PackedComponentConfigurationContext lastChild;

    /** The name of the component. */
    @Nullable
    public String name;

    // We maintain this here instead of in a LinkedHashMap, because the insertion order
    // is effected if we change the name of a component. Which we do not want.
    @Nullable
    public PackedComponentConfigurationContext nextSiebling;

    /** The parent of this component, or null for a root container. */
    @Nullable
    public final PackedComponentConfigurationContext parent;

    final PackedPodConfigurationContext pod;

    /** The state of this configuration. */
    // Maaske er det en special GuestConfigurationAdaptor som er rod paa runtime.
    protected ComponentConfigurationState state = new ComponentConfigurationState();

    protected final Object source;

    /** Any wirelets that was specified by the user when creating this configuration. */
    @Nullable
    public final WireletPack wireletContext;

    /**
     * A special constructor for the top level container.
     * 
     * @param configSite
     *            the configuration site of the component
     * @param output
     *            the output of the build process
     */
    protected PackedComponentConfigurationContext(PackedComponentDriver<?> driver, ConfigSite configSite, Object source, AssembleOutput output,
            Wirelet... wirelets) {
        this.driver = requireNonNull(driver);
        this.configSite = requireNonNull(configSite);
        this.source = source;
        this.wireletContext = WireletPack.from(this, wirelets);

        this.pod = new PackedPodConfigurationContext();
        this.parent = null;
        this.container = null;
        this.depth = 0;

        this.extension = null;
        this.artifact = new PackedAssembleContext((PackedContainerConfigurationContext) this, output);

        initializeNameXX(null);
    }

    /**
     * Creates a new abstract component configuration
     * 
     * @param configSite
     *            the configuration site of the component
     * @param parent
     *            the parent of the component
     */
    public PackedComponentConfigurationContext(PackedComponentDriver<?> driver, ConfigSite configSite, Object source,
            PackedComponentConfigurationContext parent, Wirelet... wirelets) {
        this.driver = requireNonNull(driver);
        this.configSite = requireNonNull(configSite);
        this.source = source;
        this.wireletContext = WireletPack.from(this, wirelets);

        this.parent = requireNonNull(parent);
        this.container = parent instanceof PackedContainerConfigurationContext ? (PackedContainerConfigurationContext) parent : parent.container;
        this.depth = parent.depth + 1;
        this.pod = parent.pod;

        this.extension = container.activeExtension;
        this.artifact = parent.artifact;
        initializeNameXX(null);
    }

    protected PackedComponentConfigurationContext(PackedComponentDriver<?> driver, ConfigSite configSite, PackedHostConfigurationContext parent,
            PackedContainerConfigurationContext pcc, AssembleOutput output, Wirelet... wirelets) {
        this.driver = requireNonNull(driver);
        this.configSite = requireNonNull(configSite);
        this.source = null;
        this.wireletContext = WireletPack.from(this, wirelets);

        this.parent = requireNonNull(parent);
        this.container = null;
        this.depth = parent.depth + 1;
        this.pod = ((PackedComponentConfigurationContext) parent).pod; // ??

        this.extension = null;
        this.artifact = new PackedAssembleContext(pcc, output);
        initializeNameXX(null);
    }

    public PackedContainerConfigurationContext actualContainer() {
        if (this instanceof PackedContainerConfigurationContext) {
            return (PackedContainerConfigurationContext) this;
        }
        return container;
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

    final RuntimeComponentModel descritor() {
        return RuntimeComponentModel.of(driver, this);
    }

    public final Optional<Class<? extends Extension>> extension() {
        return extension == null ? Optional.empty() : extension.optional();
    }

    protected void extensionsPrepareInstantiation(PackedInstantiationContext ic) {
        for (PackedComponentConfigurationContext c = firstChild; c != null; c = c.nextSiebling) {
            if (artifact == c.artifact) {
                c.extensionsPrepareInstantiation(ic);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public final String getDescription() {
        return description;
    }

    private void initializeNameXX(String newName) {
        String n = newName;
        if (newName == null) {
            if (this instanceof PackedContainerConfigurationContext) {
                PackedContainerConfigurationContext pcc = (PackedContainerConfigurationContext) this;
                if (pcc.wireletContext != null) {
                    ComponentNameWirelet cwn = pcc.wireletContext.nameWirelet();
                    if (cwn != null) {
                        nameState = NAME_INITIALIZED_WITH_WIRELET;
                        n = cwn.name;
                    }
                }
            }
        }

        boolean isFree = false;

        if (n == null) {
            n = initializeName0();
            isFree = true;
        } else if (n.endsWith("?")) {
            n = n.substring(0, n.length() - 1);
            isFree = true;
        }

        // maybe just putIfAbsent, under the assumption that we will rarely need to override.
        if (parent != null) {
            if (parent != null && parent.children != null && parent.children.containsKey(n)) {
                // If name exists. Lets keep a counter (maybe if bigger than 5). For people trying to
                // insert a given component 1 million times...
                if (!isFree) {
                    throw new RuntimeException("Name already exist " + n);
                }
                int counter = 1;
                String prefix = n;
                do {
                    n = prefix + counter++;
                } while (parent.children.containsKey(n));
            }

            if (newName != null) {
                parent.children.remove(name);
                parent.children.put(n, this);
            } else {
                name = n;
                if (parent.children == null) {
                    parent.children = new HashMap<>();
                    parent.firstChild = parent.lastChild = this;
                } else {
                    parent.lastChild.nextSiebling = this;
                    parent.lastChild = this;
                }
                parent.children.put(n, this);
            }
        }
        name = n;
    }

    private String initializeName0() {
        if (this instanceof PackedContainerConfigurationContext) {

            // I think try and move some of this to ComponentNameWirelet
            @Nullable
            Class<?> source = this.source.getClass();
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
        } else if (this instanceof PackedSingletonConfigurationContext) {
            return ((SingletonComponentDriver) this.driver).model.defaultPrefix();
        } else {
            return ((StatelessComponentDriver) this.driver).model.defaultPrefix();
        }
    }

    int nameState;

    private static final int NAME_INITIALIZED_WITH_WIRELET = 1 << 18; // set atomically with DONE
    private static final int NAME_SET = 1 << 17; // set atomically with ABNORMAL
    private static final int NAME_GET = 1 << 16; // true if joiner waiting
    private static final int NAME_GET_PATH = 1 << 15; // true if joiner waiting
    private static final int NAME_CHILD_GOT_PATH = 1 << 14; // true if joiner waiting

    private static final int NAME_GETSET_MASK = NAME_SET + NAME_GET + NAME_GET_PATH + NAME_CHILD_GOT_PATH;

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        // Only update with NAME_GET if no prev set/get op
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET;
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath path() {
        int anyPathMask = NAME_GET_PATH + NAME_CHILD_GOT_PATH;
        if ((nameState & anyPathMask) != 0) {
            PackedComponentConfigurationContext p = parent;
            while (p != null && ((p.nameState & anyPathMask) == 0)) {
                p.nameState = (p.nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
            }
        }
        nameState = (nameState & ~NAME_GETSET_MASK) | NAME_GET_PATH;
        return PackedComponentPath.of(this); // show we weak intern them????
    }

    /** {@inheritDoc} */
    @Override
    public void setName(String name) {
        // First lets check the name is valid
        ComponentNameWirelet.checkName(name);
        int s = nameState;

        checkConfigurable();

        if ((s & NAME_SET) != 0) {
            throw new IllegalStateException("#setName(String) can only be called once");
        }

        if ((s & NAME_GET) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #getName() has been invoked");
        }

        if ((s & NAME_GET_PATH) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #path() has been invoked");
        }

        if ((s & NAME_CHILD_GOT_PATH) != 0) {
            throw new IllegalStateException("#setName(String) cannot be called after #path() has been invoked on a child component");
        }

        nameState |= NAME_SET;

        if ((s & NAME_INITIALIZED_WITH_WIRELET) != 0) {
            return;// We never set override a name set by a wirelet
        }

        initializeNameXX(name);
//
//        switch (state.oldState) {
//        case INITIAL:
//            initializeName(State.SET_NAME_INVOKED, name);
//            return;
//        case FINAL:
//            checkConfigurable();
//        case GET_NAME_INVOKED:
//            throw new IllegalStateException("Cannot call #setName(String) after the name has been initialized via calls to #getName()");
//        case EXTENSION_USED:
//            throw new IllegalStateException("Cannot call #setName(String) after any extensions has has been used");
//        case PATH_INVOKED:
//            throw new IllegalStateException("Cannot call #setName(String) after name has been initialized via calls to #path()");
//        case INSTALL_INVOKED:
//            throw new IllegalStateException("Cannot call this method after having installed components or used extensions");
//        case LINK_INVOKED:
//            throw new IllegalStateException("Cannot call this method after #link() has been invoked");
//        case SET_NAME_INVOKED:
//            throw new IllegalStateException("#setName(String) can only be called once");
//        }
//        throw new InternalError();
    }

    final Map<String, PackedComponent> initializeChildren(PackedComponent parent, PackedInstantiationContext ic) {
        if (firstChild == null) {
            return null;
        }
        // Hmm, we should probably used LinkedHashMap to retain order.
        // It just uses so much memory...
        HashMap<String, PackedComponent> result = new HashMap<>(children.size());

        for (PackedComponentConfigurationContext c = firstChild; c != null; c = c.nextSiebling) {
            PackedComponent ac = c.driver.create(parent, c, ic);
            result.put(ac.name(), ac);
        }
        return Map.copyOf(result);
    }

    public boolean isInSameContainer(PackedComponentConfigurationContext other) {
        return containerX() == other.containerX();
    }

    @Override
    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public void setDescription(String description) {
        requireNonNull(description, "description is null");
        checkConfigurable();
        this.description = description;
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