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
package app.packed.application.sandbox;

import app.packed.container.Extension;

/**
 * not to be confused with application layers in the OSI model.
 */
// How to extend?
// Can't use extends - because define - Would be nice with sealed though
// I don't know if type classes can do something



// Devtools has a layer test that can make sure the extensions exist

// ban, prohibit, deny, 
public abstract class ApplicationLayer {

    /** Create a root application layer. */
    protected ApplicationLayer() {}

    /**
     * Create a application layer a parent.
     * 
     * @param parent
     *            the parent layer
     */
    // Problemet er at alle kan sige de er et layer... Maybe an allowedSubLayers
    protected ApplicationLayer(Class<? extends ApplicationLayer> parent) {}

    // default is in-out
    // no-in, out
    // out, no-in
    // no-in,no-out
    
    protected final void allowServicesFrom(Class<? extends ApplicationLayer> layers) {}

    /**
     * Defines the application layer.
     */
    protected abstract void define();

    protected final void forbiddenExtension(Class<? extends Extension<?>> ext) {}

    protected final void forbiddenExtension(String extensionName) {}

    // beans, assemblies, extensions
    protected final void forbiddenModule(String name) {}

    // Only this layer can use the JDBC extension

    // reserve extension?
    protected final void restrictExtensionTo(Class<? extends Extension<?>> ext) {}

    protected final void restrictExtensionTo(String extensionName) {}
}
// or just define.extends(Class<? extends ApplicationLayer> parent);