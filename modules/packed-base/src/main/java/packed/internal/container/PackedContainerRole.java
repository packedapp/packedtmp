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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

import app.packed.artifact.ArtifactContext;
import app.packed.artifact.ArtifactSource;
import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.SingletonConfiguration;
import app.packed.component.StatelessConfiguration;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerDescriptor;
import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import app.packed.inject.Factory;
import app.packed.service.ServiceExtension;
import packed.internal.artifact.AssembleOutput;
import packed.internal.artifact.InstantiationContext;
import packed.internal.component.BundleConfiguration;
import packed.internal.component.ComponentModel;
import packed.internal.component.ComponentNode;
import packed.internal.component.ComponentNodeConfiguration;
import packed.internal.component.PackedComponentDriver;
import packed.internal.component.PackedComponentDriver.ContainerComponentDriver;
import packed.internal.component.PackedComponentDriver.SingletonComponentDriver;
import packed.internal.component.PackedComponentDriver.StatelessComponentDriver;
import packed.internal.component.wirelet.WireletPack;
import packed.internal.config.ConfigSiteSupport;
import packed.internal.container.PackedContainer.PackedArtifactContext;
import packed.internal.inject.ConfigSiteInjectOperations;
import packed.internal.inject.factory.FactoryHandle;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.runtime.PackedInjector;

/** The default container context. */
public final class PackedContainerRole {

    private static final int LS_0_MAINL = 0;

    private static final int LS_1_LINKING = 1;

    private static final int LS_2_HOSTING = 2;

    private static final int LS_3_FINISHED = 3;

    /** Any extension that is active. */
    @Nullable
    public PackedExtensionConfiguration activeExtension;

    /** All used extensions, in order of registration. */
    private final LinkedHashMap<Class<? extends Extension>, PackedExtensionConfiguration> extensions = new LinkedHashMap<>();

    private TreeSet<PackedExtensionConfiguration> extensionsOrdered;

    /** The current component lookup object, updated via {@link #lookup(Lookup)} */
    // useFor future components...
    // We need to support some way to
    private ComponentLookup lookup;

    /** A container model. */
    private final ContainerModel model;

    int realState;

    public ComponentNodeConfiguration component;

    private PackedContainerRole(Object source) {
        this.lookup = this.model = ContainerModel.of(source.getClass());
    }

    public static PackedContainerRole create(PackedComponentDriver<?> driver, ConfigSite cs, Object source, ComponentNodeConfiguration parent,
            AssembleOutput output, Wirelet... wirelets) {
        PackedContainerRole p1 = new PackedContainerRole(source);
        ComponentNodeConfiguration pccc = new ComponentNodeConfiguration(parent, driver, cs, source, output, p1, wirelets);
        p1.component = pccc;
        return p1;
    }

    private void advanceTo(int newState) {
        if (realState == 0) {
            // We need to sort all extensions that are used. To make sure
            // they progress in their lifecycle in the right order.
            extensionsOrdered = new TreeSet<>(extensions.values());
            for (PackedExtensionConfiguration pec : extensionsOrdered) {
                activeExtension = pec;
                pec.onConfigured();
            }
            activeExtension = null;
            realState = LS_1_LINKING;
        }

        if (realState == LS_1_LINKING && newState > LS_1_LINKING) {
            for (ComponentNodeConfiguration cc = component.firstChild; cc != null; cc = cc.nextSibling) {
                if (cc.isContainer()) {
                    cc.container.assembleExtensions();
                }
            }
            for (PackedExtensionConfiguration pec : extensionsOrdered) {
                activeExtension = pec;
                pec.onChildrenConfigured();
            }
        }
    }

    public PackedContainerRole assemble() {
        configure();
        assembleExtensions();
        return this;
    }

    private void assembleExtensions() {
        advanceTo(LS_3_FINISHED);
    }

    public void buildDescriptor(ContainerDescriptor.Builder builder) {
        builder.setBundleDescription(component.getDescription());
        builder.setName(component.getName());
        for (PackedExtensionConfiguration e : extensions.values()) {
            e.buildDescriptor(builder);
        }
        builder.extensions.addAll(extensions.keySet());
    }

