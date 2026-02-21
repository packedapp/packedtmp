/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.namespaceold;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentRealm;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionHandle;
import sandbox.app.packed.container.ContainerPropagator;

/**
 * The configuration of a namespace.
 * <p>
 * Each container will always get their own namespace configuration instance, as methods on the namespace configuration
 */
public non-sealed abstract class OldNamespaceConfiguration<E extends Extension<E>> extends ComponentConfiguration {

    private final E extension;

    /** A handle for the namespace. */
    private final OldNamespaceHandle<E, ?> handle;

    protected OldNamespaceConfiguration(OldNamespaceHandle<E, ?> namespace, E extension, ComponentRealm actor) {
        this.handle = requireNonNull(namespace);
        this.extension = requireNonNull(extension);
        // TODO check that the extension is the right type for the namespace
    }

    public final ComponentRealm authority() {
        return handle.namespace.owner.owner();
    }

    // But how the fuck do you update these???
    // I think root always has all permissions????
    // Permissions are nice here. Because we are CrossAssembly
    // OTher wise a bit annoying
    protected final void checkPermission(Object permission) {

    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration tag(String... tags) {
        checkIsConfigurable();
        handle.componentTag(tags);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return handle.componentTags();
    }

    protected final E extension() {
        return extension;
    }

    protected final ExtensionHandle<E> extensionHandle() {
        return new PackedExtensionHandle<>(ExtensionSetup.crack(extension));
    }

    /** {@inheritDoc} */
    @Override
    protected final OldNamespaceHandle<E, ?> handle() {
        return handle;
    }

    public OldNamespaceConfiguration<E> noPropagation() {
        return propagate(ContainerPropagator.LOCAL);
    }

    @SuppressWarnings("exports")
    public OldNamespaceConfiguration<E> propagate(ContainerPropagator propagator) {
        // throw new UnsupportedOperationException();

        return this;
    }

    /** {@return the root extension of the namespace} */
    @SuppressWarnings("unchecked")
    protected final E rootExtension() {
        return (E) handle.namespace.root.instance();
    }

    /** {@return the root extension of the namespace} */
    protected final ExtensionHandle<E> rootExtensionHandle() {
        return new PackedExtensionHandle<>(handle.namespace.root);
    }
}

// CRAP! Maaden man installere beans fra brugere, og extensions er jo helt forskelligt....
// Og man kan jo ikke saadan bare faa fat i en ExtensionPoint.UseSite
// Maaske have metoder direkte her...

//Hvad hvis en extension vil configure et namespace....... ARGHHHHH
//Skal vi have en Author med???

//This must be towards the user? Yes the template (maybe coupled with a NamespaceHandle.Builder) is for extensions

//But still what exactly are we configuring here???
//We cannot add beans... These must always be added on the extension.
//Well the c

//Vi laver en configuration per authority og per container...
//Maaske cacher vi den... Men vi vil gerne lave den lazy
////Hmm skal den vaere med i configurations()... Tjah saa skal vi jo lave en cache trick.
//Med mindre vi bare instantiere den hver gang

//We have an NamespaceConfiguration per extension and per authority
//We do not distinguish about whether or not the user is the user or an Extension

//Namespace by itself it not particular usefull, but all other component configuration are not abstract
//They may also serve as an activity record. Whenever a Conf is created we are active...
//Also if we have onXX we need to maintain them once created

//This is not called NamespaceNodeConfiguration because we dont want ServiceNamespaceNodeConfiguration
// We need some info about whether or not it is still configurable.
// maybe Extension.isConfigurable... I'm thinking about extensions in deep down assemblies
