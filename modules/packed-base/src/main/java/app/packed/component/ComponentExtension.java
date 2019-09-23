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
package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionNode;
import app.packed.service.Factory;
import app.packed.service.ServiceExtension;
import app.packed.util.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.container.extension.PackedExtensionContext;
import packed.internal.service.InjectConfigSiteOperations;

/**
 * An extension that provides basic functionality for installing components in a container.
 */
public final class ComponentExtension extends Extension {

    /** The configuration of the container. */
    @Nullable
    private PackedContainerConfiguration pcc;

    /** Should never be initialized by users. */
    ComponentExtension() {}

    void addRule(ComponentRule rule) {

    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * <p>
     * This method uses the {@link ServiceExtension}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    public ComponentConfiguration install(Class<?> implementation) {
        requireNonNull(implementation, "implementation is null");
        return pcc.install(Factory.findInjectable(implementation), captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL));
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * This method uses the {@link ServiceExtension}.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     */
    public ComponentConfiguration install(Factory<?> factory) {
        requireNonNull(factory, "factory is null");
        return pcc.install(factory, captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL));
    }

    public ComponentConfiguration installInstance(Object instance) {
        requireNonNull(instance, "instance is null");
        return pcc.installInstance(instance, captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL));
    }

    /**
     * Installs a component that does not have any instance representing it.
     * <p>
     * This method uses the {@link ServiceExtension}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    public ComponentConfiguration installStatic(Class<?> implementation) {
        requireNonNull(implementation, "implementation is null");
        return pcc.installStatic(implementation, captureStackFrame(InjectConfigSiteOperations.COMPONENT_INSTALL));
    }

    /** {@inheritDoc} */
    @Override
    protected ExtensionNode<?> onAdded() {
        this.pcc = ((PackedExtensionContext) context()).pcc;
        return super.onAdded();
    }

    // Scans this package...
    public void scan() {}

    public void scan(String... packages) {}

    // Alternative to ComponentScan
    public void scanForInstall(Class<?>... classesInPackages) {}
}

// install
// noget med Main, Entry points....
// Man kan f.eks. disable et Main.... EntryPointExtension....

// @Main skal jo pege et paa en eller anden extension...

// Selvfoelelig er det hele komponenter... Ogsaa scoped
// Vi skal ikke til at have flere scans...

// AllowRuntimeInstallationOfComponents();

// @Scoped
// @Install()

// Why export, Need to export
class ComponentRule {

    // What to disable
    // Where (which components)to Disable it?
    // What todo... warn. fail, ...

    protected final void disableMemberInjection(Class<? extends Member> memberType) {
        //// Det burde vaere noget paa component....
        // Ahh vi har instance of and types...
        // architecture().disable(Inject.class)
        // architecture().disable(Inject.class, Class<? extends Member> fieldOrMethod);
        // // Field, Method, Member.class

        // Kunne ogsaa lave en @Rules() man kunne smide paa bundles...
    }

    static ComponentRule disableAnnotatedMethodHook(Class<? extends Annotation> type, Class<?>... scannable) {
        // Fungere daarlig med hook groups....
        // Vi bliver noedt til at lave en ny...
        // Og det bliver nok ikke superlet at cache den...

        // disableAnnotatedMethodHook(Main.class, String
        throw new UnsupportedOperationException();
    }

    static ComponentRule disableAnnotatedMethodHook(Class<? extends Annotation> type, Object componentFilter) {
        // ComponentFilter
        // path
        // name
        // module
        // Bundle
        // ScannableType
        throw new UnsupportedOperationException();
    }
}