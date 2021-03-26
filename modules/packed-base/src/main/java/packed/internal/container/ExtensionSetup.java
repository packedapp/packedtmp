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
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.application.BuildInfo;
import app.packed.base.NamespacePath;
import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;
import app.packed.container.Extension;
import app.packed.container.Extension.Subtension;
import app.packed.container.ExtensionConfiguration;
import app.packed.container.InternalExtensionException;
import app.packed.inject.Factory;
import packed.internal.component.ComponentSetup;
import packed.internal.component.PackedWireletHandle;
import packed.internal.component.WireletWrapper;
import packed.internal.util.LookupUtil;

/** A setup class for an extension. Exposed to end-users as {@link ExtensionConfiguration}. */
public final class ExtensionSetup implements ExtensionConfiguration {

    /** A handle for invoking {@link Extension#onContainerLinkage()}. */
    static final MethodHandle MH_INJECT_PARENT = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ExtensionSetup.class, "injectParent", Extension.class);

    /** The component representation of this extension. */
    public final NewExtensionSetup component;

    /**
     * Creates a new instance of this class as part of creating the extension component.
     * <p>
     * This constructor is called from the constructor of {@link ComponentSetup}.
     * 
     * @param component
     *            the component that this extension belongs to
     * @param model
     *            a model of the extension.
     */
    public ExtensionSetup(NewExtensionSetup component, ExtensionModel model) {
        this.component = requireNonNull(component);
    }

    /** {@inheritDoc} */
    @Override
    public BuildInfo build() {
        return component.build();
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        if (component.isConfigured) {
            throw new IllegalStateException("This extension (" + component.model.name() + ") is no longer configurable");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkExtendable() {
        if (component.memberOfContainer.children != null) {
            throw new IllegalStateException();
        }
    }

    /**
     * Returns the container this extension is a part of.
     * 
     * @return the container this extension is a part of
     */
    public ContainerSetup getMemberOfContainer() {
        return component.memberOfContainer;
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends Extension> extensionClass() {
        return component.model.extensionClass();
    }

    /**
     * Returns the extension instance.
     * 
     * @return the extension instance
     * @throws IllegalStateException
     *             if trying to call this method from the constructor of the extension
     */
    public Extension extensionInstance() {
        Extension e = component.instance;
        if (e == null) {
            throw new InternalExtensionException("Cannot call this method from the constructor of " + component.model.fullName());
        }
        return e;
    }

    Extension injectParent() {
        ContainerSetup parent = component.memberOfContainer.parent;
        if (parent != null) {
            ExtensionSetup extensionContext = parent.getExtensionContext(extensionClass());
            if (extensionContext != null) {
                return extensionContext.component.instance;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Class<?> implementation) {
        return component.wire(ComponentDriver.driverInstall(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration install(Factory<?> factory) {
        return component.wire(ComponentDriver.driverInstall(factory));
    }

    /** {@inheritDoc} */
    @Override
    public BaseComponentConfiguration installInstance(Object instance) {
        return component.wire(ComponentDriver.driverInstallInstance(instance));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPartOfImage() {
        return component.memberOfContainer.isPartOfImage();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUsed(Class<? extends Extension> extensionClass) {
        return component.memberOfContainer.isInUse(extensionClass);
    }

    /** {@inheritDoc} */
    @Override
    public Component link(Assembly<?> assembly, Wirelet... wirelets) {
        return component.link(assembly, wirelets);
    }

    /**
     * @param lookup
     */
    public void lookup(Lookup lookup) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the extension's model.
     * 
     * @return the extension's model
     */
    public ExtensionModel model() {
        return component.model;
    }


    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return component.path();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <E extends Subtension> E use(Class<E> subtensionClass) {
        requireNonNull(subtensionClass, "subtensionClass is null");

        // Finds the subtension's model and extension class
        SubtensionModel subModel = SubtensionModel.of(subtensionClass);
        Class<? extends Extension> subExtensionClass = subModel.extensionClass;

        // Check that requested subtension's extension is a direct dependency of this extension
        if (!component.model.dependencies().contains(subExtensionClass)) {
            // Special message if you try to use your own subtension
            if (component.model.extensionClass() == subExtensionClass) {
                throw new InternalExtensionException(component.model.extensionClass().getSimpleName() + " cannot use its own subtension "
                        + subExtensionClass.getSimpleName() + "." + subtensionClass.getSimpleName());
            }
            throw new InternalExtensionException(component.model.extensionClass().getSimpleName() + " must declare " + format(subModel.extensionClass)
                    + " as a dependency in order to use " + subExtensionClass.getSimpleName() + "." + subtensionClass.getSimpleName());
        }

        // Get the extension instance (create it if needed) thaw we need to create a subtension for
        Extension instance = component.memberOfContainer.useDependencyCheckedExtension(subExtensionClass, this).component.instance;

        // Create a new subtension instance using the extension instance and this.extensionClass as the requesting extension
        return (E) subModel.newInstance(instance, extensionClass());
    }

    /** {@inheritDoc} */
    @Override
    public <C extends ComponentConfiguration> C userWire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return component.memberOfContainer.component.wire(driver, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return component.wire(driver, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Wirelet> WireletHandle<T> wirelets(Class<T> wireletClass) {
        requireNonNull(wireletClass, "wireletClass is null");
        Module m = component.model.extensionClass().getModule();
        if (m != wireletClass.getModule()) {
            throw new InternalExtensionException("Must specify a wirelet that is in the same module (" + m.getName() + ") as '" + component.model.name()
                    + ", module of wirelet was " + wireletClass.getModule());
        }
        WireletWrapper wirelets = component.memberOfContainer.component.wirelets;
        if (wirelets == null) {
            return WireletHandle.of();
        }
        return new PackedWireletHandle<>(wirelets, wireletClass);
    }

}