    /**
     * Configures the configuration.
     */
    private void configure() {
        // If it is an image it has already been assembled
        if (component.source instanceof Bundle) {
            BundleConfiguration.configure((Bundle<?>) component.source, new PackedContainerConfiguration(this));
        }
        component.finalState = true;
    }

    public Set<Class<? extends Extension>> extensions() {
        return Collections.unmodifiableSet(extensions.keySet());
    }

    private static void extensionsPrepareInstantiation(ComponentNodeConfiguration pccc, InstantiationContext ic) {
        if (pccc.isContainer()) {
            PackedContainerRole ccc = pccc.container;
            PackedExtensionConfiguration ee = ccc.extensions.get(ServiceExtension.class);
            if (ee != null) {
                PackedInjector di = ServiceExtensionNode.fromExtension(((ServiceExtension) ee.instance())).onInstantiate(ic.wirelets);
                ic.put(ccc.component, di);
            }
        }
        for (ComponentNodeConfiguration c = pccc.firstChild; c != null; c = c.nextSibling) {
            if (pccc.artifact == c.artifact) {
                extensionsPrepareInstantiation(c, ic);
            }
        }
    }

    /**
     * Used to convert factories to method handle
     * 
     * @param handle
     *            the factory handle
     * @return the method handle
     */
    public MethodHandle fromFactoryHandle(FactoryHandle<?> handle) {
        return lookup.readable(handle).toMethodHandle();
    }

    /**
     * Returns the context for the specified extension type. Or null if no extension of the specified type has already been
     * added.
     * 
     * @param extensionType
     *            the type of extension to return a context for
     * @return an extension's context, iff the specified extension type has already been added
     * @see #use(Class)
     * @see #useExtension(Class, PackedExtensionConfiguration)
     */
    @Nullable
    public PackedExtensionConfiguration getExtensionContext(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return extensions.get(extensionType);
    }

    public <T> SingletonConfiguration<T> install(Factory<T> factory) {
        requireNonNull(factory, "factory is null");
        ComponentModel model = lookup.componentModelOf(factory.rawType());
        ConfigSite configSite = component.captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);
        SingletonComponentDriver scd = new SingletonComponentDriver(lookup, factory);

