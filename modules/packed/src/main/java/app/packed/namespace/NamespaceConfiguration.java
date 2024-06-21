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
package app.packed.namespace;

import static java.util.Objects.requireNonNull;

import app.packed.component.Authority;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentHandle;
import app.packed.container.ContainerPropagator;
import app.packed.extension.Extension;
import app.packed.util.TreeView;

/**
 * The configuration of a namespace.
 * <p>
 * Namespace configurations are unique per container|name configuration.
 *
 * <p>
 * Since a namespace can be shared between multiple may be shared across assemblies. Each container gets their own configuration instance.
 *
 */

// Hvad hvis en extension vil configure et namespace....... ARGHHHHH
// Skal vi have en Author med???

// This must be towards the user? Yes the template (maybe coupled with a NamespaceHandle.Builder) is for extensions

// But still what exactly are we configuring here???
// We cannot add beans... These must always be added on the extension.
// Well the c

// Vi laver en configuration per authority og per container...
// Maaske cacher vi den... Men vi vil gerne lave den lazy
//// Hmm skal den vaere med i configurations()... Tjah saa skal vi jo lave en cache trick.
// Med mindre vi bare instantiere den hver gang


// We have an NamespaceConfiguration per extension and per authority
// We do not distinguish about whether or not the user is the user or an Extension

// Namespace by itself it not particular usefull, but all other component configuration are not abstract
// They may also serve as an activity record. Whenever a Conf is created we are active...
// Also if we have onXX we need to maintain them once created

// This is not called NamespaceNodeConfiguration because we dont want ServiceNamespaceNodeConfiguration
public class NamespaceConfiguration<E extends Extension<E>> extends ComponentConfiguration {


    // CRAP! Maaden man installere beans fra brugere, og extensions er jo helt forskelligt....
    // Og man kan jo ikke saadan bare faa fat i en ExtensionPoint.UseSite
    // Maaske have metoder direkte her...
    Authority authority;

    E extension;

    // The handle is for whole namespace. Visible only to the extension itself
    private final NamespaceHandle<?> handle;

    protected NamespaceConfiguration(NamespaceHandle<?> handle) {
        this.handle = requireNonNull(handle);
    }

    protected final Authority authority() {
        throw new UnsupportedOperationException();
    }

    // But how the fuck do you update these???
    // I think root always has all permissions????
    // Permissions are nice here. Because we are CrossAssembly
    // OTher wise a bit annoying
    protected final void checkPermission(Object permission) {

    }

//    @Override
//    public final ComponentPath componentPath() {
//        // We have the unique name of the extension/namespace
//        // We have the path of the container
//        // We have the name of the namespace
//
//        // Maybe namespace types need a unique name similar to extensions within an application
//        // I think they are specific type of componentKind for each namespace
//        return null;
//    }

    @Override
    public ComponentConfiguration componentTag(String... tags) {
        throw new UnsupportedOperationException();
    }

    /** {@return the extension instance for which this configuration has been created.} */
    protected final E extension() {
        throw new UnsupportedOperationException();
    }

    public String name() {
        return handle.name();
    }

    protected final TreeView.Node<Extension<?>> node() {
        throw new UnsupportedOperationException();
    }

    // Unless Explicitly set
    // Kan vi rename efter den er blevet bundet???
    // Kan risikere kollisioner i en assembly laengere nede

    public NamespaceConfiguration<E> noPropagation(){
        return propagate(ContainerPropagator.LOCAL);
    }

    // Der er et eller andet naar vi laver namespaces efter at have linket... En container
    // Af gode grund kan den ikke vaere tilgaengelig

    // Tror sgu ikke vi kan reneme it namespace
//    public NamespaceConfiguration<E> named(String name) {
//        handle.named(name);
//        return this;
//    }

    public NamespaceConfiguration<E> propagate(ContainerPropagator propagator){
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected ComponentHandle componentHandle() {
        return handle;
    }
}

// We need some info about whether or not it is still configurable.
// maybe Extension.isConfigurable... I'm thinking about extensions in deep down assemblies