        ComponentNodeConfiguration conf = new ComponentNodeConfiguration(component, scd, configSite, null, null, this);
        model.invokeOnHookOnInstall(component.source, conf);
        return scd.toConf(conf);
    }

    public <T> SingletonConfiguration<T> installInstance(T instance) {
        requireNonNull(instance, "instance is null");
        ComponentModel model = lookup.componentModelOf(instance.getClass());
        ConfigSite configSite = component.captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);
        SingletonComponentDriver scd = new SingletonComponentDriver(lookup, instance);

        ComponentNodeConfiguration conf = new ComponentNodeConfiguration(component, scd, configSite, null, null, this);
        model.invokeOnHookOnInstall(component.source, conf); // installs any extensions...
        return scd.toConf(conf);
    }

    public StatelessConfiguration installStateless(Class<?> implementation) {
        StatelessComponentDriver scd = new StatelessComponentDriver(lookup, implementation);

        ConfigSite configSite = component.captureStackFrame(ConfigSiteInjectOperations.COMPONENT_INSTALL);

        ComponentNodeConfiguration conf = new ComponentNodeConfiguration(component, scd, configSite, null, null, this);
        scd.model.invokeOnHookOnInstall(component.source, conf);
        return scd.toConf(conf);
    }

    public ArtifactContext instantiateArtifact(WireletPack wc) {
        InstantiationContext pic = new InstantiationContext(wc);
        extensionsPrepareInstantiation(this.component, pic);

        // Will instantiate the whole container hierachy
        ComponentNode pc = PackedContainer.create(null, this, pic);
        ComponentNodeConfiguration.methodHandlePassing0(component, pc, pic);
        return new PackedArtifactContext(pc);
    }

    // Previously this method returned the specified bundle. However, to encourage people to configure the bundle before
    // calling this method: link(MyBundle().setStuff(x)) instead of link(MyBundle()).setStuff(x) we now have void return
    // type.
    // Maybe in the future LinkedBundle<- (LinkableContainerSource)
    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        requireNonNull(bundle, "bundle is null");
        PackedComponentDriver<?> d = BundleConfiguration.driver(bundle);

        // extract driveren fra bundle...
        // lav nyt barn med den...
        ComponentNodeConfiguration child = d.newContainConf(component, bundle, wirelets);

        // IDK do we want to progress to next stage just in case...
        if (realState == LS_0_MAINL) {
            advanceTo(LS_1_LINKING);
        } else if (realState == LS_2_HOSTING) {
            throw new IllegalStateException("Was hosting");
        } else if (realState == LS_3_FINISHED) {
            throw new IllegalStateException("Was Assembled");
        }

        // finalize name of this container
        if (child.isContainer()) {
            child.container.configure();
        }
    }

    public void lookup(@Nullable Lookup lookup) {
        // If user specifies null, we use whatever
        // Actually I think null might be okay, then its standard module-info.java
        // Component X has access to G, but Packed does not have access
        this.lookup = lookup == null ? model : model.withLookup(lookup);
    }

    public Class<?> sourceType() {
        return component.source.getClass();
    }

    @SuppressWarnings("unchecked")
    public <T extends Extension> T use(Class<T> extensionType) {
        return (T) useExtension(extensionType, null).instance();
    }

    /**
     * If an extension of the specified type has not already been installed, installs it. Returns the extension's context.
     * 
     * @param extensionType
     *            the type of extension
     * @param caller
     *            non-null if it is another extension that is requesting the extension
     * @return the extension's context
     * @throws IllegalStateException
     *             if an extension of the specified type has not already been installed and the container is no longer
     *             configurable
     * @throws InternalExtensionException
     *             if the
     */
    PackedExtensionConfiguration useExtension(Class<? extends Extension> extensionType, @Nullable PackedExtensionConfiguration caller) {
        requireNonNull(extensionType, "extensionType is null");
        PackedExtensionConfiguration pec = extensions.get(extensionType);

        // We do not use #computeIfAbsent, because extensions might install other extensions via Extension#onAdded.
        // Which will fail with ConcurrentModificationException (see ExtensionDependenciesTest)
        if (pec == null) {
            // Checks that we are still configurable
            if (caller == null) {
                if (realState != 0) {
                    // Cannot perform this operation
                    throw new IllegalStateException("Cannot install new extensions at this point, extensionType = " + extensionType);
                }
                component.checkConfigurable();
            } else {
                caller.checkConfigurable();
            }
            extensions.put(extensionType, pec = PackedExtensionConfiguration.of(this, extensionType));

            // Add Component
            PackedComponentDriver<?> pcd = new PackedComponentDriver.ExtensionComponentDriver(ExtensionModel.of(extensionType));
            new ComponentNodeConfiguration(component, pcd, component.configSite(), null, null, this);
        }
        return pec;
    }

    public static PackedContainerRole of(AssembleOutput output, Object source, Wirelet... wirelets) {
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);
        return PackedContainerRole.create(ContainerComponentDriver.INSTANCE, cs, source, null, output, wirelets);
    }

    public static PackedContainerRole assemble(AssembleOutput output, ArtifactSource source, Wirelet... wirelets) {
        PackedContainerRole c = of(output, source, wirelets);
        ConfigSite cs = ConfigSiteSupport.captureStackFrame(ConfigSiteInjectOperations.INJECTOR_OF);
        c = PackedContainerRole.create(ContainerComponentDriver.INSTANCE, cs, source, null, output, wirelets);
        c.assemble();
        return c;
    }

}
//
///**
//* Returns whether or not the specified extension type has been used.
//* 
//* @param extensionType
//*            the extension type to test.
//* @return whether or not the extension has been used
//*/
//boolean isExtensionUsed(Class<? extends Extension> extensionType) {
//  requireNonNull(extensionType, "extensionType is null");
//  return extensions.containsKey(extensionType);
//}
// Implementation note: We can do linking (calling bundle.configure) in two ways. Immediately, or later after the parent
// has been fully configured. We choose immediately because of nicer stack traces. And we also avoid some infinite
// loop situations, for example, if a bundle recursively links itself which fails by throwing
// java.lang.StackOverflowError instead of an infinite loop.
